package ru.parser.tokenizer.token;

public record DoctypeToken(String rawValue) implements HtmlToken {

    public static final String NAME = "DoctypeToken";

    @Override
    public String tokenName() {
        return NAME;
    }

}
