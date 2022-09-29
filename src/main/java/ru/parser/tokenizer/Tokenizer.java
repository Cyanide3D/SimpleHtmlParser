package ru.parser.tokenizer;

import java.io.IOException;
import java.io.InputStream;

import static ru.parser.tokenizer.TokenType.*;
import static ru.parser.tokenizer.TokenizerState.*;

public class Tokenizer {

    private TokenizerState state;
    private String html = "";
    private final char[] DELIMITERS = {'\t', '\n', '\r', '\f', ' '};

    private int currentPosition = 0;
    private int maxPosition = 0;



    public Tokenizer(InputStream source) {
        try {
            this.html = new String(source.readAllBytes());
            maxPosition = html.length()-1;

            skipDelimiters();
            if (!html.startsWith("<")) {
                throw new IllegalArgumentException();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public Token getNextToken() throws IOException {
        skipDelimiters();
        if (maxPosition-1 <= currentPosition) return null;
        char c = html.charAt(currentPosition);

        while (!Character.isAlphabetic(c) && maxPosition > currentPosition) {
            c = html.charAt(++currentPosition);
        }

        Token token = new Token();
        token.setType(getTokenType());
        token.setValue(getLetterSequenceAsString(token.getType()));

        return token;
    }

    private TokenType getTokenType() {
        for (int i = currentPosition-1; i >= 0; i--) {
            if (html.charAt(i) == '<') {
                state = OPEN_TAG;
                for (int j = i; html.charAt(j) != '>'; j++) {
                    if (html.charAt(j) == '/') {
                        state = CLOSE_TAG;
                        break;
                    }
                }
                return TAG_NAME;
            } else if (Character.isAlphabetic(html.charAt(i))) {
                return ATTRIBUTE_NAME;
            } else if (html.charAt(i) == '"') {
                return html.charAt(i - 1) == '=' ? ATTRIBUTE_VALUE : ATTRIBUTE_NAME;
            } else if (html.charAt(i) == '>') {
                state = BODY;
                return TAG_BODY;
            }
        }

        throw new IllegalArgumentException();
    }




    private String getLetterSequenceAsString(TokenType type) {
        skipDelimiters();
        StringBuilder builder = new StringBuilder();
        char c = html.charAt(currentPosition);
        if (type.equals(TAG_BODY)) {
            while (c != '<' && maxPosition > currentPosition) {
                builder.append(c);
                c = html.charAt(++currentPosition);
            }
        } else if (type.equals(ATTRIBUTE_VALUE)) {
            while (c != '"' && maxPosition > currentPosition) {
                builder.append(c);
                c = html.charAt(++currentPosition);
            }
        } else {
            while (Character.isAlphabetic(c) && maxPosition > currentPosition) {
                builder.append(c);
                c = html.charAt(++currentPosition);
            }
        }

        return builder.toString();
    }

    private void skipDelimiters() {
        for (char delimiter : DELIMITERS) {
            if (html.charAt(currentPosition) == delimiter && currentPosition < maxPosition) {
                currentPosition++;
                skipDelimiters();
                break;
            }
        }
    }




    public void setState(TokenizerState state) {
        this.state = state;
    }

    public TokenizerState getState() {
        return state;
    }
}
