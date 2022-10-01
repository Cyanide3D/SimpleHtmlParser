package ru.parser;

import ru.parser.tokenizer.NewNewTokenizer;
import ru.parser.tokenizer.Token;
import ru.parser.tokenizer.TokenType;
import ru.parser.tokenizer.Tokenizer;

import java.io.IOException;
import java.io.InputStream;

public class Parser {

    private NewNewTokenizer tokenizer;

    //<tagName (attrName(="tagValue"))>(body | comment)</tagName>
    public Tag parse(InputStream source) throws IOException {
        tokenizer = new NewNewTokenizer(source);
        Tag tag = new Tag();
        handle(tag, tokenizer.getNextToken());
        return tag;
    }

    private void handle(Tag tag, Token token) throws IOException {
        do {
            switch (token.getType()) {
                case TAG_NAME -> {
                    if (token.getValue().contains("/")) return;
                    if (tag.getName() == null) {
                        tag.setName(token.getValue());
                    } else {
                        Tag child = new Tag();
                        tag.addChild(child);
                        child.setName(token.getValue());
                        handle(child, tokenizer.getNextToken());
                    }
                }
                case TAG_BODY, COMMENT -> tag.addBody(token.getValue());
                case ATTRIBUTE_NAME -> {
                    String attrName = token.getValue();
                    Token nextToken = tokenizer.getNextToken();
                    if (nextToken.getType() == TokenType.ATTRIBUTE_VALUE) {
                        tag.addAttribute(attrName, nextToken.getValue());
                    } else {
                        tag.addAttribute(attrName, null);
                        handle(tag, nextToken);
                        return;
                    }
                }
            }
        } while ((token = tokenizer.getNextToken()) != null);
    }

}
