package com.akshar.bank.dto;

import com.akshar.bank.entity.Role;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role) {
}
