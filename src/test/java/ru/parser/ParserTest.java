package ru.parser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private final Parser parser = new Parser();

    private Tag parse(String html) throws IOException {
        return parser.parse(new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));
    }

    private Tag child(Tag parent, int index) {
        return parent.getChildren().get(index);
    }

    private Tag onlyChild(Tag root) {
        assertEquals(1, root.getChildren().size(), "Expected exactly one child");
        return child(root, 0);
    }

    private void assertAttribute(Tag tag, String key, String expectedValue) {
        assertEquals(expectedValue, tag.getAttributeValue(key), "Unexpected attribute value for " + key);
    }

    @Test
    @DisplayName("Пустой документ -> synthetic root без детей и body")
    void parsesEmptyDocument() throws Exception {
        Tag root = parse("");

        assertNotNull(root);
        assertEquals("", root.getName());
        assertTrue(root.getChildren().isEmpty());
        assertTrue(root.getBody().isEmpty());
    }

    @Test
    @DisplayName("Только текст -> body synthetic root")
    void parsesPlainText() throws Exception {
        Tag root = parse("hello world");

        assertEquals("", root.getName());
        assertEquals(List.of("hello world"), root.getBody());
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Один простой тег")
    void parsesSingleTag() throws Exception {
        Tag root = parse("<div></div>");

        Tag div = onlyChild(root);
        assertEquals("div", div.getName());
        assertSame(root, div.getParent());
        assertTrue(div.getChildren().isEmpty());
        assertTrue(div.getBody().isEmpty());
    }

    @Test
    @DisplayName("Текст внутри тега")
    void parsesTextInsideTag() throws Exception {
        Tag root = parse("<div>hello</div>");

        Tag div = onlyChild(root);
        assertEquals("div", div.getName());
        assertEquals(List.of("hello"), div.getBody());
    }

    @Test
    @DisplayName("Вложенные теги")
    void parsesNestedTags() throws Exception {
        Tag root = parse("<div><span>text</span></div>");

        Tag div = onlyChild(root);
        assertEquals(1, div.getChildren().size());

        Tag span = child(div, 0);
        assertEquals("span", span.getName());
        assertSame(div, span.getParent());
        assertEquals(List.of("text"), span.getBody());
    }

    @Test
    @DisplayName("Несколько соседних дочерних тегов")
    void parsesSiblingTags() throws Exception {
        Tag root = parse("<div><span>one</span><b>two</b></div>");

        Tag div = onlyChild(root);
        assertEquals(2, div.getChildren().size());

        Tag span = child(div, 0);
        Tag b = child(div, 1);

        assertEquals("span", span.getName());
        assertEquals(List.of("one"), span.getBody());

        assertEquals("b", b.getName());
        assertEquals(List.of("two"), b.getBody());
    }

    @Test
    @DisplayName("Смешанный контент: текст до и после вложенного тега")
    void parsesMixedContent() throws Exception {
        Tag root = parse("<div>pre<span>x</span>post</div>");

        Tag div = onlyChild(root);
        Tag span = child(div, 0);

        assertEquals("span", span.getName());
        assertEquals(List.of("x"), span.getBody());

        assertEquals(List.of("pre", "post"), div.getBody());
    }

    @Test
    @DisplayName("Глубокая вложенность")
    void parsesDeepNesting() throws Exception {
        Tag root = parse("<a><b><c>x</c></b></a>");

        Tag a = onlyChild(root);
        Tag b = child(a, 0);
        Tag c = child(b, 0);

        assertEquals("a", a.getName());
        assertEquals("b", b.getName());
        assertEquals("c", c.getName());

        assertSame(root, a.getParent());
        assertSame(a, b.getParent());
        assertSame(b, c.getParent());

        assertEquals(List.of("x"), c.getBody());
    }

    @Test
    @DisplayName("Явный self-closing тег")
    void parsesExplicitSelfClosingTag() throws Exception {
        Tag root = parse("<div><br/></div>");

        Tag div = onlyChild(root);
        assertEquals(1, div.getChildren().size());

        Tag br = child(div, 0);
        assertEquals("br", br.getName());
        assertSame(div, br.getParent());
        assertTrue(br.getChildren().isEmpty());
        assertTrue(br.getBody().isEmpty());
    }

    @Test
    @DisplayName("Void tag без /")
    void parsesVoidTagWithoutSlash() throws Exception {
        Tag root = parse("<div><img></div>");

        Tag div = onlyChild(root);
        assertEquals(1, div.getChildren().size());

        Tag img = child(div, 0);
        assertEquals("img", img.getName());
        assertSame(div, img.getParent());
    }

    @Test
    @DisplayName("Несколько top-level узлов")
    void parsesMultipleTopLevelNodes() throws Exception {
        Tag root = parse("<a></a><b></b>");

        assertEquals(2, root.getChildren().size());
        assertEquals("a", child(root, 0).getName());
        assertEquals("b", child(root, 1).getName());
    }

    @Test
    @DisplayName("Текст между top-level узлами")
    void parsesTextBetweenTopLevelNodes() throws Exception {
        Tag root = parse("<a></a>middle<b></b>");

        assertEquals(2, root.getChildren().size());
        assertEquals(List.of("middle"), root.getBody());
    }

    @Test
    @DisplayName("Quoted attributes копируются в Tag")
    void parsesQuotedAttributes() throws Exception {
        Tag root = parse("<div id=\"main\" class='hero'></div>");

        Tag div = onlyChild(root);
        assertEquals("div", div.getName());

        assertAttribute(div, "id", "main");
        assertAttribute(div, "class", "hero");
        assertEquals(2, div.getAttributes().size());
    }

    @Test
    @DisplayName("Unquoted attribute")
    void parsesUnquotedAttribute() throws Exception {
        Tag root = parse("<div class=hero></div>");

        Tag div = onlyChild(root);
        assertEquals("div", div.getName());
        assertAttribute(div, "class", "hero");
    }

    @Test
    @DisplayName("Boolean attributes")
    void parsesBooleanAttributes() throws Exception {
        Tag root = parse("<input disabled required>");

        Tag input = onlyChild(root);
        assertEquals("input", input.getName());

        assertAttribute(input, "disabled", "");
        assertAttribute(input, "required", "");
        assertEquals(2, input.getAttributes().size());
    }

    @Test
    @DisplayName("Смешанные атрибуты")
    void parsesMixedAttributes() throws Exception {
        Tag root = parse("<img src=\"logo.png\" alt=logo lazy>");

        Tag img = onlyChild(root);
        assertEquals("img", img.getName());

        assertAttribute(img, "src", "logo.png");
        assertAttribute(img, "alt", "logo");
        assertAttribute(img, "lazy", "");
        assertEquals(3, img.getAttributes().size());
    }

    @Test
    @DisplayName("Пустое quoted значение атрибута")
    void parsesEmptyQuotedAttributeValue() throws Exception {
        Tag root = parse("<input value=\"\">");

        Tag input = onlyChild(root);
        assertEquals("input", input.getName());
        assertAttribute(input, "value", "");
    }

    @Test
    @DisplayName("DOCTYPE как отдельный узел")
    void parsesDoctypeNode() throws Exception {
        Tag root = parse("<!DOCTYPE html><html></html>");

        assertEquals(2, root.getChildren().size());

        Tag doctype = child(root, 0);
        Tag html = child(root, 1);

        assertEquals("doctype", doctype.getName());
        assertSame(root, doctype.getParent());
        assertAttribute(doctype, "rawValue", "html");

        assertEquals("html", html.getName());
    }

    @Test
    @DisplayName("Legacy doctype")
    void parsesLegacyDoctype() throws Exception {
        Tag root = parse("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html></html>");

        assertEquals(2, root.getChildren().size());

        Tag doctype = child(root, 0);
        assertEquals("doctype", doctype.getName());
        assertAttribute(
                doctype,
                "rawValue",
                "HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\""
        );
    }

    @Test
    @DisplayName("Комментарий как отдельный узел")
    void parsesCommentNode() throws Exception {
        Tag root = parse("<!--hello-->");

        Tag comment = onlyChild(root);
        assertEquals("comment", comment.getName());
        assertEquals(List.of("hello"), comment.getBody());
        assertTrue(comment.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Комментарий между текстом и тегом")
    void parsesCommentBetweenNodes() throws Exception {
        Tag root = parse("pre<!--x--><div>ok</div>");

        assertEquals(List.of("pre"), root.getBody());
        assertEquals(2, root.getChildren().size());

        Tag comment = child(root, 0);
        Tag div = child(root, 1);

        assertEquals("comment", comment.getName());
        assertEquals(List.of("x"), comment.getBody());

        assertEquals("div", div.getName());
        assertEquals(List.of("ok"), div.getBody());
    }

    @Test
    @DisplayName("Комментарий внутри элемента")
    void parsesCommentInsideElement() throws Exception {
        Tag root = parse("<div><!--x--><span>ok</span></div>");

        Tag div = onlyChild(root);
        assertEquals(2, div.getChildren().size());

        Tag comment = child(div, 0);
        Tag span = child(div, 1);

        assertEquals("comment", comment.getName());
        assertEquals(List.of("x"), comment.getBody());

        assertEquals("span", span.getName());
        assertEquals(List.of("ok"), span.getBody());
    }

    @Test
    @DisplayName("Mismatch closing tag -> ошибка")
    void mismatchedClosingTagThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parse("<div></span>"));
        assertTrue(ex.getMessage().contains("mismatch"));
    }

    @Test
    @DisplayName("Неверный порядок closing tags -> ошибка")
    void wrongClosingOrderThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parse("<div><span></div></span>"));
        assertTrue(ex.getMessage().contains("mismatch"));
    }

    @Test
    @DisplayName("Лишний closing tag -> ошибка")
    void extraClosingTagThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parse("</div>"));
        assertTrue(ex.getMessage().contains("Unexpected closing tag"));
    }

    @Test
    @DisplayName("Незакрытый тег -> ошибка")
    void unclosedTagThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parse("<div><span>text</span>"));
        assertTrue(ex.getMessage().contains("Unclosed"));
    }

    @Test
    @DisplayName("Незакрытый корневой тег -> ошибка")
    void singleUnclosedTagThrows() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> parse("<div>"));
        assertTrue(ex.getMessage().contains("Unclosed"));
    }

    @Test
    @DisplayName("Незакрытый quoted attribute value -> ошибка")
    void brokenQuotedAttributeThrows() {
        assertThrows(Throwable.class, () -> parse("<div class=\"abc></div>"));
    }

    @Test
    @DisplayName("Пустое unquoted attribute value -> ошибка")
    void emptyUnquotedAttributeValueThrows() {
        assertThrows(Throwable.class, () -> parse("<div class=></div>"));
    }

    @Test
    @DisplayName("Незакрытый comment -> ошибка")
    void unclosedCommentThrows() {
        assertThrows(Throwable.class, () -> parse("<!--abc"));
    }

    @Test
    @DisplayName("Незакрытый doctype -> ошибка")
    void unclosedDoctypeThrows() {
        assertThrows(Throwable.class, () -> parse("<!DOCTYPE html"));
    }

    @Test
    @DisplayName("Сложный документ: doctype + comment + attrs + nested tags")
    void parsesComplexDocument() throws Exception {
        Tag root = parse("<!DOCTYPE html>pre<!--c--><div id=\"a\" disabled>txt<img src=x/>mid<span>z</span>post</div>");

        assertEquals(List.of("pre"), root.getBody());
        assertEquals(3, root.getChildren().size());

        Tag doctype = child(root, 0);
        Tag comment = child(root, 1);
        Tag div = child(root, 2);

        assertEquals("doctype", doctype.getName());
        assertAttribute(doctype, "rawValue", "html");

        assertEquals("comment", comment.getName());
        assertEquals(List.of("c"), comment.getBody());

        assertEquals("div", div.getName());
        assertAttribute(div, "id", "a");
        assertAttribute(div, "disabled", "");

        assertEquals(List.of("txt", "mid", "post"), div.getBody());
        assertEquals(2, div.getChildren().size());

        Tag img = child(div, 0);
        Tag span = child(div, 1);

        assertEquals("img", img.getName());
        assertAttribute(img, "src", "x");

        assertEquals("span", span.getName());
        assertEquals(List.of("z"), span.getBody());
    }

    @Test
    @DisplayName("Повторяющийся атрибут хранит последнее значение")
    void duplicateAttributeKeepsLastValue() throws Exception {
        Tag root = parse("<div id=\"a\" id=\"b\"></div>");
        Tag div = onlyChild(root);

        assertAttribute(div, "id", "b");
        assertEquals(1, div.getAttributes().size());
    }

    @Test
    @DisplayName("parent-ссылки корректны для doctype и comment")
    void parentLinksForSpecialNodes() throws Exception {
        Tag root = parse("<!DOCTYPE html><!--x--><div></div>");

        Tag doctype = child(root, 0);
        Tag comment = child(root, 1);
        Tag div = child(root, 2);

        assertSame(root, doctype.getParent());
        assertSame(root, comment.getParent());
        assertSame(root, div.getParent());
    }

}