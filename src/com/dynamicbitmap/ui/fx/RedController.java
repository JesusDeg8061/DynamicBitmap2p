package com.dynamicbitmap.ui.fx;

import com.dynamicbitmap.core.Contact;
import com.dynamicbitmap.core.ContactManager;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class RedController {

    @FXML
    private ListView<String> contactsList;

    @FXML
    public void initialize() {

        refreshContacts();
    }

    private void refreshContacts() {

        contactsList.getItems().clear();

        for (Contact contact :
                ContactManager.getContacts()) {

            contactsList.getItems().add(
                    contact.getAlias()
                            + " ("
                            + contact.getId()
                            + ")"
            );
        }
    }
}