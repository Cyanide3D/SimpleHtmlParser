package ru.parser.tokenizer;

import java.io.IOException;
import java.io.InputStream;

import static ru.parser.tokenizer.TokenType.*;
import static ru.parser.tokenizer.TokenType.COMMENT;
import static ru.parser.tokenizer.TokenType.TAG_BODY;
import static ru.parser.tokenizer.TokenizerState.*;

public class NewNewTokenizer {
    private TokenizerState state;
    private final String DELIMITERS = "\t\n\r\f ";
    private final InputStream source;

    public NewNewTokenizer(InputStream source) {
        this.source = source;
        state = OPEN_TAG;
    }

    public Token getNextToken() throws IOException {
        int bytes = source.read();
        if (bytes == -1) return null;
        char c = (char) bytes;

        while (DELIMITERS.indexOf(c) >= 0) {
            c = (char) source.read();
        }

        switch (state) {
            case OPEN_TAG, CLOSE_TAG -> {
                if (c == '<') return tagName(c);
                if (c == '>') {
                    state = BODY;
                    return getNextToken();
                }
                if (Character.isAlphabetic(c)) return tagAttrName(c);
                if (c == '=' || c == '"') return tagAttrValue(c);
            }
            case BODY -> {
                if (c == '<') {
                    state = OPEN_TAG;
                    return tagName(c);
                }
                if (Character.isAlphabetic(c)) return tagBody(c);
            }
            case BODY_END -> {
                state = OPEN_TAG;
                return tagName(c);
            }
            case COMMENT -> {
                state = BODY;
                return comment(c);
            }
        }

        throw new IllegalArgumentException();
    }

    private Token comment(char c) throws IOException {
        while (DELIMITERS.indexOf(c) >= 0 || c == '-' || c == '<') {
            c = (char) source.read();
        }
        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(c);
            c = (char) source.read();
        } while (DELIMITERS.indexOf(c) < 0 && c != '-');

        while (c == '-' || c == '>') {
            c = (char) source.read();
        }

        return new Token(COMMENT, stringBuilder.toString());
    }

    private Token tagName(char c) throws IOException {
        while (DELIMITERS.indexOf(c) >= 0 || c == '<') {
            c = (char) source.read();
        }
        if (c == '!') {
            state = TokenizerState.COMMENT;
            return getNextToken();
        }
        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(c);
            c = (char) source.read();
        } while (DELIMITERS.indexOf(c) < 0 && c != '>');

        if (c == '>') state = BODY;
        return new Token(TAG_NAME, stringBuilder.toString().replaceAll("/", ""));
    }

    private Token tagAttrName(char c) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(c);
            c = (char) source.read();
        } while (DELIMITERS.indexOf(c) < 0 && c != '=' && c != '>');

        state = c == '>' ? BODY : OPEN_TAG;
        return new Token(ATTRIBUTE_NAME, stringBuilder.toString());
    }

    private Token tagAttrValue(char c) throws IOException {
        while (DELIMITERS.indexOf(c) >= 0 || c == '"' || c == '=') {
            c = (char) source.read();
        }

        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(c);
            c = (char) source.read();
        } while (c != '"');

        state = OPEN_TAG;
        return new Token(ATTRIBUTE_VALUE, stringBuilder.toString());
    }

    private Token tagBody(char c) throws IOException {
        while (DELIMITERS.indexOf(c) >= 0 || c == '>') {
            c = (char) source.read();
        }

        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(c);
            c = (char) source.read();
        } while (c != '<');

        state = BODY_END;
        return new Token(TAG_BODY, stringBuilder.toString().trim());
    }
}
