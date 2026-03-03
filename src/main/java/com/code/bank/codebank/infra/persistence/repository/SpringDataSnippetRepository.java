package com.code.bank.codebank.infra.persistence.repository;

import com.code.bank.codebank.infra.persistence.entity.SnippetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataSnippetRepository extends JpaRepository<SnippetEntity, UUID> {
}
