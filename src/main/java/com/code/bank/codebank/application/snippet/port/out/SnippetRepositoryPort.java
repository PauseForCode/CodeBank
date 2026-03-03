package com.code.bank.codebank.application.snippet.port.out;

import com.code.bank.codebank.application.snippet.domain.Snippet;

import java.util.Optional;
import java.util.UUID;

public interface SnippetRepositoryPort {

    Snippet save(Snippet snippet);

    Optional<Snippet> findById(UUID id);
}
