package com.Ansh.FinTrust.Repositories;

import com.Ansh.FinTrust.Entities.SuspiciousActivity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SuspiciousActivityRepo extends MongoRepository<SuspiciousActivity, String> {
    List<SuspiciousActivity> findByUserId(String userId);
}

