package com.akshar.bank.dto;

import com.akshar.bank.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OpenAccountRequest(
        @NotNull AccountType type,
        @NotNull @DecimalMin("0.00") BigDecimal initialDeposit) {
}