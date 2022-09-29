package ru.parser_old;

import java.util.ArrayList;
import java.util.List;

public class LexemeBuffer {

    private final List<Lexeme> lexemes;
    private int index = 0;

    public LexemeBuffer() {
        lexemes = new ArrayList<>();
    }

    public LexemeBuffer(List<Lexeme> lexemes) {
        this.lexemes = lexemes;
    }

    public void back() {
        index--;
    }

    public boolean hasNext() {
        return lexemes.size() > index;
    }

    public Lexeme next() {
        return lexemes.get(index++);
    }

}
