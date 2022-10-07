package ru.parser.tokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Predicate;

import static ru.parser.tokenizer.TokenType.*;
import static ru.parser.tokenizer.TokenizerState.*;

public class Tokenizer {

    private TokenizerState state;
    private final String DELIMITERS = "\t\n\r\f ";
    private final InputStream source;

    public Tokenizer(InputStream source) {
        this.source = source;
        this.state = OPEN_TAG;
    }


    public Token getNextToken() throws IOException {
        int bytes = source.read();
        if (bytes == -1) return null;
        char c = (char) bytes;

        switch (state) {
            case OPEN_TAG -> {
                char character = match(c, ch -> ch == '<' || Character.isAlphabetic(ch) || ch == '"' || ch == '>' || ch == '/');
                if (character == '/')
                    return getTagNameToken(character);
                if (character == '<')
                    return getTagNameToken(character);
                if (Character.isAlphabetic(character))
                    return getAttrNameToken(character);
                if (character == '"')
                    return getAttrValueToken(character);
                if (character == '>') {
                    state = BODY;
                    return getNextToken();
                }
            }

            case BODY -> {
                char character = match(c, ch -> ch == '<' || Character.isAlphabetic(ch));
                if (Character.isAlphabetic(character))
                    return getTagBodyToken(character);
                if (character == '<')
                    return getTagNameToken(character);
            }

            case CLOSE_TAG -> {
                char character = match(c, ch -> Character.isAlphabetic(ch) || ch == '/');
                return getTagNameToken(character);
            }
        }
        return null;
    }

    private Token getTagBodyToken(char c) throws IOException {
        StringBuilder builder = new StringBuilder();
        writeInStringBuilderWhile(builder, skipDelimiters(c), ch -> ch != '<');

        state = CLOSE_TAG;

        return new Token(TAG_BODY, builder.toString().trim());
    }

    private Token getTagNameToken(char c) throws IOException {
        StringBuilder builder = new StringBuilder();
        c = writeInStringBuilderWhile(builder, skipCharactersWhile(c, ch -> ch == '<'), ch -> ch != '>' && DELIMITERS.indexOf(ch) < 0);

        if (c == '>')
            state = BODY;
        else
            state = OPEN_TAG;

        return new Token(TAG_NAME, builder.toString());
    }

    private Token getAttrValueToken(char c) throws IOException {
        StringBuilder builder = new StringBuilder();
        writeInStringBuilderWhile(builder, skipCharactersWhile(c, ch -> ch == '"'), ch -> ch != '"');

        return new Token(ATTRIBUTE_VALUE, builder.toString());
    }

    private Token getAttrNameToken(char c) throws IOException {
        StringBuilder builder = new StringBuilder();
        c = writeInStringBuilderWhile(builder, skipDelimiters(c), ch -> ch != '>' && DELIMITERS.indexOf(ch) < 0 && ch != '=');

        if (c == '>')
            state = BODY;

        return new Token(ATTRIBUTE_NAME, builder.toString());
    }


    private char skipDelimiters(char c) throws IOException {
        return skipCharactersWhile(c, ch -> false);
    }

    private char skipCharactersWhile(char c, Predicate<Character> predicate) throws IOException {
        while (predicate.test(c) || DELIMITERS.indexOf(c) >= 0) {
            c = (char) source.read();
        }
        return c;
    }

    private char writeInStringBuilderWhile(StringBuilder builder, char c, Predicate<Character> predicate) throws IOException {
        do {
            builder.append(c);
            c = (char) source.read();
        } while (predicate.test(c));

        return c;
    }

    private char match(char c, Predicate<Character> predicate) throws IOException {
        char character = skipCharactersWhile(c, ch -> false);
        if (predicate.test(character)) {
            return character;
        } else {
            throw new RuntimeException("Unexpected character: " + character);
        }
    }


}
