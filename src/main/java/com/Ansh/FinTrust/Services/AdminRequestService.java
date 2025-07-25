package com.Ansh.FinTrust.Services;

import com.Ansh.FinTrust.DTO.AdminRequest;

import java.util.List;

public interface AdminRequestService {

    public boolean isAccessApproved(String adminUsername, String targetUsername, String filename, String operation);
    public void createAccessRequest(String adminUsername, String targetUsername, String filename, String operation) throws Exception;
    public List<String> getUsersThatApprovedAccess(String adminUsername, String action) throws Exception;
    public void respondToAccessRequests(String requestId, String action, String currentUser) throws Exception;
    public List<AdminRequest> getPendingRequestsForUser(String username);
}
