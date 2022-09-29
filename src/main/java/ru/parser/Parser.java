package ru.parser;

import ru.parser.tokenizer.Tokenizer;

import java.io.InputStream;

public class Parser {

    private Tokenizer tokenizer;

    public Tag parse(InputStream source) {
        tokenizer = new Tokenizer(source);
        return null;
    }

}
