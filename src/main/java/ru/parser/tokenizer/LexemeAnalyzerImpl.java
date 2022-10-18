package ru.parser.tokenizer;

import java.io.IOException;
import java.io.InputStream;

import static ru.parser.tokenizer.LexemeType.*;

public class LexemeAnalyzerImpl implements LexemeAnalyzer {

    private final InputStream source;
    private final String DELIMITERS = "\t\n\r\f";

    public LexemeAnalyzerImpl(InputStream source) {
        this.source = source;
    }

    @Override
    public Lexeme getNextLexeme() throws IOException {
        int bytes = source.read();
        if (bytes == -1) return null;
        char c = (char) bytes;

        if (Character.isLetterOrDigit(c))
            return new Lexeme(c, CHARACTER);
        if (c == '\'' || c == '"')
            return new Lexeme(c, QUOTE);
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


}
