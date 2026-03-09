package ru.parser.tokenizer;

public enum TokenizerState {

    DATA,

    TAG_OPEN,

            TAG_NAME,

    BEFORE_ATTR_NAME,

            ATTR_NAME,

    BEFORE_ATTR_VALUE,

            ATTR_VALUE_QUOTED,

    ATTR_VALUE_UNQUOTED,

            COMMENT

}
