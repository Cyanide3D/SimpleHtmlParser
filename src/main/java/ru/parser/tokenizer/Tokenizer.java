package ru.parser.tokenizer;

import java.io.IOException;
import java.io.InputStream;

import static ru.parser.tokenizer.TokenType.*;
import static ru.parser.tokenizer.TokenizerState.*;

public class Tokenizer {

    private TokenizerState state;
    private final String DELIMITERS = "\t\n\r\f ";
    private final InputStream source;

    private TokenType prevTokenType;
    private char lastChat = ' ';

    public Tokenizer(InputStream source) {
        this.source = source;
    }


    public Token getNextToken() throws IOException {
        int bytes = source.read();
        if (bytes == -1) return null;
        char c = (char) bytes;

        while (DELIMITERS.indexOf(c) >= 0) {
            c = (char) source.read();
        }

        Token token = null;
        if (c == '<' || prevTokenType == TAG_BODY) {
            token = tagName((char) source.read());
        } else if (lastChat == '>' || c == '>') {
            token = tagBody(c);
        } else if (Character.isAlphabetic(c) && (prevTokenType == TAG_NAME || prevTokenType == ATTRIBUTE_NAME || prevTokenType == ATTRIBUTE_VALUE)) {
            token = tagAttrName(c);
        } else if (c == '"' || c == '=') {
            token = tagAttrValue(c);
        } else {
            throw new UnsupportedOperationException();
        }

        prevTokenType = token.getType();
        return token;
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
        String result = stringBuilder.toString();

        if (result.contains("/"))
            state = CLOSE_TAG;
        else
            state = OPEN_TAG;
        lastChat = c;
        return new Token(TAG_NAME, result.replaceAll("/",""));
    }

    private Token tagAttrName(char c) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(c);
            c = (char) source.read();
        } while (DELIMITERS.indexOf(c) < 0 && c != '=' && c != '>');

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

        return new Token(ATTRIBUTE_VALUE, stringBuilder.toString());
    }

    private Token tagBody(char c) throws IOException {
        while (c == '>' || DELIMITERS.indexOf(c) >= 0) {
            c = (char) source.read();
        }

        if (c == '<') return tagName(c);

        StringBuilder stringBuilder = new StringBuilder();
        do {
            stringBuilder.append(c);
            c = (char) source.read();
        } while (c != '<');

        state = BODY;
        return new Token(TAG_BODY, stringBuilder.toString());
    }

}
