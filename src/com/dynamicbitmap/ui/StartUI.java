package com.dynamicbitmap.ui;

import com.dynamicbitmap.core.IdentityManager;

import javax.swing.*;
import java.awt.*;

public class StartUI extends JFrame {

    public StartUI() {

        setTitle("DynamicBitmap P2P");
        setSize(400, 200);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(30,30,30));

        String id = IdentityManager.getNodeId();

        JLabel label = new JLabel("ID: " + id);
        label.setForeground(new Color(0,255,150));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JButton enter = new JButton("Entrar a la red");

        enter.addActionListener(e -> {
            new MainUI(id); // siguiente fase
            dispose();
        });

        add(label, BorderLayout.CENTER);
        add(enter, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new StartUI();
    }
}