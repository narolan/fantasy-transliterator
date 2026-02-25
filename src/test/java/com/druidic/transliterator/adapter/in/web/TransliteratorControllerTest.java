package com.druidic.transliterator.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransliteratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getIndexReturnsPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("scripts", "selectedScript"));
    }

    @Test
    void getWithTextParamTransliterates() throws Exception {
        mockMvc.perform(get("/").param("text", "hello").param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("result"))
                .andExpect(model().attribute("inputText", "hello"));
    }

    @Test
    void postTransliterates() throws Exception {
        mockMvc.perform(post("/").param("inputText", "hello").param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("result"));
    }

    @Test
    void invalidScriptFallsBackToFuthark() throws Exception {
        mockMvc.perform(get("/").param("text", "hello").param("script", "INVALID"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedScript",
                        com.druidic.transliterator.core.Script.ELDER_FUTHARK));
    }

    @Test
    void longInputIsTruncated() throws Exception {
        String longInput = "a".repeat(600);
        mockMvc.perform(post("/").param("inputText", longInput).param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("inputText", "a".repeat(500)));
    }

    @Test
    void blankInputReturnsNoResult() throws Exception {
        mockMvc.perform(post("/").param("inputText", "   ").param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("result", org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void tengwarScriptWorks() throws Exception {
        mockMvc.perform(post("/").param("inputText", "hello").param("script", "TENGWAR"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("result"));
    }
}
