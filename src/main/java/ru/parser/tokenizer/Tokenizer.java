package ru.parser.tokenizer;

import ru.parser.tokenizer.token.HtmlToken;

import java.io.IOException;

public interface Tokenizer {

    HtmlToken getNextToken() throws IOException;

}
