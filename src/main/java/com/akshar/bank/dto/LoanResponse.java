package com.akshar.bank.dto;

import com.akshar.bank.entity.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LoanResponse(
        Long id,
        String applicantEmail,
        String accountNumber,
        BigDecimal principal,
        BigDecimal annualInterestRate,
        Integer tenureMonths,
        BigDecimal emi,
        LoanStatus status,
        LocalDateTime createdAt,
        LocalDateTime decidedAt) {
}