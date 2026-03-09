package ru.parser.tokenizer.token;

public record CommentToken(String text) implements HtmlToken {

    public static final String NAME = "CommentToken";

    @Override
    public String tokenName() {
        return NAME;
    }

}
