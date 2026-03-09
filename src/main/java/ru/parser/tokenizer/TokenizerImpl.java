package ru.parser.tokenizer;

import ru.parser.tokenizer.token.CommentToken;
import ru.parser.tokenizer.token.DoctypeToken;
import ru.parser.tokenizer.token.EndTagToken;
import ru.parser.tokenizer.token.HtmlToken;
import ru.parser.tokenizer.token.StartTagToken;
import ru.parser.tokenizer.token.TextToken;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static ru.parser.Tag.VOID_TAGS;
import static ru.parser.tokenizer.LexemeType.CHARACTER;
import static ru.parser.tokenizer.LexemeType.CLOSE_BRACKET;
import static ru.parser.tokenizer.LexemeType.DASH;
import static ru.parser.tokenizer.TokenizerState.ATTR_VALUE_QUOTED;
import static ru.parser.tokenizer.TokenizerState.ATTR_VALUE_UNQUOTED;
import static ru.parser.tokenizer.TokenizerState.BEFORE_ATTR_NAME;
import static ru.parser.tokenizer.TokenizerState.BEFORE_ATTR_VALUE;
import static ru.parser.tokenizer.TokenizerState.COMMENT;
import static ru.parser.tokenizer.TokenizerState.DATA;
import static ru.parser.tokenizer.TokenizerState.TAG_NAME;
import static ru.parser.tokenizer.TokenizerState.TAG_OPEN;

public class TokenizerImpl implements Tokenizer {

    private final List<Character> doctypeCharacters = List.of('d', 'o', 'c', 't', 'y', 'p', 'e');

    private TokenizerState state;

    private final LexemeAnalyzer analyzer;

    private Deque<Lexeme> lexemes = new ArrayDeque<>();

    private boolean closingTag;

    private String currentTagName;

    private Map<String, String> currentAttrs = new HashMap<>();

    private String currentAttr;


    public TokenizerImpl(LexemeAnalyzer analyzer) throws IOException {
        this.analyzer = analyzer;
        this.state = DATA;
    }


    public HtmlToken getNextToken() {
        Lexeme lexeme = getNextLexeme();

        if (lexeme == null) {
            return null;
        }

        return switch (state) {
            case DATA -> data(lexeme);
            case TAG_OPEN -> tagOpen(lexeme);
            case TAG_NAME -> tagName(lexeme);
            case BEFORE_ATTR_NAME -> beforeAttrName(lexeme);
            case BEFORE_ATTR_VALUE -> beforeAttrValue(lexeme);
            case ATTR_VALUE_QUOTED -> attrValueQuoted(lexeme);
            case ATTR_VALUE_UNQUOTED -> attrValueUnquoted(lexeme);
            case COMMENT -> comment(lexeme);
            default ->  throw new RuntimeException("Unexpected lexeme: " + lexeme.getType());
        };
    }

    protected HtmlToken comment(Lexeme lexeme) {
        assertNotNullLexeme(lexeme);

        if (lexeme.isCharacter()) {
            return doctype(lexeme);
        }

        assertLexeme(lexeme, DASH);
        assertLexeme(requireLexeme(), DASH);

        lexeme = requireLexeme();
        StringBuilder text = new StringBuilder();
        while (true) {
            while (!lexeme.isDash()) {
                text.append(lexeme.getValue());
                lexeme = requireLexeme();
            }

            lexeme = requireLexeme();

            if (!lexeme.isDash()) {
                text.append("-").append(lexeme.getValue());
                lexeme = requireLexeme();
                continue;
            }

            lexeme = requireLexeme();

            if (!lexeme.isCloseBracket()) {
                text.append("--").append(lexeme.getValue());
                lexeme = requireLexeme();
                continue;
            }

            changeState(DATA);
            return new CommentToken(text.toString());
        }
    }

