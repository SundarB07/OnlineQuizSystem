package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StudentFrame extends JFrame {

    public StudentFrame() {
        setTitle("Student Dashboard");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(245, 250, 255));

        JLabel lblTitle = new JLabel("Welcome to the Quiz Portal", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setBounds(60, 40, 380, 30);
        panel.add(lblTitle);

        JTextArea instructions = new JTextArea(
            "Instructions:\n" +
            "1. You will be given one question at a time.\n" +
            "2. Select the correct answer and proceed.\n" +
            "3. Timer will be active during the quiz.\n" +
            "4. Your score will be shown at the end."
        );
        instructions.setBounds(70, 100, 350, 120);
        instructions.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructions.setEditable(false);
        instructions.setBackground(new Color(245, 250, 255));
        instructions.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        panel.add(instructions);

        JButton btnStart = new JButton("Start Quiz");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnStart.setBackground(new Color(46, 140, 90));
        btnStart.setForeground(Color.WHITE);
        btnStart.setBounds(180, 250, 130, 40);
        btnStart.setFocusPainted(false);
        btnStart.addActionListener(e -> {
            dispose();
            new QuizTaker();
        });
        panel.add(btnStart);

        add(panel);
        setVisible(true);
    }
}
