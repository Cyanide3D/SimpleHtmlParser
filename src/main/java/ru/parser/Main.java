package ru.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
//        Tokenizer tokenizer = new TokenizerImpl(new LexAnalyzerImpl(Files.newInputStream(Path.of("index.html"))));
//        Token token;
//        while ((token = tokenizer.getNextToken()) != null) {
//            System.out.println(token);
//        }
//
//        for (int i = 0; i < 3; i++) {
//            System.out.println(tokenizer.getNextToken());
//        }

        Parser parser = new Parser();
        Tag tree = parser.parse(Files.newInputStream(Path.of("index.html")));
    }
}
