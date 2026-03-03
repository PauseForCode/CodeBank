package com.code.bank.codebank.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "snippets")
public class SnippetEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected SnippetEntity() {
    }

    public SnippetEntity(UUID id, String title, String code, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.code = code;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
