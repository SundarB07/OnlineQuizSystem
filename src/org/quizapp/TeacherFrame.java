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
        btnCreateQuiz.addActionListener(e -> {
            dispose();
            new QuizCreationPanel(teacherId, true); // true: follow-up with question input
        });
        add(btnCreateQuiz);

        JButton btnViewQuiz = new JButton("View Existing Quizzes");
        btnViewQuiz.setBounds(300, 70, 220, 40);
        btnViewQuiz.setBackground(new Color(46, 140, 90));
        btnViewQuiz.setForeground(Color.WHITE);
        btnViewQuiz.addActionListener(e -> {
            dispose();
            new ExistingQuizPanel(teacherId);
        });
        add(btnViewQuiz);

        setVisible(true);
    }
}
