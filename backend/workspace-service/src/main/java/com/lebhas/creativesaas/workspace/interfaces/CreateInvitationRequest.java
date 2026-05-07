package com.lebhas.creativesaas.workspace.interfaces;

import com.lebhas.creativesaas.common.security.Role;
import com.lebhas.creativesaas.common.validation.ValidationMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateInvitationRequest(
        @NotBlank(message = ValidationMessages.REQUIRED)
        @Email
        @Size(max = 160)
        String email,
        @NotNull(message = ValidationMessages.REQUIRED)
        Role role
) {
}
