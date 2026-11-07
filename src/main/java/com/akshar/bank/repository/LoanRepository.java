package com.akshar.bank.repository;

import com.akshar.bank.entity.Loan;
import com.akshar.bank.entity.LoanStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @EntityGraph(attributePaths = { "applicant", "account" })
    List<Loan> findByApplicantEmailOrderByCreatedAtDesc(String email);

    @EntityGraph(attributePaths = { "applicant", "account" })
    List<Loan> findByStatusOrderByCreatedAtAsc(LoanStatus status);
}