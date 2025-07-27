package com.Ansh.FinTrust.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SessionPinServiceImpl implements SessionPinService{

    private final RedisTemplate<String, String> redisTemplate;

    public SessionPinServiceImpl(@Qualifier("redisTemplate1") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String generateAndStorePin(String username) {
        String pin = String.valueOf((int)(Math.random() * 900000) + 100000);

        redisTemplate.opsForValue().set("session_pin:" + username, pin, Duration.ofMinutes(30));

        return pin;
    }

    @Override
    public boolean validatePin(String username, String pin) {
        String storedPin = redisTemplate.opsForValue().get("session_pin:" + username);
        return storedPin != null && storedPin.equals(pin);
    }

    @Override
    public void deletePin(String username) {
        redisTemplate.delete("session_pin:" + username);
    }

}
