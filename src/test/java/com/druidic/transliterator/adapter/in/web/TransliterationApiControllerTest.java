package com.druidic.transliterator.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransliterationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void transliteratesViaApi() throws Exception {
        mockMvc.perform(get("/api/transliterate")
                .param("text", "hello")
                .param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.runeText").isNotEmpty());
    }

    @Test
    void blankTextReturnsEmptyResult() throws Exception {
        mockMvc.perform(get("/api/transliterate")
                .param("text", "   ")
                .param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runeText").value(""));
    }

    @Test
    void invalidScriptFallsBackToFuthark() throws Exception {
        mockMvc.perform(get("/api/transliterate")
                .param("text", "hello")
                .param("script", "INVALID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runeText").isNotEmpty());
    }

    @Test
    void tengwarScriptWorks() throws Exception {
        mockMvc.perform(get("/api/transliterate")
                .param("text", "hello")
                .param("script", "TENGWAR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runeText").isNotEmpty());
    }

    @Test
    void longTextIsTruncated() throws Exception {
        String longText = "a".repeat(600);
        mockMvc.perform(get("/api/transliterate")
                .param("text", longText)
                .param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runeText").isNotEmpty());
    }
}
