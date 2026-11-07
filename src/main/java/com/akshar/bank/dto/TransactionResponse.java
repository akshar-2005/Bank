package com.akshar.bank.dto;

import com.akshar.bank.entity.TxnStatus;
import com.akshar.bank.entity.TxnType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        TxnType type,
        TxnStatus status,
        BigDecimal amount,
        String fromAccountNumber,
        String toAccountNumber,
        String description,
        LocalDateTime createdAt) {
}
