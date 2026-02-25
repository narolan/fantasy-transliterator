package com.druidic.transliterator.adapter.in.web;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class EndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    private WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = MockMvcWebClientBuilder.mockMvcSetup(mockMvc).build();
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
    }

    @Test
    void loadPageAndVerifyStructure() throws Exception {
        HtmlPage page = webClient.getPage("http://localhost/");

        assertEquals("Druidic Transliterator", page.getTitleText());
        assertNotNull(page.getHtmlElementById("transliterator-form"));
        assertNotNull(page.getHtmlElementById("inputText"));
        assertNotNull(page.getHtmlElementById("script"));
        assertFalse(page.getByXPath("//details[contains(@class,'legend-details')]").isEmpty());
    }

    @Test
    void submitFormAndVerifyOutput() throws Exception {
        HtmlPage page = webClient.getPage("http://localhost/");

        HtmlTextArea textarea = page.getHtmlElementById("inputText");
        textarea.setText("hello");

        HtmlElement submitButton = page.querySelector(".translate-btn");
        HtmlPage resultPage = submitButton.click();

        assertNotNull(resultPage.getHtmlElementById("output"));
        assertTrue(resultPage.asNormalizedText().contains("\u16BA\u16D6\u16DA\u16DA\u16DF"));
    }

    @Test
    void switchScriptsProducesDifferentOutput() throws Exception {
        // Futhark
        HtmlPage futharkPage = webClient.getPage("http://localhost/");
        HtmlTextArea ta1 = futharkPage.getHtmlElementById("inputText");
        ta1.setText("hello");
        HtmlPage futharkResult = ((HtmlElement) futharkPage.querySelector(".translate-btn")).click();
        String futharkOutput = futharkResult.getHtmlElementById("output").asNormalizedText();

        // Tengwar
        HtmlPage tengwarPage = webClient.getPage("http://localhost/");
        HtmlTextArea ta2 = tengwarPage.getHtmlElementById("inputText");
        ta2.setText("hello");
        HtmlSelect select = tengwarPage.getHtmlElementById("script");
        HtmlOption tengwarOption = select.getOptionByValue("TENGWAR");
        select.setSelectedAttribute(tengwarOption, true);
        HtmlPage tengwarResult = ((HtmlElement) tengwarPage.querySelector(".translate-btn")).click();
        String tengwarOutput = tengwarResult.getHtmlElementById("output").asNormalizedText();

        assertNotEquals(futharkOutput, tengwarOutput, "Futhark and Tengwar should produce different output");
    }

    @Test
    void shareUrlPrefillsInputAndRendersOutput() throws Exception {
        HtmlPage page = webClient.getPage("http://localhost/?text=world&script=ELDER_FUTHARK");

        HtmlTextArea textarea = page.getHtmlElementById("inputText");
        assertEquals("world", textarea.getText());
        assertNotNull(page.getHtmlElementById("output"));
    }

    @Test
    void footerUpdatesPerScript() throws Exception {
        HtmlPage futharkPage = webClient.getPage("http://localhost/");
        String futharkFooter = futharkPage.querySelector(".site-footer").asNormalizedText();
        assertTrue(futharkFooter.contains("Proto-Germanic"), "Futhark footer should mention Proto-Germanic");

        HtmlPage tengwarPage = webClient.getPage("http://localhost/?text=a&script=TENGWAR");
        String tengwarFooter = tengwarPage.querySelector(".site-footer").asNormalizedText();
        assertTrue(tengwarFooter.contains("Tolkien"), "Tengwar footer should mention Tolkien");
    }
}
