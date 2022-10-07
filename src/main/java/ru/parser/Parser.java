package ru.parser;

import ru.parser.tokenizer.Token;
import ru.parser.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Parser {

    private Tokenizer tokenizer;
    private final Set<String> notCloseableTags = Set.of("br", "DOCTYPE", "meta", "img");
    private final Stack<Token> cache = new Stack<>();


    public Tag parse(InputStream source) throws IOException {
        tokenizer = new Tokenizer(source);

        Tag root = new Tag();
        constructTree(root, tokenizer.getNextToken());

        return root;
    }



    private void constructTree(Tag tag, Token token) throws IOException {
        do {
            switch (token.getType()) {
                case ATTRIBUTE_NAME -> {
                    String name = token.getValue();
                    cache.push(token);
                    tag.addAttribute(name, null);
                }
                case ATTRIBUTE_VALUE -> {
                    tag.addAttribute(cache.pop().getValue(), token.getValue());
                }
                case TAG_NAME -> {
                    if (token.getValue().contains("/"))
                        return;
                    if (tag.getName() == null) {
                        tag.setName(token.getValue());
                    } else {
                        Tag child = new Tag();
                        child.setParent(tag);
                        tag.addChild(child);
                        child.setName(token.getValue());
                        if (!isNonCloseableTag(token.getValue())) {
                            constructTree(child, tokenizer.getNextToken());
                        }
                    }
                }
                case TAG_BODY -> {
                    tag.addBody(token.getValue());
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
