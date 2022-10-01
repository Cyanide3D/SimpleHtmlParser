package ru.parser;

import ru.parser.tokenizer.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
//        NewNewTokenizer tokenizer = new NewNewTokenizer(Files.newInputStream(Path.of("index.html")));
//        Token token;
//        while ((token = tokenizer.getNextToken()) != null) {
//            System.out.println(token);
//        }

        Parser parser = new Parser();
        Tag tree = parser.parse(Files.newInputStream(Path.of("index.html")));
    }
}
