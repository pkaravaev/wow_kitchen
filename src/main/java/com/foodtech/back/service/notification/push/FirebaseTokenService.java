package com.foodtech.back.service.notification.push;

import com.foodtech.back.entity.auth.FirebaseToken;
import com.foodtech.back.entity.model.User;
import com.foodtech.back.repository.auth.FirebaseTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class FirebaseTokenService {

    private final FirebaseTokenRepository tokenRepository;

    public FirebaseTokenService(FirebaseTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public void setToken(User user, String token) {
        Optional<FirebaseToken> tokenOpt = tokenRepository.findByUserId(user.getId());
        if (tokenOpt.isPresent()) {
            tokenOpt.get().setToken(token);
        } else {
            tokenRepository.save(new FirebaseToken(token, user));
        }
    }
}
