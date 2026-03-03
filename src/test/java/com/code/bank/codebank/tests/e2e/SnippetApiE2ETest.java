package com.code.bank.codebank.tests.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
class SnippetApiE2ETest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateAndGetSnippet() throws Exception {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        String payload = """
                {
                  "title": "hello-world",
                  "code": "System.out.println(\\\"Hello\\\");"
                }
                """;

        MvcResult created = mockMvc.perform(post("/api/snippets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("hello-world"))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        String id = createdJson.get("id").asText();
        assertThat(id).isNotBlank();

        mockMvc.perform(get("/api/snippets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("hello-world"))
                .andExpect(jsonPath("$.code").value("System.out.println(\"Hello\");"));
    }
}
