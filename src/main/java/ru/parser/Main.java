package ru.parser;

import ru.parser.tokenizer.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        Tokenizer tokenizer = new Tokenizer(Files.newInputStream(Path.of("index.html")));
        tokenizer.getNextToken();
    }

}
