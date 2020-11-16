package com.foodtech.back.util.mapper;

import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.dto.model.UserInfoDto;
import com.foodtech.back.dto.model.UserUpdateDto;
import com.foodtech.back.dto.payment.cloud.CloudPaymentResponse;
import com.foodtech.back.entity.auth.Role;
import com.foodtech.back.entity.bonus.BonusAccount;
import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.payment.BankCard;
import com.foodtech.back.security.JwtUser;
import org.modelmapper.ModelMapper;

import java.util.Set;

public class UserMapper {

    private static final ModelMapper MAPPER = new ModelMapper();

    public static User newUser(FullMobileNumber fullNumber, Address address) {
        User user = new User();
        user.setCountryCode(fullNumber.getCountryCode());
        user.setMobileNumber(fullNumber.getMobileNumber());
        user.setEnabled(true);
        user.setRoles(Set.of(Role.ROLE_USER));
        return user;
    }

    public static UserInfoDto toUserDto(User user, BankCard actualCard) {
        BonusAccount bonusAccount = user.getBonusAccount();
        UserInfoDto userDto = new UserInfoDto();
        userDto.setFullNumber(user.getFullMobileNumber());
        userDto.setName(user.getName());
        userDto.setBonusAmount(bonusAccount.getBonusAmount());
        userDto.setRegistrationPromoCode(bonusAccount.getRegistrationPromoCode());
        userDto.setBankCard(actualCard);
        userDto.setAddress(user.getActualAddress());
        return userDto;
    }

    public static User toUser(JwtUser jwtUser) {
        return MAPPER.map(jwtUser, User.class);
    }

    public static void updateData(User user, UserUpdateDto dto) {
        user.setName(dto.getName().trim());
    }

    public static BankCard toBankCard(User user, CloudPaymentResponse model) {
        BankCard bankCard = new BankCard();
        bankCard.setUser(user);
        bankCard.setCardMask(model.getCardFirstSix() + "******" + model.getCardLastFour());
        bankCard.setCardIssuer(model.getIssuer());
        bankCard.setCardType(model.getCardType());
        bankCard.setToken(model.getToken());
        bankCard.setActual(true);
        return bankCard;
    }
}
