package com.foodtech.back.service.auth;

import com.foodtech.back.repository.auth.BlacklistAuthTokenRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BlacklistAuthTokenService {

    private final BlacklistAuthTokenRepository blacklistTokenRepository;

    public BlacklistAuthTokenService(BlacklistAuthTokenRepository blacklistTokenRepository) {
        this.blacklistTokenRepository = blacklistTokenRepository;
    }

    @Cacheable("blackListToken")
    public boolean tokenInBlacklist(String token) {
        return blacklistTokenRepository.existsByTokenEquals(token);
    }
}
