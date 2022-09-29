package ru.parser_old;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public Node parse(String html) {
        LexemeBuffer lexemeBuffer = new LexemeBuffer(readLexemesFromHtml(html));
        Node node = new Node();
        tag(node, lexemeBuffer);
        return node;
    }

    private void tag(Node node, LexemeBuffer buffer) {
        if (buffer.hasNext()){
            Lexeme lexeme = buffer.next();
            switch (lexeme.getType()) {
                case LEFT_BRACER -> {
                    node.setTag(makeStringFromLexeme(buffer));
                    attrs(node, buffer);
                    tag(node, buffer);
                }
                case RIGHT_BRACER -> {
                    //TODO
                }

                case SPACE -> {
                    skipWhitespaces(buffer);
                    tag(node, buffer);
                }
            }
        }
    }

    private void attrs(Node node, LexemeBuffer buffer) {

    }

    private void content(Node node, LexemeBuffer buffer) {

    }

    private String makeStringFromLexeme(LexemeBuffer buffer) {
        StringBuilder stringBuilder = new StringBuilder();
        while (buffer.hasNext()) {
            Lexeme lexeme = buffer.next();
            if (lexeme.getType().equals(LexemeType.CHARACTER)) {
                stringBuilder.append(lexeme.getValue());
            } else {
                buffer.back();
                break;
            }
        }

        return stringBuilder.toString();
    }

    private Lexeme skipWhitespaces(LexemeBuffer buffer) { {
        Lexeme lexeme = buffer.next();
        while (lexeme.getType().equals(LexemeType.SPACE)) {
            lexeme = buffer.next();
        }
        buffer.back();
        return lexeme;
    }

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
            case '/' -> LexemeType.SLASH;
            default -> LexemeType.CHARACTER;
        };
    }

}
