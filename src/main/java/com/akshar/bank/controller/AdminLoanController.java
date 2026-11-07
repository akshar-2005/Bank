package com.akshar.bank.controller;

import com.akshar.bank.dto.LoanResponse;
import com.akshar.bank.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/loans")
@RequiredArgsConstructor
public class AdminLoanController {

    private final LoanService loanService;

    @GetMapping("/pending")
    public List<LoanResponse> pending() {
        return loanService.pendingLoans();
    }

    @PostMapping("/{id}/approve")
    public LoanResponse approve(@PathVariable Long id) {
        return loanService.approve(id);
    }

    @PostMapping("/{id}/reject")
    public LoanResponse reject(@PathVariable Long id) {
        return loanService.reject(id);
    }
}