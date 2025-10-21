package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class QuestionInputPanel extends JFrame {
    int quizId;
    int totalQuestions;
    int currentQuestion = 1;

    JTextField txtQuestion, txtOptionA, txtOptionB, txtOptionC, txtOptionD, txtCorrect;

    public QuestionInputPanel(int quizId) {
        this.quizId = quizId;

        setTitle("Add Questions to Quiz ID: " + quizId);
        setSize(700, 500);
        setLayout(null);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        askNumberOfQuestions();
    }

    void askNumberOfQuestions() {
        String input = JOptionPane.showInputDialog(this, "Enter number of questions for this quiz:");
        if (input == null) {
            dispose();
            new TeacherFrame();
            return;
        }
        try {
            totalQuestions = Integer.parseInt(input);
            if (totalQuestions <= 0) throw new NumberFormatException();
            showQuestionInput();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number. Try again.");
            askNumberOfQuestions();
        }
    }

    void showQuestionInput() {
        getContentPane().removeAll();

        JLabel lblQ = new JLabel("Question " + currentQuestion + ":");
        lblQ.setBounds(50, 30, 100, 25);
        add(lblQ);
        txtQuestion = new JTextField();
        txtQuestion.setBounds(180, 30, 450, 25);
        add(txtQuestion);

        JLabel lblA = new JLabel("Option A:");
        lblA.setBounds(50, 80, 100, 25);
        add(lblA);
        txtOptionA = new JTextField();
        txtOptionA.setBounds(180, 80, 300, 25);
        add(txtOptionA);

        JLabel lblB = new JLabel("Option B:");
        lblB.setBounds(50, 130, 100, 25);
        add(lblB);
        txtOptionB = new JTextField();
        txtOptionB.setBounds(180, 130, 300, 25);
        add(txtOptionB);

        JLabel lblC = new JLabel("Option C:");
        lblC.setBounds(50, 180, 100, 25);
        add(lblC);
        txtOptionC = new JTextField();
        txtOptionC.setBounds(180, 180, 300, 25);
        add(txtOptionC);

        JLabel lblD = new JLabel("Option D:");
        lblD.setBounds(50, 230, 100, 25);
        add(lblD);
        txtOptionD = new JTextField();
        txtOptionD.setBounds(180, 230, 300, 25);
        add(txtOptionD);

        JLabel lblCorrect = new JLabel("Correct Option:");
        lblCorrect.setBounds(50, 280, 120, 25);
        add(lblCorrect);
        txtCorrect = new JTextField();
        txtCorrect.setBounds(180, 280, 100, 25);
        add(txtCorrect);

        JButton btnNext = new JButton(currentQuestion == totalQuestions ? "Finish" : "Next");
        btnNext.setBounds(300, 350, 120, 35);
        btnNext.setBackground(new Color(72, 132, 255));
        btnNext.setForeground(Color.WHITE);
        btnNext.addActionListener(e -> saveQuestion());
        add(btnNext);

        JButton btnBack = new JButton("Back");
        btnBack.setBounds(50, 350, 100, 35);
        btnBack.setBackground(Color.GRAY);
        btnBack.setForeground(Color.WHITE);
        btnBack.addActionListener(e -> {
            dispose();
            new TeacherFrame();
        });
        add(btnBack);

        revalidate();
        repaint();
        setVisible(true);
    }

    void saveQuestion() {
        String question = txtQuestion.getText().trim();
        String a = txtOptionA.getText().trim();
        String b = txtOptionB.getText().trim();
        String c = txtOptionC.getText().trim();
        String d = txtOptionD.getText().trim();
        String correct = txtCorrect.getText().trim();

        if (question.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty() || correct.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO questions (quiz_id, question_text, optionA, optionB, optionC, optionD, correct_option) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, quizId);
            ps.setString(2, question);
            ps.setString(3, a);
            ps.setString(4, b);
            ps.setString(5, c);
            ps.setString(6, d);
            ps.setString(7, correct);
            ps.executeUpdate();

            if (currentQuestion < totalQuestions) {
                currentQuestion++;
                showQuestionInput(); // next question
            } else {
                JOptionPane.showMessageDialog(this, "All questions added successfully!");
                dispose();
                new TeacherFrame();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving question: " + e.getMessage());
        }
    }
}
