package ru.parser.tokenizer.token;

public record TextToken(String text) implements HtmlToken {

    public static final String NAME = "TextToken";

    @Override
    public String tokenName() {
        return NAME;
    }

}
