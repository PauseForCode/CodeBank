package com.code.bank.codebank.application.snippet.domain;

import java.time.Instant;
import java.util.UUID;

public record Snippet(
        UUID id,
        String title,
        String code,
        Instant createdAt
) {
}
