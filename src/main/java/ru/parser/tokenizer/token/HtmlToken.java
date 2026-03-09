package ru.parser.tokenizer.token;

sealed public interface HtmlToken permits StartTagToken, EndTagToken, TextToken, CommentToken, DoctypeToken {

    String tokenName();

}