    protected HtmlToken doctype(Lexeme lexeme) {
        if (!lexeme.isCharacter()) {
            throw new RuntimeException("Unexpected lexeme: " + lexeme.getType());
        }

        storeLexeme(lexeme);

        for (int i = 0; i < doctypeCharacters.size(); i++) {
            lexeme = requireLexeme();
            assertLexeme(lexeme, CHARACTER);

            Character doctypeCharacter = doctypeCharacters.get(i);
            Character lexemeCharacter = Character.toLowerCase(lexeme.getValue());

            if (!doctypeCharacter.equals(lexemeCharacter)) {
                throw new RuntimeException("Unexpected lexeme: " + lexeme.getType());
            }
        }

        lexeme = skipWsAndDelimiters(requireLexeme());
        assertNotNullLexeme(lexeme);

        StringBuilder text = new StringBuilder();
        while (!lexeme.isCloseBracket()) {
            text.append(lexeme.getValue());
            lexeme = requireLexeme();
        }

        changeState(DATA);
        return new DoctypeToken(text.toString().trim());
    }

    protected HtmlToken attrValueUnquoted(Lexeme lexeme) {
        if (lexeme.isWhitespace()) {
            throw new RuntimeException("lexeme is whitespace");
        }

        StringBuilder text = new StringBuilder();
        while (!lexeme.isWhitespace() && !lexeme.isCloseBracket() && !lexeme.isSlash()) {
            text.append(lexeme.getValue());
            lexeme = requireLexeme();
        }

        if (text.isEmpty()) {
            throw new RuntimeException("attr value is empty");
        }

        currentAttrs.put(currentAttr, text.toString());
        changeState(BEFORE_ATTR_NAME);
        return beforeAttrName(lexeme);
    }

    protected HtmlToken attrValueQuoted(Lexeme lexeme) {
        if (!lexeme.isQuote()) {
            throw new RuntimeException("unexpected lexeme: " + lexeme.getType());
        }

        LexemeType quoteType = lexeme.getType();
        StringBuilder text = new StringBuilder();
        Lexeme currentLexeme = requireLexeme();
        while (currentLexeme.getType() != quoteType) {
            text.append(currentLexeme.getValue());
            currentLexeme = requireLexeme();
        }
        currentAttrs.put(currentAttr, text.toString());
        changeState(BEFORE_ATTR_NAME);
        return beforeAttrName(requireLexeme());
    }

    protected HtmlToken beforeAttrValue(Lexeme lexeme) {
        lexeme = skipWsAndDelimiters(lexeme);
        assertNotNullLexeme(lexeme);

        if (lexeme.isDoubleQuote() || lexeme.isSingleQuote()) {
            changeState(ATTR_VALUE_QUOTED);
            return attrValueQuoted(lexeme);
        }

        changeState(ATTR_VALUE_UNQUOTED);
        return attrValueUnquoted(lexeme);
    }

    protected HtmlToken beforeAttrName(Lexeme lexeme) {
        lexeme = skipWsAndDelimiters(lexeme);
        assertNotNullLexeme(lexeme);

        if (lexeme.isCloseBracket()) {
            changeState(DATA);
            return new StartTagToken(currentTagName, new HashMap<>(currentAttrs),  isNonCloseableTag(currentTagName));
        }

        if (lexeme.isSlash()) {
            lexeme = skipWsAndDelimiters(requireLexeme());
            assertLexeme(lexeme, CLOSE_BRACKET);
            changeState(DATA);
            return new StartTagToken(currentTagName, new HashMap<>(currentAttrs), true);
        }

        assertLexeme(lexeme, CHARACTER);

        StringBuilder name = new StringBuilder();
        while (lexeme.isCharacter()) {
            name.append(lexeme.getValue());
            lexeme = requireLexeme();
        }

        currentAttr = name.toString();

        lexeme = skipWsAndDelimiters(lexeme);
        assertNotNullLexeme(lexeme);

        if (lexeme.isEqual()) {
            changeState(BEFORE_ATTR_VALUE);
            return beforeAttrValue(requireLexeme());
        }

        currentAttrs.put(currentAttr, "");

        if (lexeme.isCloseBracket()) {
            changeState(DATA);
            return new StartTagToken(currentTagName, new HashMap<>(currentAttrs), isNonCloseableTag(currentTagName));
        }

        if (lexeme.isSlash()) {
            lexeme = skipWsAndDelimiters(requireLexeme());
            assertLexeme(lexeme, CLOSE_BRACKET);
            changeState(DATA);
            return new StartTagToken(currentTagName, new HashMap<>(currentAttrs), true);
        }

        if (lexeme.isCharacter()) {
            changeState(BEFORE_ATTR_NAME);
            return beforeAttrName(lexeme);
        }

        throw new RuntimeException("Unexpected lexeme: " + lexeme.getType());
    }

