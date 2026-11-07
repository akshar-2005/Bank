package com.akshar.bank.controller;

import com.akshar.bank.dto.AccountResponse;
import com.akshar.bank.dto.OpenAccountRequest;
import com.akshar.bank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> openAccount(Authentication authentication,
            @Valid @RequestBody OpenAccountRequest request) {
        AccountResponse response = accountService.openAccount(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse getAccount(Authentication authentication,
            @PathVariable String accountNumber) {
        return accountService.getAccount(authentication.getName(), accountNumber);
    }

    @GetMapping
    public List<AccountResponse> myAccounts(Authentication authentication) {
        return accountService.myAccounts(authentication.getName());
    }
}
