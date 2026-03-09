package ru.parser.tokenizer.token;

public record EndTagToken(String name) implements HtmlToken {

    public static final String NAME = "EndTagToken";

    @Override
    public String tokenName() {
        return NAME;
    }

}
