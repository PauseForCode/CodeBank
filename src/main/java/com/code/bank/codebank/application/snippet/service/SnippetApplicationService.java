package com.code.bank.codebank.application.snippet.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.code.bank.codebank.application.snippet.domain.Snippet;
import com.code.bank.codebank.application.snippet.port.out.SnippetRepositoryPort;

@Service
public class SnippetApplicationService {

    private final SnippetRepositoryPort snippetRepository;

    public SnippetApplicationService(SnippetRepositoryPort snippetRepository) {
        this.snippetRepository = snippetRepository;
    }

    public Snippet create(String title, String code) {
        Snippet snippet = new Snippet(
                UUID.randomUUID(),
                title.trim(),
                code.trim(),
                Instant.now()
        );

        return snippetRepository.save(snippet);
    }

    public Snippet getById(UUID id) {
        return snippetRepository.findById(id)
                .orElseThrow(() -> new SnippetNotFoundException(id));
    }
}
