package ru.parser.tokenizer;

import java.io.IOException;
import java.io.InputStream;

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

public class LexemeAnalyzerImpl implements LexemeAnalyzer {

    private final InputStream source;
    private final String DELIMITERS = "\t\n\r\f";

    public LexemeAnalyzerImpl(InputStream source) {
        this.source = source;
    }

    @Override
    public Lexeme getNextLexeme(){
        int bytes = readByte();
        if (bytes == -1) return null;
        char c = (char) bytes;

        if (c == '!')
            return new Lexeme(c, EXCLAMATION_MARK);
        if (Character.isLetterOrDigit(c))
            return new Lexeme(c, CHARACTER);
        if (c == '\'')
            return new Lexeme(c, SINGLE_QUOTE);
        if ( c == '"')
            return new Lexeme(c, DOUBLE_QUOTE);
        if (c == '/')
            return new Lexeme(c, SLASH);
        if (c == '<')
            return new Lexeme(c, OPEN_BRACKET);
        if (c == '>')
            return new Lexeme(c, CLOSE_BRACKET);
        if (c == '-')
            return new Lexeme(c, DASH);
        if (c == '=')
            return new Lexeme(c, EQUAL);
        if (c == ' ')
            return new Lexeme(c, WS);
        if (DELIMITERS.indexOf(c) != -1)
            return new Lexeme(c, DELIMITER);

        return new Lexeme(c, CHARACTER);
    }

    private int readByte() {
        try {
            return source.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
