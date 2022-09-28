package ru.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

public class Parser {

    public Node parse(String html) {
        List<Lexeme> lexemes = readLexemesFromHtml(html);
        return null;
    }

    private List<Lexeme> readLexemesFromHtml(String html) {
        List<Lexeme> lexemes = new ArrayList<>();

        for (int i = 0; i < html.length(); i++) {
            char c = html.charAt(i);
            lexemes.add(new Lexeme(c, getLexemeType(c)));
        }

        return lexemes;
    }

    private LexemeType getLexemeType(Character character) {
        return switch (character) {
            case '<' -> LexemeType.LEFT_BRACER;
            case '>' -> LexemeType.RIGHT_BRACER;
            case '=' -> LexemeType.EQUAL;
            case '"' -> LexemeType.QUOTATION_MARK;
            case ' ' -> LexemeType.SPACE;
            default -> LexemeType.CHARACTER;
        };
    }

}
