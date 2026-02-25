package com.druidic.transliterator.adapter.in.web;

import com.druidic.transliterator.core.Script;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TemplateRenderingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pageTitleRenders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>Druidic Transliterator</title>")));
    }

    @Test
    void formElementsPresent() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"inputText\"")))
                .andExpect(content().string(containsString("id=\"script\"")))
                .andExpect(content().string(containsString("type=\"submit\"")))
                .andExpect(content().string(containsString("id=\"transliterator-form\"")));
    }

    @Test
    void scriptDropdownContainsAllScripts() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String html = result.getResponse().getContentAsString();
        for (Script s : Script.values()) {
            assert html.contains("value=\"" + s.name() + "\"")
                    : "Missing script option: " + s.name();
            assert html.contains(s.getDisplayName())
                    : "Missing display name: " + s.getDisplayName();
        }
    }

    @Test
    void outputRendersWithTransliteratedRunes() throws Exception {
        mockMvc.perform(post("/").param("inputText", "hello").param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"output\"")))
                .andExpect(content().string(containsString("ᚺᛖᛚᛚᛟ")));
    }

    @Test
    void copyAndShareButtonsAppearWithResult() throws Exception {
        mockMvc.perform(post("/").param("inputText", "hello").param("script", "ELDER_FUTHARK"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("copy-btn")))
                .andExpect(content().string(containsString("share-btn")));
    }

    @Test
    void outputSectionAbsentWhenNoResult() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("id=\"output\""))));
    }

    @Test
    void legendSectionRendersWithCorrectEntryCount() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("legend-details")))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int count = countOccurrences(html, "legend-pair");
        assertEquals(23, count, "Futhark legend should have 23 entries");
    }

    @Test
    void footerShowsScriptDescription() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Proto-Germanic runic alphabet")));
    }

    @Test
    void tengwarRendersDistinctOutputAndFooter() throws Exception {
        MvcResult result = mockMvc.perform(post("/").param("inputText", "hello").param("script", "TENGWAR"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tengwar Output")))
                .andExpect(content().string(containsString("Tolkien")))
                .andReturn();

        String html = result.getResponse().getContentAsString();
        int count = countOccurrences(html, "legend-pair");
        assertEquals(22, count, "Tengwar legend should have 22 entries");
    }

    private int countOccurrences(String text, String substring) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(substring, idx)) != -1) {
            count++;
            idx += substring.length();
        }
        return count;
    }
}
