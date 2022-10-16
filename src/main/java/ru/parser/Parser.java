package ru.parser;

import ru.parser.tokenizer.Token;
import ru.parser.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Parser {

    private Tokenizer tokenizer;
    private final Set<String> notCloseableTags = Set.of("br", "DOCTYPE", "meta", "img");
    private final Stack<Tag> cache = new Stack<>();


    public Tag parse(InputStream source) throws IOException {
        tokenizer = new Tokenizer(source);
        Tag tag = new Tag();
        tag.setName("");
        cache.push(tag);
        constructTree(tokenizer.getNextToken());

        return tag.getChildren().get(0);
    }



    private void constructTree(Token token) throws IOException {
        do {
            switch (token.getType()) {
                case TAG_NAME -> {
                    if (token.getValue().contains("/")) {
                        cache.pop();
                        continue;
                    }
                    if (isNonCloseableTag(cache.peek().getName()))
                        cache.pop();

                    Tag tag = new Tag();
                    cache.peek().addChild(tag);
                    tag.setName(token.getValue());
                    cache.push(tag);
                }
                case ATTRIBUTE_VALUE -> {
                    cache.peek().getLastAttr().setValue(token.getValue());
                }
                case ATTRIBUTE_NAME -> {
                    cache.peek().addAttribute(token.getValue(), null);
                }
                case TAG_BODY -> {
                    cache.peek().addBody(token.getValue());
                }
            }
        } while ((token = tokenizer.getNextToken()) != null);
    }

    private boolean isNonCloseableTag(String name) {
        for (String tag : notCloseableTags) {
            if (name.toLowerCase().contains(tag.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

}
