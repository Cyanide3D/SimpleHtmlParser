package ru.parser.tokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static ru.parser.tokenizer.LexemeType.*;
import static ru.parser.tokenizer.TokenType.*;
import static ru.parser.tokenizer.TokenizerState.*;

public class TokenizerImpl implements Tokenizer {

    private TokenizerState state;
    private final LexemeAnalyzer analyzer;
    private Lexeme lexeme;


    public TokenizerImpl(LexemeAnalyzer analyzer) throws IOException {
        this.analyzer = analyzer;
        lexeme = analyzer.getNextLexeme();
        state = OPEN_TAG;
    }


    public Token getNextToken() throws IOException {
        if (lexeme == null) return null;
        skipLexemes(DELIMITER, WS);

        switch (state) {
            case OPEN_TAG -> {
                switch (lexeme.getType()) {
                    case OPEN_BRACKET -> {
                        return getTagName();
                    }
                    case CHARACTER -> {
                        return getTagAttrName();
                    }
                    case EQUAL -> {
                        return getTagAttrValue();
                    }
                    case CLOSE_BRACKET -> {
                        skipLexemes(SLASH, WS, CLOSE_BRACKET);
                        state = BODY;
                        return getNextToken();
                    }
                    case SLASH -> {
                        state = BODY;
                        return new Token(CLOSE, "/");
                    }
                }
            }
            case BODY -> {
                skipLexemes(CLOSE_BRACKET, WS, DELIMITER, SLASH);
                if (lexeme == null) return null;
                switch (lexeme.getType()) {
                    case OPEN_BRACKET -> {
                        state = OPEN_TAG;
                        return getNextToken();
                    }
                    case CHARACTER, DASH -> {
                        return getTagBody();
                    }
                }


            }
        }
        throw new RuntimeException("Unexpected lexeme: " + lexeme.getType());
    }
    private Token getTagBody() throws IOException {
        skipLexemes(CLOSE_BRACKET);
        StringBuilder builder = new StringBuilder();
        do {
            builder.append(lexeme.getValue());
            lexeme = analyzer.getNextLexeme();
        } while (!lexeme.getType().equals(OPEN_BRACKET));

        state = OPEN_TAG;

        return new Token(TAG_BODY, builder.toString().trim());
    }

    private Token getComment() throws IOException {
        skipLexemes(DELIMITER, WS);
        StringBuilder builder = new StringBuilder();
        do {
            builder.append(lexeme.getValue());
            lexeme = analyzer.getNextLexeme();
        } while (!builder.toString().endsWith("-->"));

        state = BODY;

        return new Token(TokenType.COMMENT, builder.substring(0, builder.length() - 3).trim());
    }

    private Token getTagName() throws IOException {
        skipLexemes(WS, OPEN_BRACKET);
        StringBuilder builder = new StringBuilder();

        if (lexeme.getValue().equals('!')) {
            for (int i = 0; i < 3; i++) {
                builder.append(lexeme.getValue());
                lexeme = analyzer.getNextLexeme();
            }
            if (builder.toString().equals("!--"))
                return getComment();
        }

        do {
            builder.append(lexeme.getValue());
            lexeme = analyzer.getNextLexeme();
        } while (!lexeme.getType().equals(WS) && !lexeme.getType().equals(SLASH) && !lexeme.getType().equals(CLOSE_BRACKET));

        if (lexeme.getType().equals(CLOSE_BRACKET))
            state = BODY;

        return new Token(builder.toString().contains("/") ? CLOSE : TAG_NAME, builder.toString());
    }

    private Token getTagAttrName() throws IOException {
        StringBuilder builder = new StringBuilder();
        do {
            builder.append(lexeme.getValue());
            lexeme = analyzer.getNextLexeme();
        } while (!lexeme.getType().equals(WS) && !lexeme.getType().equals(SLASH) && !lexeme.getType().equals(CLOSE_BRACKET) && !lexeme.getType().equals(EQUAL));

        if (lexeme.getType().equals(CLOSE_BRACKET))
            state = BODY;

        return new Token(ATTRIBUTE_NAME, builder.toString());
    }

    private Token getTagAttrValue() throws IOException {
        skipLexemes(WS, EQUAL, QUOTE);
        StringBuilder builder = new StringBuilder();
        do {
            builder.append(lexeme.getValue());
            lexeme = analyzer.getNextLexeme();
        } while (!lexeme.getType().equals(QUOTE));

        lexeme = analyzer.getNextLexeme();

        return new Token(ATTRIBUTE_VALUE, builder.toString());
    }

    private void skipLexemes(LexemeType... types) throws IOException {
        HashSet<LexemeType> t = new HashSet<>(Arrays.asList(types));
        while (lexeme != null && t.contains(lexeme.getType())) {
            lexeme = analyzer.getNextLexeme();
        }
    }

}
