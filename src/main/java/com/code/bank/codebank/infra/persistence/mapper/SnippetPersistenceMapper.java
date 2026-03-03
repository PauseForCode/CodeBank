package com.code.bank.codebank.infra.persistence.mapper;

import org.mapstruct.Mapper;

import com.code.bank.codebank.application.snippet.domain.Snippet;
import com.code.bank.codebank.infra.persistence.entity.SnippetEntity;

@Mapper(componentModel = "spring")
public interface SnippetPersistenceMapper {

    SnippetEntity toEntity(Snippet snippet);

    Snippet toDomain(SnippetEntity entity);
}
