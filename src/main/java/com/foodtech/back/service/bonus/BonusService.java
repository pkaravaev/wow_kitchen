package com.foodtech.back.service.bonus;

import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.entity.bonus.PromoCodeImpersonal;
import com.foodtech.back.entity.model.Order;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.repository.bonus.PromoCodeImpersonalRepository;
import com.foodtech.back.repository.model.OrderRepository;
import com.foodtech.back.repository.model.UserRepository;
import com.foodtech.back.util.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class BonusService {

    public static final int REG_PROMO_CODE_LENGTH = 5;
    public final static int REGISTRATION_PROMO_CODE_BONUS_AMOUNT = 500;

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    private final PromoCodeImpersonalRepository promoCodeImpersonalRepository;

    public BonusService(UserRepository userRepository, OrderRepository orderRepository,
                        PromoCodeImpersonalRepository promoCodeImpersonalRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.promoCodeImpersonalRepository = promoCodeImpersonalRepository;
    }

    public BonusAccount createBonusAccount() {
        BonusAccount account = new BonusAccount();
        account.setRegistrationPromoCode(createRegistrationPromoCode());
        account.setBonusAmount(0);
        account.setBonusesSpent(0);
        account.setRegistrationPromoCodeUsed(false);
        account.setRegistrationPromoCashbackReturned(false);
        return account;
    }

    private String createRegistrationPromoCode() {
        String code;
        do {
            code = generateRegistrationPromoCode();
        } while (userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code));

        return code;
    }

    private String generateRegistrationPromoCode() {
        return RandomStringUtils.random(REG_PROMO_CODE_LENGTH, true, true);
    }

    /* На настоящий момент имеется два вида промо-кодов: регистрационный и неперсонализированный
    * регистрационный выдается в момент регистрации и используется для приглашения друзей в приложение и получить за это бонусы,
    * неперсонализированный позволяет получить бонусы (юзер может использовать конкретный неперсон. промо-код только один раз) */
    public Integer applyPromoCode(String code, Long userId) {
        User user = userRepository.findByIdForUpdate(userId).orElseThrow();
        Optional<Long> codeOwnerId = userRepository.findPromoCodeOwnerId(code.trim());
        return codeOwnerId.isPresent() ? applyRegistrationPromoCode(codeOwnerId.get(), user)
                : applyImpersonalPromoCode(code, user);
    }

    private Integer applyRegistrationPromoCode(Long codeOwnerId, User user) {

        if (user.getId().equals(codeOwnerId)) {
            throw new OwnPromoCodeApplyingException();
        }

        BonusAccount bonusAccount = user.getBonusAccount();
        if (bonusAccount.isRegistrationPromoCodeUsed()) {
            throw new RegistrationPromoCodeAlreadyUsedException();
        }

        bonusAccount.setRegistrationPromoCodeOwnerUserId(codeOwnerId);
        bonusAccount.addBonuses(REGISTRATION_PROMO_CODE_BONUS_AMOUNT);
        bonusAccount.setRegistrationPromoCodeUsed(true);
        bonusAccount.setRegistrationPromoCashbackReturned(false);
        log.info("Registration promo code for user '{}' successfully applied", user);
        return REGISTRATION_PROMO_CODE_BONUS_AMOUNT;
    }

    private Integer applyImpersonalPromoCode(String code, User user) {

        PromoCodeImpersonal promoCode = promoCodeImpersonalRepository.findByPromoCodeEquals(code)
                .orElseThrow(PromoCodeNotFoundException::new);

        /* Проверяем, что юзер еще не использовал этот промо-код */
        if (user.getUsedPromoCodes().contains(promoCode)) {
            throw new PromoCodeAlreadyUsedException();
        }

        user.getBonusAccount().addBonuses(promoCode.getAmount());
        user.getUsedPromoCodes().add(promoCode);
        log.info("Impersonal promo code '{}' successfully applied for user '{}'", code, user);
        return promoCode.getAmount();
    }

    public void changeRegistrationPromoCode(String code, Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        BonusAccount bonusAccount = user.getBonusAccount();
        code = code.trim().replaceAll(" ", "");
        validateRegistrationPromoCode(code);
        bonusAccount.setRegistrationPromoCode(code);
    }

    private void validateRegistrationPromoCode(String code) {
        if (code.length() < 5 || code.length() > 10 ) {
            throw new ConstraintViolationException("promo code length invalid", null);
        }

        if (userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code)) {
            throw new PromoCodeAlreadyExistsException();
        }
    }

    public boolean writeOffBonuses(Long userId, int appliedBonusAmount) {

        if (appliedBonusAmount == 0) {
            return true;
        }

        if (appliedBonusAmount < 0) {
            log.error("Error during writing off bonuses. Applied bonus amount can't be negative");
            return false;
        }

        /* Запрос, вешающий pessimistic лок на запись в базе, чтобы избежать ситуации,
        когда одновременно с одного бонусного аккаунта используются одни и те же баллы для разных заказов */
        User user = userRepository.findByIdForUpdate(userId).orElseThrow();
        BonusAccount bonusAccount = user.getBonusAccount();
        if (bonusAccount.getBonusAmount() < appliedBonusAmount) {
            log.error("Error during writing off bonuses. User bonus amount less than applied bonus amount. User: '{}'", user);
            return false;
        }

        bonusAccount.writeOffBonuses(appliedBonusAmount);
        bonusAccount.addBonusesSpent(appliedBonusAmount);
        log.info("Written off {} bonuses from user '{}'", appliedBonusAmount, user);
        return true;
    }

    public void checkAndGiveBonusesToPromoCodeOwner(Long userId) {
        User user = userRepository.findByIdForUpdate(userId).orElseThrow();
        if (needToGiveBonusesToPromoCodeOwner(user.getBonusAccount())) {
            giveBonusesToPromoCodeOwner(user);
        }
    }

    private boolean needToGiveBonusesToPromoCodeOwner(BonusAccount bonusAccount) {
        return bonusAccount.isRegistrationPromoCodeUsed()
                && !bonusAccount.isRegistrationPromoCashbackReturned()
                && bonusAccount.getBonusesSpent() >= REGISTRATION_PROMO_CODE_BONUS_AMOUNT;
    }

    private void giveBonusesToPromoCodeOwner(User user) {
        Long codeOwnerUserId = user.getBonusAccount().getRegistrationPromoCodeOwnerUserId();
        User codeOwner = userRepository.findById(codeOwnerUserId).orElseThrow();
        codeOwner.getBonusAccount().addBonuses(REGISTRATION_PROMO_CODE_BONUS_AMOUNT);
        user.getBonusAccount().setRegistrationPromoCashbackReturned(true);
        log.info("User '{}' spent registration bonuses. Returned bonuses to promo code owner: '{}'", user, codeOwner);
    }

    public void returnBonuses(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        BonusAccount bonusAccount = order.getUser().getBonusAccount();
        Integer returnAmount = order.getAppliedBonusAmount();
        bonusAccount.addBonuses(returnAmount);
        int bonusesSpent = bonusAccount.getBonusesSpent() - returnAmount;
        bonusAccount.setBonusesSpent(Math.max(bonusesSpent, 0));
    }

    public int countCartCostWithBonuses(int costWithoutBonuses, int userBonusAmount) {
        if (userBonusAmount < 0 || costWithoutBonuses < 0) {
            throw new IllegalArgumentException("Bonuses amount can't be negative number");
        }

        int half = costWithoutBonuses/2;
        return (half >= userBonusAmount) ? (costWithoutBonuses - userBonusAmount) : (costWithoutBonuses - half);
    }


    /* Методы для админки */

    public List<PromoCodeImpersonal> getImpersonalPromoCodes() {
        return promoCodeImpersonalRepository.findAll();
    }

    public boolean addImpersonalPromoCode(PromoCodeImpersonal code) {
        boolean codeIsValid = Objects.isNull(code.getId())
                && !userRepository.existsByBonusAccountRegistrationPromoCodeEquals(code.getPromoCode())
                && !promoCodeImpersonalRepository.existsByPromoCodeEquals(code.getPromoCode());

        if (codeIsValid) {
            promoCodeImpersonalRepository.save(code);
            return true;
        }

        return false;
    }

    public void deleteImpersonalPromoCode(Long id) {
        promoCodeImpersonalRepository.deleteById(id);
    }

    public void changeBonusAmount(Long userId, Integer newValue) {
        User user = userRepository.findById(userId).orElseThrow();
        user.getBonusAccount().setBonusAmount(newValue);
    }
}
