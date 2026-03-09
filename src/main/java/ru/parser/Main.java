package ru.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        Parser parser = new Parser();
        Tag tree = parser.parse(Files.newInputStream(Path.of("index.html")));
        int i = 0;
    }
}
