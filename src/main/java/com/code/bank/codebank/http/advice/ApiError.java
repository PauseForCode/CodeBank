package com.code.bank.codebank.http.advice;

import java.time.Instant;

public record ApiError(
        String code,
        String message,
        Instant timestamp
) {
}
