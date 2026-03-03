package com.code.bank.codebank.infra.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.code.bank.codebank.application.snippet.domain.Snippet;
import com.code.bank.codebank.application.snippet.port.out.SnippetRepositoryPort;
import com.code.bank.codebank.infra.persistence.entity.SnippetEntity;
import com.code.bank.codebank.infra.persistence.mapper.SnippetPersistenceMapper;
import com.code.bank.codebank.infra.persistence.repository.SpringDataSnippetRepository;

@Component
public class SnippetRepositoryAdapter implements SnippetRepositoryPort {

    private final SpringDataSnippetRepository repository;
    private final SnippetPersistenceMapper mapper;

    public SnippetRepositoryAdapter(SpringDataSnippetRepository repository, SnippetPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Snippet save(Snippet snippet) {
        SnippetEntity entity = mapper.toEntity(snippet);
        SnippetEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Snippet> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }
}
