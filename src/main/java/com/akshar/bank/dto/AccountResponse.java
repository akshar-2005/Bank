package com.akshar.bank.dto;

import com.akshar.bank.entity.AccountType;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        String accountNumber,
        BigDecimal balance,
        AccountType type) {
}