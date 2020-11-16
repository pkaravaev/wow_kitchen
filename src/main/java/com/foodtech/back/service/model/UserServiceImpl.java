package com.foodtech.back.service.model;

import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.dto.model.UserUpdateDto;
import com.foodtech.back.entity.model.Address;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.entity.model.iiko.DeliveryCoordinate;
import com.foodtech.back.repository.model.UserRepository;
import com.foodtech.back.service.bonus.BonusService;
import com.foodtech.back.util.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final AddressService addressService;

    private final BonusService bonusService;

    private final UserPreferencesService preferencesService;

    public UserServiceImpl(UserRepository userRepository, AddressService addressService, BonusService bonusService,
                           UserPreferencesService preferencesService) {
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.bonusService = bonusService;
        this.preferencesService = preferencesService;
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> get(Long userId){
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> get(FullMobileNumber fullNumber) {
        return userRepository.findByCountryCodeAndMobileNumber(fullNumber.getCountryCode(), fullNumber.getMobileNumber());
    }

    @Override
    public User getWithActualAddress(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        Address actualAddress = addressService.getActualByUser(userId);
        user.setActualAddress(actualAddress);
        return user;
    }

    @Override
    @Transactional
    public User login(CredentialsAuthDto authDto) {
        FullMobileNumber fullNumber = authDto.getFullNumber();
        Address address = authDto.getAddress();
        Optional<User> userOpt = userRepository.findByCountryCodeAndMobileNumber(fullNumber.getCountryCode(),
                fullNumber.getMobileNumber());
        return userOpt.isEmpty() ? createNew(fullNumber, address, authDto.getCoordinates())
                                 : loginExisting(userOpt.get(), address, authDto.getCoordinates());
    }

    private User createNew(FullMobileNumber fullNumber, Address address, DeliveryCoordinate coordinate) {
        log.info("Creating new user - {}", fullNumber);
        User newUser = UserMapper.newUser(fullNumber, address);
        newUser.setBonusAccount(bonusService.createBonusAccount());
        userRepository.save(newUser);
        addressService.add(newUser, address, coordinate);
        preferencesService.create(newUser);
        return newUser;
    }

    private User loginExisting(User user, Address address, DeliveryCoordinate coordinate) {
        log.info("Login user '{}'", user);
        addressService.add(user, address, coordinate);
        return user;
    }

    @Override
    @Transactional
    public User update(Long userId, UserUpdateDto userDto) {
        User user = userRepository.findById(userId).orElseThrow();
        UserMapper.updateData(user, userDto);
        return user;
    }
}
