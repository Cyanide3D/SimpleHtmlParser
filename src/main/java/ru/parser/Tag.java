package ru.parser;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;

public class Tag {

    private String name;
    private List<Attribute> attributes;
    private List<Tag> children;
    private List<String> body;
    private Tag parent;

    public Tag() {
        this.attributes = new ArrayList<>();
        this.children = new ArrayList<>();
        this.body = new ArrayList<>();
    }

    public void addAttribute(String name, String value) {
        attributes.add(new Attribute(name, value));
    }

    public void addChild(Tag tag) {
        children.add(tag);
    }
}
