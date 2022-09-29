package ru.parser;

import ru.parser.tokenizer.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        Tokenizer tokenizer = new Tokenizer(Files.newInputStream(Path.of("index.html")));
        for (int i = 0; i < 60; i++) {
            System.out.println(tokenizer.getNextToken());
            System.out.println(tokenizer.getState());
        }
    }

}
