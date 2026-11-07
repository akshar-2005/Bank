package com.akshar.bank.dto;

import com.akshar.bank.entity.Role;

public record LoginResponse(
        String token,
        String email,
        Role role) {
}