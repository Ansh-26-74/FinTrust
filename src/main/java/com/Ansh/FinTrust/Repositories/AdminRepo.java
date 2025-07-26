package com.Ansh.FinTrust.Repositories;

import com.Ansh.FinTrust.Entities.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdminRepo extends MongoRepository<Admin, String> {
    Optional<Admin> findByUsername(String username);
}
