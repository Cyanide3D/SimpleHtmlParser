package ru.parser;

import ru.parser.tokenizer.LexemeAnalyzerImpl;
import ru.parser.tokenizer.Tokenizer;
import ru.parser.tokenizer.TokenizerImpl;
import ru.parser.tokenizer.token.CommentToken;
import ru.parser.tokenizer.token.DoctypeToken;
import ru.parser.tokenizer.token.EndTagToken;
import ru.parser.tokenizer.token.HtmlToken;
import ru.parser.tokenizer.token.StartTagToken;
import ru.parser.tokenizer.token.TextToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Stack;

public class Parser {

    private final Stack<Tag> cache = new Stack<>();

    public Tag parse(InputStream is) throws IOException {
        Tag tag = new Tag();
        tag.setName("");
        cache.clear();
        cache.push(tag);
        buildTree(new TokenizerImpl(new LexemeAnalyzerImpl(is)));
        return tag;
    }

    private void buildTree(Tokenizer tokenizer) throws IOException {
        HtmlToken token;

        while ((token = tokenizer.getNextToken()) != null) {
            switch (token.tokenName()) {
                case StartTagToken.NAME -> {
                    StartTagToken startTagToken = (StartTagToken) token;
                    Tag tag = new Tag();
                    tag.setName(startTagToken.name());

                    Map<String, String> attrs = startTagToken.attrs();
                    for (Map.Entry<String, String> attr : attrs.entrySet()) {
                        tag.addAttribute(attr.getKey(), attr.getValue());
                    }

                    Tag lastTag = cache.peek();
                    lastTag.addChild(tag);
                    tag.setParent(lastTag);

                    if (!startTagToken.selfClosing()) {
                        cache.push(tag);
                    }
                }
                case EndTagToken.NAME -> {
                    EndTagToken endTagToken = (EndTagToken) token;
                    String name = endTagToken.name();
                    if (cache.size() == 1) {
                        throw new RuntimeException("Unexpected closing tag: " + name);
                    }
                    Tag lastTag = cache.peek();
                    if (!lastTag.getName().equals(name)) {
                        throw new RuntimeException(
                                "End tag name mismatch: expected </" + lastTag.getName() + "> but got </" + name + ">"
                        );
                    }
                    cache.pop();
                }
                case TextToken.NAME -> {
                    TextToken textToken = (TextToken) token;
                    Tag lastTag = cache.peek();
                    lastTag.addBody(textToken.text());
                }
                case DoctypeToken.NAME -> {
                    DoctypeToken doctypeToken = (DoctypeToken) token;
                    Tag tag = new Tag();
                    tag.setName("doctype");
                    tag.addAttribute("rawValue", doctypeToken.rawValue());
                    Tag lastTag = cache.peek();
                    lastTag.addChild(tag);
                    tag.setParent(lastTag);
                }
                case CommentToken.NAME -> {
                    CommentToken commentToken = (CommentToken) token;
                    Tag tag = new Tag();
                    tag.setName("comment");
                    tag.addBody(commentToken.text());
                    Tag lastTag = cache.peek();
                    lastTag.addChild(tag);
                    tag.setParent(lastTag);
                }
                default -> throw new IllegalStateException("Unexpected token: " + token.tokenName());
            }
        }

        if (cache.size() != 1) {
            throw new RuntimeException("Unclosed tags remain");
        }
    }

}
