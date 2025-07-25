package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.AdminRequest;
import com.Ansh.FinTrust.Entities.AdminAccessRequest;
import com.Ansh.FinTrust.Entities.User;
import com.Ansh.FinTrust.Repositories.AdminAccessRequestRepo;
import com.Ansh.FinTrust.Repositories.UserRepo;
import com.mongodb.internal.logging.LoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.expression.ExpressionException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRequestServiceImpl implements AdminRequestService {

    private final MongoTemplate mongoTemplate;
    private final AdminAccessRequestRepo adminAccessRequestRepo;
    private final UserRepo userRepo;
    private final EmailService emailService;

    public boolean isAccessApproved(String adminUsername, String targetUsername, String filename, String operation) {
        return adminAccessRequestRepo.findByAdminUsernameAndTargetUsernameAndFilenameAndOperationAndStatus(
                adminUsername, targetUsername, filename, operation, "APPROVED"
        ).isPresent();
    }

    public void createAccessRequest(String adminUsername, String targetUsername, String filename, String operation) throws Exception{
        AdminAccessRequest request = new AdminAccessRequest();
        request.setAdminUsername(adminUsername);
        request.setTargetUsername(targetUsername);
        request.setFilename(filename);
        request.setOperation(operation);
        request.setStatus("PENDING");
        request.setRequestedAt(new Date());
        adminAccessRequestRepo.save(request);

        User targetUser = userRepo.findByUsername(targetUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

        emailService.sendEmail(
                targetUser.getEmail(),  // assuming your User entity has getEmail()
                "Admin Access Request - FinTrust",
                "Admin <b>" + adminUsername + "</b> is requesting to <b>" + operation +
                        "</b> your file: <b>" + filename + "</b>. Please login and approve or reject the request."
        );
    }

    public List<String> getUsersThatApprovedAccess(String adminUsername, String operation) throws Exception{
        List<String> approvedUsernames = new ArrayList<>();

        List<String> allUsernames = mongoTemplate.getCollection("fs.files")
                .distinct("metadata.username", String.class)
                .into(new ArrayList<>());

        for (String targetUsername : allUsernames) {
            if (targetUsername.equals(adminUsername)) continue;

            boolean isApproved = isAccessApproved(adminUsername, targetUsername, null, operation);

            if (isApproved) {
                approvedUsernames.add(targetUsername);
            } else {
                Optional<AdminAccessRequest> existing = adminAccessRequestRepo
                        .findByAdminUsernameAndTargetUsernameAndFilenameAndOperationAndStatus(
                                adminUsername, targetUsername, null, operation, "PENDING"
                        );
                if (existing.isEmpty()) {
                    AdminAccessRequest request = new AdminAccessRequest();
                    request.setAdminUsername(adminUsername);
                    request.setTargetUsername(targetUsername);
                    request.setOperation(operation);
                    request.setStatus("PENDING");
                    request.setRequestedAt(new Date());

                    adminAccessRequestRepo.save(request);

                    User targetUser = userRepo.findByUsername(targetUsername)
                            .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));

                    emailService.sendEmail(
                            targetUser.getEmail(),
                            "Admin Access Request - FinTrust",
                            "Admin <b>" + adminUsername + "</b> is requesting to <b>" + operation +
                                    "</b> your files. Please login and approve or reject the request."
                    );
                }

            }
        }
        return approvedUsernames;
    }

    public void respondToAccessRequests(String requestId, String action, String currentUser) throws Exception{
        AdminAccessRequest request = adminAccessRequestRepo.findById(requestId)
                .orElseThrow(() -> new Exception("Request not found"));

        if (!request.getTargetUsername().equals(currentUser)) {
            throw new Exception("Unauthorized action");
        }

        if (!request.getStatus().equals("PENDING")) {
            throw new Exception("Request is already responded to");
        }

        request.setStatus(action.toUpperCase());
        request.setRespondedAt(new Date());

        adminAccessRequestRepo.save(request);
    }

    public List<AdminRequest> getPendingRequestsForUser(String username) {
        List<AdminAccessRequest> pendingRequests = adminAccessRequestRepo
                .findAllByTargetUsernameAndStatus(username, "PENDING");

        return pendingRequests.stream()
                .map(req -> new AdminRequest(
                        req.getId(),
                        req.getAdminUsername(),
                        req.getFilename(),
                        req.getOperation(),
                        req.getRequestedAt()
                ))
                .toList();
    }


}
