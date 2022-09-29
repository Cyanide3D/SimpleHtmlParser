package ru.parser_old;

public class Lexeme {

    private Character value;
    private LexemeType type;

    public Lexeme() {

    }

    public Lexeme(Character value, LexemeType type) {
        this.value = value;
        this.type = type;
    }

    public Character getValue() {
        return value;
    }

    public void setValue(Character value) {
        this.value = value;
    }

    public LexemeType getType() {
        return type;
    }

    public void setType(LexemeType type) {
        this.type = type;
    }
}
