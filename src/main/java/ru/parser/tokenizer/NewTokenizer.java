package ru.parser.tokenizer;

import java.io.IOException;
import java.io.InputStream;

import static ru.parser.tokenizer.TokenType.*;
import static ru.parser.tokenizer.TokenizerState.*;

public class NewTokenizer {
    private TokenizerState state;
    private final String DELIMITERS = "\t\n\r\f ";
    private final InputStream source;

    private State nextToHandle;

    public NewTokenizer(InputStream source) {
        this.source = source;
        nextToHandle = State.T_NAME;
    }


    public Token getNextToken() throws IOException {
        int bytes = source.read();
        if (bytes == -1) return null;
        char c = (char) bytes;

        while (DELIMITERS.indexOf(c) >= 0) {
            c = (char) source.read();
        }

        switch (nextToHandle) {
            case T_NAME -> {
                return tagName(c);
            }
            case T_ATTRS -> {
                return tagAttrName(c);
            }
            case T_BODY -> {
                return tagBody(c);
            }
        }

        throw new UnsupportedOperationException();
    }

    private Token tagName(char c) throws IOException {
        while (DELIMITERS.indexOf(c) >= 0 || c == '<') {
            c = (char) source.read();
        }

        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(c);
            c = (char) source.read();
        } while (DELIMITERS.indexOf(c) < 0 && c != '>');

        nextToHandle = c == '>' ? State.T_BODY : State.T_ATTRS;
        String result = stringBuilder.toString();
        state = result.contains("/") ? CLOSE_TAG : OPEN_TAG;
        return new Token(TAG_NAME, result.replaceAll("/",""));
    }

    private Token tagAttrName(char c) throws IOException {
        while (DELIMITERS.indexOf(c) >= 0) {
            c = (char) source.read();
        }
        if (c == '>') return tagBody(c);
        if (c == '=' || c == '"') return tagAttrValue(c);

        StringBuilder builder = new StringBuilder();
        do {
            builder.append(c);
            c = (char) source.read();
        } while (DELIMITERS.indexOf(c) < 0 && c != '=' && c != '>');

        if (c == '>') nextToHandle = State.T_BODY;
        return new Token(ATTRIBUTE_NAME, builder.toString());
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

        return new Token(ATTRIBUTE_VALUE, stringBuilder.toString());
    }

    private Token tagBody(char c) throws IOException {
        while (DELIMITERS.indexOf(c) >= 0 || c == '>') {
            c = (char) source.read();
        }
        if (c == '<') return tagName(c);
        StringBuilder builder = new StringBuilder();
        do {
            builder.append(c);
            c = (char) source.read();
        } while (c != '<');

        nextToHandle = State.T_NAME;
        state = BODY;
        return new Token(TAG_BODY, builder.toString().trim());
    }

    private enum State {
        T_NAME, T_ATTRS, T_BODY
    }

}
