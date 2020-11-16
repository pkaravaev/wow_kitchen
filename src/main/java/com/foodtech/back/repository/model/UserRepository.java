package com.foodtech.back.repository.model;

import com.foodtech.back.entity.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("NullableProblems")
public interface UserRepository extends JpaRepository<User, Long> {

    @Override
    List<User> findAll();

    @Override
    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id=?1")
//    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "10000")})
    Optional<User> findByIdForUpdate(Long id);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByCountryCodeAndMobileNumber(String countryCode, String mobileNumber);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles " +
            "LEFT JOIN FETCH u.addresses " +
            "WHERE u.countryCode=?1 AND u.mobileNumber=?2")
    Optional<User> findByCountryCodeAndMobileNumberWithAddresses(String countryCode, String mobileNumber);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.roles " +
            "LEFT JOIN FETCH u.addresses " +
            "WHERE u.id=?1")
    Optional<User> findByIdWithAddresses(Long id);

    @Query("SELECT DISTINCT u.id FROM User u WHERE u.bonusAccount.registrationPromoCode =?1")
    Optional<Long> findPromoCodeOwnerId(String code);

    boolean existsByBonusAccountRegistrationPromoCodeEquals(String code);

    void deleteByMobileNumber(String number);
}
