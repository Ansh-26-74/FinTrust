package com.Ansh.FinTrust.Helper;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public class PhoneNumberValidation {

    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[6-9]\\d{9}$");
    }

    public boolean isValidCountryCode(String code) {
        return code != null && code.matches("^\\+\\d{1,3}$");
    }


}
