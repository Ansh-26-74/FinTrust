package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.SuspiciousEventType;
import com.Ansh.FinTrust.Entities.SuspiciousActivity;

import java.util.List;

public interface SuspiciousActivityService {

    void logEvent(String userId, SuspiciousEventType eventType, String details);

    List<SuspiciousActivity> getAllLogs();

    List<SuspiciousActivity> getLogsByUser(String userId);

}
