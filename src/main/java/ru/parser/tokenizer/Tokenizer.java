package ru.parser.tokenizer;

import java.io.IOException;

public interface Tokenizer {

    Token getNextToken() throws IOException;

}
