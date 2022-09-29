package ru.parser_old;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {
        String html = Files.readString(Path.of("index.html"));
        Parser parser = new Parser();
        Node node = parser.parse(html);
    }

}
