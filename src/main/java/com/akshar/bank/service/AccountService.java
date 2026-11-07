package com.akshar.bank.service;

import com.akshar.bank.dto.AccountResponse;
import com.akshar.bank.dto.OpenAccountRequest;
import com.akshar.bank.entity.Account;
import com.akshar.bank.entity.User;
import com.akshar.bank.repository.AccountRepository;
import com.akshar.bank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.akshar.bank.exception.ResourceNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public AccountResponse openAccount(String email, OpenAccountRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setType(request.type());
        account.setBalance(request.initialDeposit());
        account.setOwner(user);

        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String email, String accountNumber) {
        Account account = accountRepository.findByAccountNumberAndOwnerEmail(accountNumber, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> myAccounts(String email) {
        return accountRepository.findByOwnerEmailOrderByIdAsc(email)
                .stream().map(this::toResponse).toList();
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = String.valueOf(1_000_000_000L + (long) (Math.random() * 9_000_000_000L));
        } while (accountRepository.findByAccountNumber(number).isPresent());
        return number;
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(a.getId(), a.getAccountNumber(), a.getBalance(), a.getType());
    }
}