    protected HtmlToken tagName(Lexeme lexeme) {
        lexeme = skipWsAndDelimiters(lexeme);
        assertLexeme(lexeme, CHARACTER);

        StringBuilder name = new StringBuilder();
        while (lexeme.isCharacter()) {
            name.append(lexeme.getValue());
            lexeme = requireLexeme();
        }

        currentTagName = name.toString();

        lexeme = skipWsAndDelimiters(lexeme);
        assertNotNullLexeme(lexeme);

        if (closingTag) {
            assertLexeme(lexeme, CLOSE_BRACKET);
            changeState(DATA);
            return new EndTagToken(currentTagName);
        }

        currentAttrs.clear();

        if (lexeme.isCloseBracket()) {
            changeState(DATA);
            return new StartTagToken(currentTagName, new HashMap<>(), isNonCloseableTag(currentTagName));
        }

        if (lexeme.isCharacter()) {
            changeState(BEFORE_ATTR_NAME);
            return beforeAttrName(lexeme);
        }

        if (lexeme.isSlash()) {
            lexeme = skipWsAndDelimiters(requireLexeme());
            assertLexeme(lexeme, CLOSE_BRACKET);
            changeState(DATA);
            return new StartTagToken(currentTagName, new HashMap<>(), true);
        }

        throw new RuntimeException("Unexpected lexeme: " + lexeme.getType());
    }

    protected HtmlToken tagOpen(Lexeme lexeme) {
        assertNotNullLexeme(lexeme);
        lexeme = skipWsAndDelimiters(lexeme);
        assertNotNullLexeme(lexeme);

        if (lexeme.isSlash()) {
            changeState(TAG_NAME);
            closingTag = true;
            return tagName(requireLexeme());
        }

        if (lexeme.isCharacter()) {
            changeState(TAG_NAME);
            closingTag = false;
            return tagName(lexeme);
        }

        if (lexeme.isExclamationMark()) {
            changeState(COMMENT);
            return comment(requireLexeme());
        }

        throw new RuntimeException("Unexpected lexeme: " + lexeme.getType());
    }

    protected HtmlToken data(Lexeme lexeme) {
        assertNotNullLexeme(lexeme);

        if (lexeme.isOpenBracket()) {
            changeState(TAG_OPEN);
            return tagOpen(requireLexeme());
        }

        StringBuilder text = new StringBuilder();
        do {
            text.append(lexeme.getValue());
            lexeme = getNextLexeme();
        } while (lexeme != null && !lexeme.isOpenBracket());

        storeLexeme(lexeme);
        return new TextToken(text.toString());
    }

    protected void assertNotNullLexeme(Lexeme lexeme) {
        if (lexeme == null)
            throw new NullPointerException("lexeme is null");
    }

    protected void assertLexeme(Lexeme lexeme, LexemeType type) {
        assertNotNullLexeme(lexeme);
        if (lexeme.getType() != type)
            throw new RuntimeException(lexeme.getType() + " != " + type);
    }

    protected Lexeme skipWsAndDelimiters(Lexeme lexeme) {
        while (lexeme != null && (lexeme.isDelimiter() || lexeme.isWhitespace())) {
            lexeme = getNextLexeme();
        }
        return lexeme;
    }

    protected void storeLexeme(Lexeme lexeme) {
        if (lexeme != null) {
            this.lexemes.addFirst(lexeme);
        }
    }

    protected Lexeme requireLexeme() {
        Lexeme nextLexeme = getNextLexeme();
        assertNotNullLexeme(nextLexeme);
        return nextLexeme;
    }

    protected Lexeme getNextLexeme() {
        if (lexemes.isEmpty()) {
            return analyzer.getNextLexeme();
        }
        return lexemes.poll();
    }

    private boolean isNonCloseableTag(String name) {
        return name != null && VOID_TAGS.contains(name.toLowerCase(Locale.ROOT));
    }

    protected void changeState(TokenizerState state) {
        this.state = state;
    }

}
