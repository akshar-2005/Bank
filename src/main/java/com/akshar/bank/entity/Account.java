package com.akshar.bank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, unique = true)
private String accountNumber;

@Column(nullable = false, precision = 19, scale = 2)
private BigDecimal balance = BigDecimal.ZERO;

@Enumerated(EnumType.STRING)
@Column(nullable = false)
private AccountType type;

@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "owner_id")
private User owner;

@Version
private Long version;

private LocalDateTime createdAt = LocalDateTime.now();
}
