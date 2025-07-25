package com.Ansh.FinTrust.Services;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

public interface EmailService {

    public void sendEmail(String to, String subject, String content)throws MessagingException;

}
