package ru.parser.tokenizer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.parser.tokenizer.token.CommentToken;
import ru.parser.tokenizer.token.DoctypeToken;
import ru.parser.tokenizer.token.EndTagToken;
import ru.parser.tokenizer.token.HtmlToken;
import ru.parser.tokenizer.token.StartTagToken;
import ru.parser.tokenizer.token.TextToken;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerImplTest {

    private TokenizerImpl tokenizer(String html) throws IOException {
        return new TokenizerImpl(
                new LexemeAnalyzerImpl(
                        new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8))
                )
        );
    }

    private HtmlToken next(TokenizerImpl tokenizer) {
        return tokenizer.getNextToken();
    }

    private TextToken assertTextToken(HtmlToken token, String expectedText) {
        assertNotNull(token, "Expected token, got null");
        assertInstanceOf(TextToken.class, token);
        TextToken textToken = (TextToken) token;

        // Подправь имя геттера, если у тебя не getText()
        assertEquals(expectedText, textToken.text());
        return textToken;
    }

    private CommentToken assertCommentToken(HtmlToken token, String expectedText) {
        assertNotNull(token, "Expected token, got null");
        assertInstanceOf(CommentToken.class, token);
        CommentToken commentToken = (CommentToken) token;

        // Подправь имя геттера, если у тебя не getText()
        assertEquals(expectedText, commentToken.text());
        return commentToken;
    }

    private StartTagToken assertStartTag(
            HtmlToken token,
            String expectedName,
            Map<String, String> expectedAttrs,
            boolean expectedSelfClosing
    ) {
        assertNotNull(token, "Expected token, got null");
        assertInstanceOf(StartTagToken.class, token);
        StartTagToken startTag = (StartTagToken) token;

        // Подправь имена геттеров, если у тебя другие
        assertEquals(expectedName, startTag.name());
        assertEquals(expectedAttrs, startTag.attrs());
        assertEquals(expectedSelfClosing, startTag.selfClosing());

        return startTag;
    }

    private EndTagToken assertEndTag(HtmlToken token, String expectedName) {
        assertNotNull(token, "Expected token, got null");
        assertInstanceOf(EndTagToken.class, token);
        EndTagToken endTag = (EndTagToken) token;

        // Подправь имя геттера, если у тебя другое
        assertEquals(expectedName, endTag.name());
        return endTag;
    }

    @Test
    @DisplayName("Пустой ввод -> null")
    void emptyInputReturnsNull() throws Exception {
        TokenizerImpl tokenizer = tokenizer("");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Обычный текст без тегов -> один TextToken")
    void plainTextProducesTextToken() throws Exception {
        TokenizerImpl tokenizer = tokenizer("hello world");
        assertTextToken(next(tokenizer), "hello world");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Текст до и после тега разбивается на отдельные токены")
    void textAroundTag() throws Exception {
        TokenizerImpl tokenizer = tokenizer("hello<div>world</div>tail");

        assertTextToken(next(tokenizer), "hello");
        assertStartTag(next(tokenizer), "div", Map.of(), false);
        assertTextToken(next(tokenizer), "world");
        assertEndTag(next(tokenizer), "div");
        assertTextToken(next(tokenizer), "tail");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит простой открывающий тег без атрибутов")
    void parsesSimpleStartTag() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<div>");
        assertStartTag(next(tokenizer), "div", Map.of(), false);
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит простой закрывающий тег")
    void parsesSimpleEndTag() throws Exception {
        TokenizerImpl tokenizer = tokenizer("</div>");
        assertEndTag(next(tokenizer), "div");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит self-closing тег через /")
    void parsesExplicitSelfClosingTag() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<br/>");
        assertStartTag(next(tokenizer), "br", Map.of(), true);
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит void tag без / как selfClosing=true")
    void parsesVoidTagWithoutSlashAsSelfClosing() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<img>");
        assertStartTag(next(tokenizer), "img", Map.of(), true);
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит start + text + end")
    void parsesSimpleElementSequence() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<div>abc</div>");

        assertStartTag(next(tokenizer), "div", Map.of(), false);
        assertTextToken(next(tokenizer), "abc");
        assertEndTag(next(tokenizer), "div");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит один boolean attribute")
    void parsesBooleanAttribute() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<input disabled>");
        assertStartTag(next(tokenizer), "input", Map.of("disabled", ""), true);
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит несколько boolean attributes")
    void parsesMultipleBooleanAttributes() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<input disabled required>");
        assertStartTag(
                next(tokenizer),
                "input",
                Map.of("disabled", "", "required", ""),
                true
        );
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит один quoted attribute с двойными кавычками")
    void parsesDoubleQuotedAttribute() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<div class=\"main\">");
        assertStartTag(next(tokenizer), "div", Map.of("class", "main"), false);
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит один quoted attribute с одинарными кавычками")
    void parsesSingleQuotedAttribute() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<div class='main'>");
        assertStartTag(next(tokenizer), "div", Map.of("class", "main"), false);
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит пустое quoted value")
    void parsesEmptyQuotedAttributeValue() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<input value=\"\">");
        assertStartTag(next(tokenizer), "input", Map.of("value", ""), true);
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит unquoted attribute value")
    void parsesUnquotedAttributeValue() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<div class=main>");
        assertStartTag(next(tokenizer), "div", Map.of("class", "main"), false);
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит несколько разных атрибутов")
    void parsesMultipleMixedAttributes() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<img src=\"logo.png\" alt=logo lazy>");
        assertStartTag(
                next(tokenizer),
                "img",
                Map.of(
                        "src", "logo.png",
                        "alt", "logo",
                        "lazy", ""
                ),
                true
        );
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит self-closing тег с атрибутами")
    void parsesSelfClosingTagWithAttributes() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<img src=\"x\" alt=\"y\"/>");
        assertStartTag(
                next(tokenizer),
                "img",
                Map.of("src", "x", "alt", "y"),
                true
        );
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит unquoted value перед />")
    void parsesUnquotedAttributeBeforeSelfClosing() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<img src=test/>");
        assertStartTag(next(tokenizer), "img", Map.of("src", "test"), true);
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит текст с пробелами как один TextToken")
    void keepsWhitespaceInText() throws Exception {
        TokenizerImpl tokenizer = tokenizer("hello   world");
        assertTextToken(next(tokenizer), "hello   world");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Игнорирует комментарий как отдельный CommentToken")
    void parsesCommentToken() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!--hello-->");
        assertCommentToken(next(tokenizer), "hello");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит комментарий с одним дефисом внутри")
    void parsesCommentWithSingleDashInside() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!--a-b-->");
        assertCommentToken(next(tokenizer), "a-b");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит комментарий с двойным дефисом внутри")
    void parsesCommentWithDoubleDashInside() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!--a--b-->");
        assertCommentToken(next(tokenizer), "a--b");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит комментарий между текстом и тегами")
    void parsesCommentBetweenTokens() throws Exception {
        TokenizerImpl tokenizer = tokenizer("hi<!--x--><div>ok</div>");

        assertTextToken(next(tokenizer), "hi");
        assertCommentToken(next(tokenizer), "x");
        assertStartTag(next(tokenizer), "div", Map.of(), false);
        assertTextToken(next(tokenizer), "ok");
        assertEndTag(next(tokenizer), "div");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("EOF после < даёт ошибку")
    void eofAfterOpenBracketThrows() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<");
        assertThrows(Throwable.class, tokenizer::getNextToken);
    }

    @Test
    @DisplayName("EOF внутри closing tag даёт ошибку")
    void eofInsideClosingTagThrows() throws Exception {
        TokenizerImpl tokenizer = tokenizer("</div");
        assertThrows(Throwable.class, tokenizer::getNextToken);
    }

    @Test
    @DisplayName("EOF внутри quoted attribute value даёт ошибку")
    void eofInsideQuotedAttributeThrows() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<div class=\"abc>");
        assertThrows(Throwable.class, tokenizer::getNextToken);
    }

    @Test
    @DisplayName("EOF внутри comment даёт ошибку")
    void eofInsideCommentThrows() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!--abc");
        assertThrows(Throwable.class, tokenizer::getNextToken);
    }

    @Test
    @DisplayName("После = без значения tokenizer должен падать")
    void missingAttributeValueThrows() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<div class=>");
        assertThrows(Throwable.class, tokenizer::getNextToken);
    }

    @Test
    @DisplayName("После = с пробелом tokenizer должен падать")
    void missingAttributeValueThrows2() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<div class= >");
        assertThrows(Throwable.class, tokenizer::getNextToken);
    }

    @Test
    @DisplayName("Не разрешает self-closing у closing tag")
    void invalidClosingTagWithSlashThrows() throws Exception {
        TokenizerImpl tokenizer = tokenizer("</div/>");
        assertThrows(Throwable.class, tokenizer::getNextToken);
    }

    @Test
    @DisplayName("Парсит сложный пример с разными токенами")
    void parsesComplexSequence() throws Exception {
        TokenizerImpl tokenizer = tokenizer(
                "pre<div id=\"a\" disabled>txt<img src=x/>tail</div>post"
        );

        assertTextToken(next(tokenizer), "pre");
        assertStartTag(next(tokenizer), "div", Map.of("id", "a", "disabled", ""), false);
        assertTextToken(next(tokenizer), "txt");
        assertStartTag(next(tokenizer), "img", Map.of("src", "x"), true);
        assertTextToken(next(tokenizer), "tail");
        assertEndTag(next(tokenizer), "div");
        assertTextToken(next(tokenizer), "post");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит простой DOCTYPE")
    void parsesSimpleDoctype() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!DOCTYPE html>");

        HtmlToken token = next(tokenizer);
        assertNotNull(token);
        assertInstanceOf(DoctypeToken.class, token);

        DoctypeToken doctype = (DoctypeToken) token;
        // Подправь имя геттера, если у тебя не getValue()
        assertEquals("html", doctype.rawValue());

        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит lowercase doctype")
    void parsesLowercaseDoctype() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!doctype html>");

        HtmlToken token = next(tokenizer);
        assertNotNull(token);
        assertInstanceOf(DoctypeToken.class, token);

        DoctypeToken doctype = (DoctypeToken) token;
        assertEquals("html", doctype.rawValue());

        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит doctype с лишними пробелами")
    void parsesDoctypeWithExtraWhitespace() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!DOCTYPE    html   >");

        HtmlToken token = next(tokenizer);
        assertNotNull(token);
        assertInstanceOf(DoctypeToken.class, token);

        DoctypeToken doctype = (DoctypeToken) token;
        assertEquals("html", doctype.rawValue());

        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит legacy doctype с PUBLIC")
    void parsesLegacyPublicDoctype() throws Exception {
        TokenizerImpl tokenizer = tokenizer(
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" " +
                        "\"http://www.w3.org/TR/html4/strict.dtd\">"
        );

        HtmlToken token = next(tokenizer);
        assertNotNull(token);
        assertInstanceOf(DoctypeToken.class, token);

        DoctypeToken doctype = (DoctypeToken) token;
        assertEquals(
                "HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"",
                doctype.rawValue()
        );

        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Парсит doctype между текстом и тегом")
    void parsesDoctypeBetweenOtherTokens() throws Exception {
        TokenizerImpl tokenizer = tokenizer("pre<!DOCTYPE html><div>x</div>");

        assertTextToken(next(tokenizer), "pre");

        HtmlToken doctypeToken = next(tokenizer);
        assertNotNull(doctypeToken);
        assertInstanceOf(DoctypeToken.class, doctypeToken);
        DoctypeToken doctype = (DoctypeToken) doctypeToken;
        assertEquals("html", doctype.rawValue());

        assertStartTag(next(tokenizer), "div", Map.of(), false);
        assertTextToken(next(tokenizer), "x");
        assertEndTag(next(tokenizer), "div");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("После DOCTYPE tokenizer возвращается в DATA")
    void tokenizerReturnsToDataAfterDoctype() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!DOCTYPE html>text");

        HtmlToken token = next(tokenizer);
        assertNotNull(token);
        assertInstanceOf(DoctypeToken.class, token);

        DoctypeToken doctype = (DoctypeToken) token;
        assertEquals("html", doctype.rawValue());

        assertTextToken(next(tokenizer), "text");
        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("Неверное слово после <! не считается doctype")
    void invalidDoctypeKeywordThrows() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!DOCX html>");
        assertThrows(Throwable.class, tokenizer::getNextToken);
    }

    @Test
    @DisplayName("Незакрытый doctype даёт ошибку")
    void unclosedDoctypeThrows() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!DOCTYPE html");
        assertThrows(Throwable.class, tokenizer::getNextToken);
    }

    @Test
    @DisplayName("DOCTYPE без payload тоже парсится")
    void parsesDoctypeWithoutPayload() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!DOCTYPE >");

        HtmlToken token = next(tokenizer);
        assertNotNull(token);
        assertInstanceOf(DoctypeToken.class, token);

        DoctypeToken doctype = (DoctypeToken) token;
        assertEquals("", doctype.rawValue());

        assertNull(next(tokenizer));
    }

    @Test
    @DisplayName("DOCTYPE с несколькими словами в payload")
    void parsesDoctypeWithMultipleWords() throws Exception {
        TokenizerImpl tokenizer = tokenizer("<!DOCTYPE html SYSTEM \"about:legacy-compat\">");

        HtmlToken token = next(tokenizer);
        assertNotNull(token);
        assertInstanceOf(DoctypeToken.class, token);

        DoctypeToken doctype = (DoctypeToken) token;
        assertEquals("html SYSTEM \"about:legacy-compat\"", doctype.rawValue());

        assertNull(next(tokenizer));
    }
}