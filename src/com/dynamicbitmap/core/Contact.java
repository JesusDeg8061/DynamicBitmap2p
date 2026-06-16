package com.dynamicbitmap.core;

public class Contact {

    private String id;
    private String alias;

    public Contact(
            String id,
            String alias
    ) {
        this.id = id;
        this.alias = alias;
    }

    public String getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return alias;
    }
}