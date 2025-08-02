package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.SuspiciousEventType;
import com.Ansh.FinTrust.Entities.SuspiciousActivity;
import com.Ansh.FinTrust.Repositories.SuspiciousActivityRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuspiciousActivityServiceImpl implements SuspiciousActivityService{

    private final SuspiciousActivityRepo suspiciousActivityRepo;

    @Override
    public void logEvent(String userId, SuspiciousEventType eventType, String details) {
        SuspiciousActivity log = new SuspiciousActivity();

        log.setUserId(userId);
        log.setEventType(eventType);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        suspiciousActivityRepo.save(log);
    }

    @Override
    public List<SuspiciousActivity> getAllLogs() {
        return suspiciousActivityRepo.findAll();
    }

    @Override
    public List<SuspiciousActivity> getLogsByUser(String userId) {
        return suspiciousActivityRepo.findByUserId(userId);
    }
}
