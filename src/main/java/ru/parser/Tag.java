package ru.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Tag {

    public static final Set<String> VOID_TAGS = Set.of(
            "area","base","br","col","embed","hr","img","input",
            "link","meta","param","source","track","wbr"
    );

    private String name;

    private final List<Attribute> attributes;

    private final List<Tag> children;

    private final List<String> body;

    private Tag parent;

    public Tag() {
        this.attributes = new ArrayList<>();
        this.children = new ArrayList<>();
        this.body = new ArrayList<>();
    }

    public void addBody(String body) {
        this.body.add(body);
    }
//    TODO по окончанию всех работ доделать
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
        tag.parent = this;
        children.add(tag);
    }

    public String getAttributeValue(String name) {
        for (Attribute attribute : attributes) {
            if (attribute.getName().equals(name))
                return attribute.getValue();
        }
        return null;
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

    public List<Tag> getChildren() {
        return children;
    }

    public List<String> getBody() {
        return body;
    }

    public Tag getParent() {
        return parent;
    }

    public void setParent(Tag parent) {
        this.parent = parent;
    }
}
