package ru.parser.tokenizer;

import java.io.IOException;

public interface LexemeAnalyzer {

    Lexeme getNextLexeme() throws IOException;

}
