package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class QuizCreationPanel extends JFrame {
    int teacherId;
    JTextField txtQuizTitle, txtDuration;
    boolean followQuestionInput = false;

    public QuizCreationPanel(int teacherId, boolean followQuestionInput) {
        this.teacherId = teacherId;
        this.followQuestionInput = followQuestionInput;

        setTitle("Create New Quiz");
        setSize(700, 400);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel lblTitle = new JLabel("Quiz Title:");
        lblTitle.setBounds(100, 50, 100, 25);
        add(lblTitle);

        txtQuizTitle = new JTextField();
        txtQuizTitle.setBounds(220, 50, 300, 28);
        add(txtQuizTitle);

        JLabel lblDuration = new JLabel("Duration (mins):");
        lblDuration.setBounds(100, 100, 120, 25);
        add(lblDuration);

        txtDuration = new JTextField();
        txtDuration.setBounds(220, 100, 150, 28);
        add(txtDuration);

        JButton btnCreate = new JButton("Create Quiz");
        btnCreate.setBounds(400, 100, 150, 30);
        btnCreate.addActionListener(e -> createQuiz());
        add(btnCreate);

        JButton btnBack = new JButton("Back");
        btnBack.setBounds(50, 300, 100, 30);
        btnBack.setBackground(Color.GRAY);
        btnBack.setForeground(Color.WHITE);
        btnBack.addActionListener(e -> {
            dispose();
            new TeacherFrame();
        });
        add(btnBack);

        setVisible(true);
    }

    void createQuiz() {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO quiz (title, duration, created_by) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, txtQuizTitle.getText());
            ps.setInt(2, Integer.parseInt(txtDuration.getText()));
            ps.setInt(3, teacherId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int quizId = -1;
            if (rs.next()) quizId = rs.getInt(1);

            if (followQuestionInput && quizId != -1) {
                dispose();
                new QuestionInputPanel(quizId); // after quiz creation, go to question input
            } else {
                JOptionPane.showMessageDialog(this, "Quiz created! ID: " + quizId);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }
}
