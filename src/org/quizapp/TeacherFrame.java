package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class TeacherFrame extends JFrame {
    JTextField txtQuizTitle, txtDuration, txtQuestion, txtOptionA, txtOptionB, txtOptionC, txtOptionD, txtCorrect;
    JButton btnCreateQuiz, btnAddQuestion;
    int quizId = -1;  // store created quiz id
    int teacherId = 1; // temporarily hardcoded (you can fetch from login later)

    public TeacherFrame() {
        setTitle("Teacher Dashboard - Quiz Creator");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        Font labelFont = new Font("Segoe UI", Font.PLAIN, 15);

        JLabel lblHeading = new JLabel("Create Quiz & Add Questions", SwingConstants.CENTER);
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeading.setBounds(150, 10, 400, 40);
        add(lblHeading);

        JLabel lblTitle = new JLabel("Quiz Title:");
        lblTitle.setFont(labelFont);
        lblTitle.setBounds(100, 70, 100, 25);
        add(lblTitle);

        txtQuizTitle = new JTextField();
        txtQuizTitle.setBounds(220, 70, 350, 28);
        add(txtQuizTitle);

        JLabel lblDuration = new JLabel("Duration (sec):");
        lblDuration.setFont(labelFont);
        lblDuration.setBounds(100, 110, 120, 25);
        add(lblDuration);

        txtDuration = new JTextField();
        txtDuration.setBounds(220, 110, 150, 28);
        add(txtDuration);

        btnCreateQuiz = new JButton("Create Quiz");
        btnCreateQuiz.setBounds(400, 110, 170, 30);
        btnCreateQuiz.setBackground(new Color(72, 132, 255));
        btnCreateQuiz.setForeground(Color.WHITE);
        btnCreateQuiz.addActionListener(e -> createQuiz());
        add(btnCreateQuiz);

        int y = 170;
        JLabel lblQ = new JLabel("Question:");
        lblQ.setFont(labelFont);
        lblQ.setBounds(100, y, 100, 25);
        add(lblQ);
        txtQuestion = new JTextField();
        txtQuestion.setBounds(220, y, 350, 28);
        add(txtQuestion);

        y += 40;
        add(createLabel("Option A:", 100, y));
        txtOptionA = createField(220, y);
        add(txtOptionA);

        y += 40;
        add(createLabel("Option B:", 100, y));
        txtOptionB = createField(220, y);
        add(txtOptionB);

        y += 40;
        add(createLabel("Option C:", 100, y));
        txtOptionC = createField(220, y);
        add(txtOptionC);

        y += 40;
        add(createLabel("Option D:", 100, y));
        txtOptionD = createField(220, y);
        add(txtOptionD);

        y += 40;
        add(createLabel("Correct Option (A/B/C/D):", 100, y));
        txtCorrect = createField(320, y);
        add(txtCorrect);

        btnAddQuestion = new JButton("Add Question");
        btnAddQuestion.setBounds(250, 460, 200, 35);
        btnAddQuestion.setBackground(new Color(40, 167, 69));
        btnAddQuestion.setForeground(Color.WHITE);
        btnAddQuestion.addActionListener(e -> addQuestion());
        add(btnAddQuestion);

        setVisible(true);
    }

    JLabel createLabel(String text, int x, int y) {
        JLabel l = new JLabel(text);
        l.setBounds(x, y, 200, 25);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        return l;
    }

    JTextField createField(int x, int y) {
        JTextField t = new JTextField();
        t.setBounds(x, y, 350, 28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return t;
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
            if (rs.next()) {
                quizId = rs.getInt(1);
            }
            JOptionPane.showMessageDialog(this, "Quiz created successfully! ID: " + quizId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating quiz: " + e.getMessage());
        }
    }

    void addQuestion() {
        if (quizId == -1) {
            JOptionPane.showMessageDialog(this, "Please create a quiz first!");
            return;
        }
        try (Connection con = DBConnection.getConnection()) {
            String sql = "INSERT INTO questions (quiz_id, question_text, optionA, optionB, optionC, optionD, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, quizId);
            ps.setString(2, txtQuestion.getText());
            ps.setString(3, txtOptionA.getText());
            ps.setString(4, txtOptionB.getText());
            ps.setString(5, txtOptionC.getText());
            ps.setString(6, txtOptionD.getText());
            ps.setString(7, txtCorrect.getText());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Question added successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error adding question: " + e.getMessage());
        }
    }
}
