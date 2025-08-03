package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.SuspiciousEventType;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Repositories.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class SessionPinServiceImpl implements SessionPinService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SuspiciousActivityService suspiciousActivityService;
    private final UserRepo userRepo;

    public SessionPinServiceImpl(@Qualifier("redisTemplate1") RedisTemplate<String, String> redisTemplate, SuspiciousActivityService suspiciousActivityService, UserRepo userRepo) {
        this.redisTemplate = redisTemplate;
        this.suspiciousActivityService = suspiciousActivityService;
        this.userRepo = userRepo;
    }

    @Override
    public String generateAndStorePin(String username) {
        String pin = String.valueOf((int) (Math.random() * 900000) + 100000);

        redisTemplate.opsForValue().set("session_pin:" + username, pin, Duration.ofMinutes(30));

        return pin;
    }

    @Override
    public boolean validatePin(String username, String pin) {
        String storedPin = redisTemplate.opsForValue().get("session_pin:" + username);
        Optional<User> user = userRepo.findByUsername(username);

        if (storedPin == null || !storedPin.equals(pin)) {
            suspiciousActivityService.logEvent(
                    user.get().getId(),
                    SuspiciousEventType.WRONG_PIN_ENTRY,
                    "User entered incorrect session PIN"
            );
            return false;
        }
        return true;
    }

    @Override
    public void deletePin(String username) {
        redisTemplate.delete("session_pin:" + username);
    }

}
