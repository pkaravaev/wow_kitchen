package com.foodtech.back.unit.service.model;

import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.dto.model.UserUpdateDto;
import com.foodtech.back.entity.auth.Role;
import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.repository.model.UserRepository;
import com.foodtech.back.service.bonus.BonusService;
import com.foodtech.back.service.model.AddressService;
import com.foodtech.back.service.model.UserPreferencesService;
import com.foodtech.back.service.model.UserServiceImpl;
import com.foodtech.back.unit.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceImplUnitTest extends AbstractUnitTest {

    private static final String COUNTRY_CODE = "7";
    private static final String MOBILE_NUMBER = "9999999999";
    private static final FullMobileNumber FULL_NUMBER = new FullMobileNumber(COUNTRY_CODE, MOBILE_NUMBER);

    @Autowired
    UserServiceImpl userService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    AddressService addressService;

    @MockBean
    BonusService bonusService;

    @MockBean
    UserPreferencesService preferencesService;

    private static CredentialsAuthDto credentialsAuthDto() {
        CredentialsAuthDto credentialsAuthDto = new CredentialsAuthDto();
        credentialsAuthDto.setFullNumber(FULL_NUMBER);
        credentialsAuthDto.setLatitude(BigDecimal.valueOf(50.0000));
        credentialsAuthDto.setLongitude(BigDecimal.valueOf(70.0000));
        Address address = new Address();
        address.setCity("Some city");
        address.setStreet("Some street");
        address.setHome("Some home");
        credentialsAuthDto.setAddress(address);
        return credentialsAuthDto;
    }

    @Test
    void loginNewUser() {
        //when
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE_NUMBER)).thenReturn(Optional.empty());

        //do
        User newUser = userService.login(credentialsAuthDto());

        assertUserDataValid(newUser);
        verify(userRepository, times(1)).save(newUser);
        verify(addressService, times(1)).add(newUser, credentialsAuthDto().getAddress(), credentialsAuthDto().getCoordinates());
        verify(preferencesService, times(1)).create(newUser);
    }

    @Test
    void loginExistingUser() {
        //when
        User existingUser = new User();
        existingUser.setCountryCode(COUNTRY_CODE);
        existingUser.setMobileNumber(MOBILE_NUMBER);
        existingUser.setEnabled(true);
        existingUser.setRoles(Set.of(Role.ROLE_USER));
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE_NUMBER)).thenReturn(Optional.of(existingUser));

        //do
        User loggedUser = userService.login(credentialsAuthDto());

        assertUserDataValid(loggedUser);
        verify(userRepository, times(0)).save(loggedUser);
        verify(preferencesService, times(0)).create(loggedUser);
        verify(addressService, times(1)).add(loggedUser, credentialsAuthDto().getAddress(), credentialsAuthDto().getCoordinates());
    }

    private void assertUserDataValid(User user) {
        assertEquals(COUNTRY_CODE, user.getCountryCode());
        assertEquals(MOBILE_NUMBER, user.getMobileNumber());
        assertEquals(true, user.getEnabled());
        assertEquals(Set.of(Role.ROLE_USER), user.getRoles());
    }

    @Test
    void update() {
        //when
        User user = new User();
        user.setName("Old name");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        //do
        UserUpdateDto updateDto = new UserUpdateDto("Updated name");
        User updatedUser = userService.update(1L, updateDto);

        //then
        assertEquals(updateDto.getName(), updatedUser.getName());
    }

    @Test
    void updateUserNotFound() {
        //when
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        //do
        UserUpdateDto updateDto = new UserUpdateDto("Updated name");
        assertThrows(NoSuchElementException.class, () -> userService.update(1L, updateDto));
    }

}