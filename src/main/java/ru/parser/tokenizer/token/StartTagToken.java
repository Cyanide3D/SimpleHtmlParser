package ru.parser.tokenizer.token;

import java.util.Map;

public record StartTagToken(String name, Map<String, String> attrs, boolean selfClosing) implements HtmlToken {

    public static final String NAME = "StartTagToken";

    @Override
    public String tokenName() {
        return NAME;
    }

}
