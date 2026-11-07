package com.akshar.bank.service;

import com.akshar.bank.dto.TransactionResponse;
import com.akshar.bank.dto.TransferRequest;
import com.akshar.bank.entity.Account;
import com.akshar.bank.entity.Transaction;
import com.akshar.bank.entity.TxnStatus;
import com.akshar.bank.entity.TxnType;
import com.akshar.bank.exception.ResourceNotFoundException;
import com.akshar.bank.repository.AccountRepository;
import com.akshar.bank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account from;
    private Account to;

    @BeforeEach
    void setUp() {
        from = account(1L, "1111111111", "1000.00");
        to = account(2L, "2222222222", "500.00");
    }

    private Account account(Long id, String number, String balance) {
        Account a = new Account();
        a.setId(id);
        a.setAccountNumber(number);
        a.setBalance(new BigDecimal(balance));
        return a;
    }

    @Test
    void transfer_movesMoneyAndRecordsTransaction() {
        when(accountRepository.findByAccountNumberAndOwnerEmail("1111111111", "a@mail.com"))
                .thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("2222222222"))
                .thenReturn(Optional.of(to));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.transfer(
                "a@mail.com",
                new TransferRequest("1111111111", "2222222222", new BigDecimal("200.00")));

        assertEquals(0, new BigDecimal("800.00").compareTo(from.getBalance()));
        assertEquals(0, new BigDecimal("700.00").compareTo(to.getBalance()));
        assertEquals(TxnType.TRANSFER, response.type());
        assertEquals(TxnStatus.SUCCESS, response.status());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_withInsufficientBalance_throwsAndChangesNothing() {
        when(accountRepository.findByAccountNumberAndOwnerEmail("1111111111", "a@mail.com"))
                .thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("2222222222"))
                .thenReturn(Optional.of(to));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> transactionService.transfer(
                "a@mail.com",
                new TransferRequest("1111111111", "2222222222", new BigDecimal("5000.00"))));

        assertEquals("Insufficient balance", ex.getMessage());
        assertEquals(0, new BigDecimal("1000.00").compareTo(from.getBalance()));
        assertEquals(0, new BigDecimal("500.00").compareTo(to.getBalance()));
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_toSameAccount_throws() {
        assertThrows(IllegalArgumentException.class, () -> transactionService.transfer(
                "a@mail.com",
                new TransferRequest("1111111111", "1111111111", new BigDecimal("10.00"))));

        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void transfer_fromAccountNotOwned_throwsNotFound() {
        when(accountRepository.findByAccountNumberAndOwnerEmail("1111111111", "other@mail.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.transfer(
                "other@mail.com",
                new TransferRequest("1111111111", "2222222222", new BigDecimal("10.00"))));

        verify(accountRepository, never()).save(any());
    }
}