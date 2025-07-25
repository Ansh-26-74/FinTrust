package com.Ansh.FinTrust.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoleTestController {

    @GetMapping("/admin/hello")
    public String adminHello() {
        return "Hello Admin! You have access.";
    }

    @GetMapping("/user/hello")
    public String userHello() {
        return "Hello User! You are authenticated.";
    }
}
