package com.code.bank.codebank.http.dto;

import java.time.Instant;
import java.util.UUID;

import com.code.bank.codebank.application.snippet.domain.Snippet;

public record SnippetResponse(
        UUID id,
        String title,
        String code,
        Instant createdAt
) {

    public static SnippetResponse from(Snippet snippet) {
        return new SnippetResponse(
                snippet.id(),
                snippet.title(),
                snippet.code(),
                snippet.createdAt()
        );
    }
}
