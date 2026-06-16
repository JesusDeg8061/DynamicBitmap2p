package com.dynamicbitmap.ui.fx;

import com.dynamicbitmap.core.ContactManager;
import com.dynamicbitmap.core.IdentityManager;
import com.dynamicbitmap.core.ThemeManager;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

public class ConfiguracionController {

    @FXML
    private TextField myIdField;

    @FXML
    private TextField remoteIdField;

    @FXML
    private TextField aliasField;

    @FXML
    private CheckBox darkModeCheck;

@FXML
public void initialize() {

    myIdField.setText(
            IdentityManager.getNodeId()
    );

    darkModeCheck.setSelected(
            ThemeManager.isDarkMode()
    );
}

    @FXML
    private void copyId() {

        StringSelection selection =
                new StringSelection(
                        myIdField.getText()
                );

        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                        selection,
                        null
                );
    }

    @FXML
    private void addContact() {

        String id =
                remoteIdField.getText();

        String alias =
                aliasField.getText();

        if (id.isBlank()) {
            return;
        }

        ContactManager.addContact(
                id,
                alias
        );

        remoteIdField.clear();
        aliasField.clear();
    }

@FXML
private void toggleDarkMode() {

    ThemeManager.setDarkMode(
            darkModeCheck.isSelected()
    );

    ThemeManager.applyThemeToAllScenes();
}
}