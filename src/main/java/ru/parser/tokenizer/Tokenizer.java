package ru.parser.tokenizer;

import java.io.IOException;
import java.io.InputStream;

public class Tokenizer {

    private TokenizerState state;
    private String html = "";
    private final char[] DELIMITERS = {'\t', '\n', '\r', '\f', ' '};

    private int currentPosition = 0;
    private int maxPosition = 0;




    public Tokenizer(InputStream source) {
        try {
            this.html = new String(source.readAllBytes());
            maxPosition = html.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public Token getNextToken() throws IOException {
        skipDelimiters();
        char c = html.charAt(currentPosition);
        return null;
    }






    private String getString() {
        skipDelimiters();
        StringBuilder builder = new StringBuilder();
        char c = html.charAt(currentPosition);
        while (Character.isAlphabetic(c)) {
            builder.append(c);
            c = html.charAt(++currentPosition);
        }

        return builder.toString();
    }

    private void skipDelimiters() {
        for (char delimiter : DELIMITERS) {
            if (html.charAt(currentPosition) == delimiter) {
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
