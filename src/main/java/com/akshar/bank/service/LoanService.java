package com.akshar.bank.service;

import com.akshar.bank.dto.LoanApplyRequest;
import com.akshar.bank.dto.LoanResponse;
import com.akshar.bank.entity.Account;
import com.akshar.bank.entity.Loan;
import com.akshar.bank.entity.LoanStatus;
import com.akshar.bank.entity.Transaction;
import com.akshar.bank.entity.TxnStatus;
import com.akshar.bank.entity.TxnType;
import com.akshar.bank.exception.ResourceNotFoundException;
import com.akshar.bank.repository.AccountRepository;
import com.akshar.bank.repository.LoanRepository;
import com.akshar.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public LoanResponse apply(String email, LoanApplyRequest request) {
        Account account = accountRepository
                .findByAccountNumberAndOwnerEmail(request.accountNumber(), email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        Loan loan = new Loan();
        loan.setApplicant(account.getOwner());
        loan.setAccount(account);
        loan.setPrincipal(request.principal());
        loan.setAnnualInterestRate(request.annualInterestRate());
        loan.setTenureMonths(request.tenureMonths());
        loan.setEmi(calculateEmi(request.principal(), request.annualInterestRate(), request.tenureMonths()));
        loan.setStatus(LoanStatus.PENDING);

        return toResponse(loanRepository.save(loan));
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> myLoans(String email) {
        return loanRepository.findByApplicantEmailOrderByCreatedAtDesc(email)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> pendingLoans() {
        return loanRepository.findByStatusOrderByCreatedAtAsc(LoanStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public LoanResponse approve(Long loanId) {
        Loan loan = findPendingLoan(loanId);

        Account account = loan.getAccount();
        account.setBalance(account.getBalance().add(loan.getPrincipal()));
        accountRepository.save(account);

        Transaction txn = new Transaction();
        txn.setToAccount(account);
        txn.setAmount(loan.getPrincipal());
        txn.setType(TxnType.DEPOSIT);
        txn.setStatus(TxnStatus.SUCCESS);
        txn.setDescription("Loan disbursement");
        transactionRepository.save(txn);

        loan.setStatus(LoanStatus.APPROVED);
        loan.setDecidedAt(LocalDateTime.now());
        return toResponse(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse reject(Long loanId) {
        Loan loan = findPendingLoan(loanId);
        loan.setStatus(LoanStatus.REJECTED);
        loan.setDecidedAt(LocalDateTime.now());
        return toResponse(loanRepository.save(loan));
    }

    private Loan findPendingLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalArgumentException("Loan has already been " + loan.getStatus().name().toLowerCase());
        }
        return loan;
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int months) {
        MathContext mc = new MathContext(20, RoundingMode.HALF_UP);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), mc);
        BigDecimal growth = BigDecimal.ONE.add(monthlyRate).pow(months, mc);
        BigDecimal emi = principal.multiply(monthlyRate).multiply(growth)
                .divide(growth.subtract(BigDecimal.ONE), mc);
        return emi.setScale(2, RoundingMode.HALF_UP);
    }

    private LoanResponse toResponse(Loan l) {
        return new LoanResponse(
                l.getId(),
                l.getApplicant().getEmail(),
                l.getAccount().getAccountNumber(),
                l.getPrincipal(),
                l.getAnnualInterestRate(),
                l.getTenureMonths(),
                l.getEmi(),
                l.getStatus(),
                l.getCreatedAt(),
                l.getDecidedAt());
    }
}