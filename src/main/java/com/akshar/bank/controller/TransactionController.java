package com.akshar.bank.controller;

import com.akshar.bank.dto.AmountRequest;
import com.akshar.bank.dto.PageResponse;
import com.akshar.bank.dto.TransactionResponse;
import com.akshar.bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts/{accountNumber}")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public TransactionResponse deposit(Authentication authentication,
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {
        return transactionService.deposit(authentication.getName(), accountNumber, request.amount());
    }

    @PostMapping("/withdraw")
    public TransactionResponse withdraw(Authentication authentication,
            @PathVariable String accountNumber,
            @Valid @RequestBody AmountRequest request) {
        return transactionService.withdraw(authentication.getName(), accountNumber, request.amount());
    }

    @GetMapping("/transactions")
    public PageResponse<TransactionResponse> history(Authentication authentication,
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return transactionService.getHistory(authentication.getName(), accountNumber, page, size);
    }
}