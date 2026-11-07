package com.akshar.bank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "from_account_id")
private Account fromAccount;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "to_account_id")
private Account toAccount;

@Column(nullable = false, precision = 19, scale = 2)
private BigDecimal amount;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private TxnType type;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private TxnStatus status;

private String description;

private LocalDateTime createdAt = LocalDateTime.now();
}