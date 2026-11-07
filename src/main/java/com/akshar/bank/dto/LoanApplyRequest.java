package com.akshar.bank.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record LoanApplyRequest(
        @NotBlank String accountNumber,
        @NotNull @DecimalMin("1000.00") BigDecimal principal,
        @NotNull @DecimalMin("0.01") @DecimalMax("50.00") BigDecimal annualInterestRate,
        @NotNull @Min(1) @Max(360) Integer tenureMonths) {
}