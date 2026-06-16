package com.dynamicbitmap.core;

import java.util.ArrayList;
import java.util.List;

public class ContactManager {

    private static final List<Contact> contacts =
            new ArrayList<>();

    public static void addContact(
            String id,
            String alias
    ) {

        contacts.add(
                new Contact(
                        id,
                        alias
                )
        );
    }

    public static List<Contact> getContacts() {

        return contacts;
    }
}