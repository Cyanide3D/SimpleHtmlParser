package ru.parser.tokenizer;

import static ru.parser.tokenizer.LexemeType.CHARACTER;
import static ru.parser.tokenizer.LexemeType.CLOSE_BRACKET;
import static ru.parser.tokenizer.LexemeType.DASH;
import static ru.parser.tokenizer.LexemeType.DELIMITER;
import static ru.parser.tokenizer.LexemeType.DOUBLE_QUOTE;
import static ru.parser.tokenizer.LexemeType.EQUAL;
import static ru.parser.tokenizer.LexemeType.EXCLAMATION_MARK;
import static ru.parser.tokenizer.LexemeType.OPEN_BRACKET;
import static ru.parser.tokenizer.LexemeType.SINGLE_QUOTE;
import static ru.parser.tokenizer.LexemeType.SLASH;
import static ru.parser.tokenizer.LexemeType.UNDERSCORE;
import static ru.parser.tokenizer.LexemeType.WS;

public class Lexeme {

    private Character value;

    private LexemeType type;

    public Lexeme() {
    }

    public Lexeme(Character value, LexemeType type) {
        this.value = value;
        this.type = type;
    }

    public Character getValue() {
        return value;
    }

    public void setValue(Character value) {
        this.value = value;
    }

    public LexemeType getType() {
        return type;
    }

    public void setType(LexemeType type) {
        this.type = type;
    }

    public boolean isWhitespace() {
        return getType() == WS;
    }

    public boolean isDelimiter() {
        return getType() == DELIMITER;
    }

    public boolean isOpenBracket() {
        return getType() == OPEN_BRACKET;
    }

    public boolean isCloseBracket() {
        return getType() == CLOSE_BRACKET;
    }

    public boolean isEqual() {
        return getType() == EQUAL;
    }

    public boolean isSlash() {
        return getType() == SLASH;
    }

    public boolean isDoubleQuote() {
        return getType() == DOUBLE_QUOTE;
    }

    public boolean isDash() {
        return getType() == DASH;
    }

    public boolean isSingleQuote() {
        return getType() == SINGLE_QUOTE;
    }

    public boolean isQuote() {
        return isDoubleQuote() || isSingleQuote();
    }

    public boolean isUnderscore() {
        return getType() == UNDERSCORE;
    }

    public boolean isCharacter() {
        return getType() == CHARACTER;
    }

    public boolean isExclamationMark() {
        return getType() == EXCLAMATION_MARK;
    }

}
