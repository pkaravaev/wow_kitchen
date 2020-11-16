package com.foodtech.back.service.model;

import com.foodtech.back.dto.auth.CredentialsAuthDto;
import com.foodtech.back.dto.model.FullMobileNumber;
import com.foodtech.back.dto.model.UserUpdateDto;
import com.foodtech.back.entity.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> getAll();

    Optional<User> get(Long userId);

    Optional<User> get(FullMobileNumber fullNumber);

    User getWithActualAddress(Long id);

    User update(Long userId, UserUpdateDto userDto);

    User login(CredentialsAuthDto authDto);

//    void deleteCard(Long userId, Long cardId);
}
