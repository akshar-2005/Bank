package com.akshar.bank.controller;

import com.akshar.bank.dto.LoanApplyRequest;
import com.akshar.bank.dto.LoanResponse;
import com.akshar.bank.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping
    public ResponseEntity<LoanResponse> apply(Authentication authentication,
            @Valid @RequestBody LoanApplyRequest request) {
        LoanResponse response = loanService.apply(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    public List<LoanResponse> myLoans(Authentication authentication) {
        return loanService.myLoans(authentication.getName());
    }
}