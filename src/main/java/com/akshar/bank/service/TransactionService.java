package com.akshar.bank.service;

import com.akshar.bank.dto.PageResponse;
import com.akshar.bank.dto.TransactionResponse;
import com.akshar.bank.dto.TransferRequest;
import com.akshar.bank.entity.Account;
import com.akshar.bank.entity.Transaction;
import com.akshar.bank.entity.TxnStatus;
import com.akshar.bank.entity.TxnType;
import com.akshar.bank.exception.ResourceNotFoundException;
import com.akshar.bank.repository.AccountRepository;
import com.akshar.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponse deposit(String email, String accountNumber, BigDecimal amount) {
        Account account = findOwnedAccount(email, accountNumber);

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction txn = new Transaction();
        txn.setToAccount(account);
        txn.setAmount(amount);
        txn.setType(TxnType.DEPOSIT);
        txn.setStatus(TxnStatus.SUCCESS);
        txn.setDescription("Cash deposit");

        return toResponse(transactionRepository.save(txn));
    }

    @Transactional
    public TransactionResponse withdraw(String email, String accountNumber, BigDecimal amount) {
        Account account = findOwnedAccount(email, accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction txn = new Transaction();
        txn.setFromAccount(account);
        txn.setAmount(amount);
        txn.setType(TxnType.WITHDRAW);
        txn.setStatus(TxnStatus.SUCCESS);
        txn.setDescription("Cash withdrawal");

        return toResponse(transactionRepository.save(txn));
    }

    @Transactional
    public TransactionResponse transfer(String email, TransferRequest request) {
        if (request.fromAccountNumber().equals(request.toAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        Account from = findOwnedAccount(email, request.fromAccountNumber());
        Account to = findAccount(request.toAccountNumber());
        BigDecimal amount = request.amount();

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        accountRepository.save(from);
        accountRepository.save(to);

        Transaction txn = new Transaction();
        txn.setFromAccount(from);
        txn.setToAccount(to);
        txn.setAmount(amount);
        txn.setType(TxnType.TRANSFER);
        txn.setStatus(TxnStatus.SUCCESS);
        txn.setDescription("Fund transfer");

        return toResponse(transactionRepository.save(txn));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getHistory(String email, String accountNumber, int page, int size) {
        Account account = findOwnedAccount(email, accountNumber);

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
        Page<Transaction> result = transactionRepository
                .findByFromAccountIdOrToAccountId(account.getId(), account.getId(), pageRequest);

        return new PageResponse<>(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    private Account findOwnedAccount(String email, String accountNumber) {
        return accountRepository.findByAccountNumberAndOwnerEmail(accountNumber, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private Account findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getType(),
                t.getStatus(),
                t.getAmount(),
                t.getFromAccount() == null ? null : t.getFromAccount().getAccountNumber(),
                t.getToAccount() == null ? null : t.getToAccount().getAccountNumber(),
                t.getDescription(),
                t.getCreatedAt());
    }
}