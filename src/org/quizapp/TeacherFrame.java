package org.quizapp;

import javax.swing.*;
import java.awt.*;

public class TeacherFrame extends JFrame {
    int teacherId = 1; // hardcoded for now

    public TeacherFrame() {
        setTitle("Teacher Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblHeading = new JLabel("Teacher Dashboard", SwingConstants.CENTER);
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblHeading.setBounds(200, 10, 400, 40);
        add(lblHeading);

        JButton btnCreateQuiz = new JButton("Create New Quiz");
        btnCreateQuiz.setBounds(50, 70, 200, 40);
        btnCreateQuiz.setBackground(new Color(72, 132, 255));
        btnCreateQuiz.setForeground(Color.WHITE);
        btnCreateQuiz.addActionListener(e -> openCreateQuizPanel());
        add(btnCreateQuiz);

        JButton btnViewQuiz = new JButton("View Existing Quizzes");
        btnViewQuiz.setBounds(300, 70, 220, 40);
        btnViewQuiz.setBackground(new Color(46, 140, 90));
        btnViewQuiz.setForeground(Color.WHITE);
        btnViewQuiz.addActionListener(e -> showExistingQuizzes());
        add(btnViewQuiz);

     // Logout Button
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBounds(650, 20, 100, 35); // position on top-right corner
        btnLogout.setBackground(new Color(220, 53, 69)); // red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(true);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnLogout.addActionListener(e -> {
            dispose();
            new RoleSelectionFrame();
        });

        add(btnLogout); // make sure it's added to the frame


        setVisible(true);
    }

    void openCreateQuizPanel() {
        dispose();
        new QuizCreationPanel(teacherId, true); // true to follow QuestionInputPanel
    }

    void showExistingQuizzes() {
        dispose();
        new ExistingQuizPanel(teacherId);
    }
}
