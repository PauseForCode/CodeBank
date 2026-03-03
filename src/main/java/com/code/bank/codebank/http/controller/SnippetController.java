package com.code.bank.codebank.http.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.code.bank.codebank.application.snippet.domain.Snippet;
import com.code.bank.codebank.application.snippet.service.SnippetApplicationService;
import com.code.bank.codebank.http.dto.CreateSnippetRequest;
import com.code.bank.codebank.http.dto.SnippetResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/snippets")
public class SnippetController {

    private final SnippetApplicationService snippetApplicationService;

    public SnippetController(SnippetApplicationService snippetApplicationService) {
        this.snippetApplicationService = snippetApplicationService;
    }

    @PostMapping
    public ResponseEntity<SnippetResponse> create(@Valid @RequestBody CreateSnippetRequest request) {
        Snippet snippet = snippetApplicationService.create(request.title(), request.code());
        return ResponseEntity
                .created(URI.create("/api/snippets/" + snippet.id()))
                .body(SnippetResponse.from(snippet));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SnippetResponse> getById(@PathVariable UUID id) {
        Snippet snippet = snippetApplicationService.getById(id);
        return ResponseEntity.ok(SnippetResponse.from(snippet));
    }
}
