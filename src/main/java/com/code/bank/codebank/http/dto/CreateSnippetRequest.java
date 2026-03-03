package com.code.bank.codebank.http.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSnippetRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank String code
) {
}
