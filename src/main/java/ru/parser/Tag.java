package ru.parser;

import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public void addBody(String body) {
        this.body.add(body);
    }
//
//    public String collectAsString() {
//        StringBuilder builder = new StringBuilder();
//        builder.append("<").append(name);
//        for (Attribute attribute : attributes) {
//            builder.append(" ").append(attribute.getName());
//            if (attribute.getValue() != null) {
//                builder.append("=\"").append(attribute.getValue()).append("\"");
//            }
//        }
//        builder.append(">").append(String.join(" ", body));
//        for (Tag child : children) {
//            builder.append(child.collectAsString());
//        }
//        builder.append("</").append(name).append(">");
//        return builder.toString();
//    }
    public void addAttribute(String name, String value) {
        attributes.removeIf(attribute -> attribute.getName().equals(name));
        attributes.add(new Attribute(name, value));
    }

    public Attribute getLastAttr() {
        return attributes.get(attributes.size() - 1);
    }

    public void addChild(Tag tag) {
        children.add(tag);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public List<Tag> getChildren() {
        return children;
    }

    public void setChildren(List<Tag> children) {
        this.children = children;
    }

    public List<String> getBody() {
        return body;
    }

    public void setBody(List<String> body) {
        this.body = body;
    }

    public Tag getParent() {
        return parent;
    }

    public void setParent(Tag parent) {
        this.parent = parent;
    }
}
