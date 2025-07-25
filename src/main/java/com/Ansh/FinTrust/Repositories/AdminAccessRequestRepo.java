package com.Ansh.FinTrust.Repositories;

import com.Ansh.FinTrust.Entities.AdminAccessRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface AdminAccessRequestRepo extends MongoRepository<AdminAccessRequest, String> {
    Optional<AdminAccessRequest> findByAdminUsernameAndTargetUsernameAndFilenameAndOperationAndStatus(
            String adminUsername,
            String targetUsername,
            String filename,
            String operation,
            String status
    );

    List<AdminAccessRequest> findAllByAdminUsernameAndOperationAndStatus(
            String adminUsername,
            String operation,
            String status
    );

    List<AdminAccessRequest> findAllByTargetUsernameAndStatus(String targetUsername, String status);

}

