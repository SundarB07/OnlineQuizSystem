package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class QuizTaker extends JFrame {
    JLabel lblQuestion, lblTimer;
    JRadioButton[] options = new JRadioButton[4];
    JButton btnNext;
    ButtonGroup bg;
    int current = 0, score = 0, total = 0;
    Timer timer;

    ResultSet rs;

    public QuizTaker() {
        setTitle("Quiz in Progress");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(250, 250, 255));

        lblTimer = new JLabel("Time Left: 30s", SwingConstants.RIGHT);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTimer.setBounds(400, 10, 180, 30);
        panel.add(lblTimer);

        lblQuestion = new JLabel("", SwingConstants.LEFT);
        lblQuestion.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblQuestion.setBounds(50, 60, 500, 40);
        panel.add(lblQuestion);

        bg = new ButtonGroup();
        int y = 120;
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setBounds(70, y, 400, 25);
            options[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            options[i].setBackground(panel.getBackground());
            bg.add(options[i]);
            panel.add(options[i]);
            y += 35;
        }

        btnNext = new JButton("Next");
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnNext.setBackground(new Color(72, 132, 255));
        btnNext.setForeground(Color.WHITE);
        btnNext.setBounds(230, 300, 120, 40);
        btnNext.addActionListener(e -> nextQuestion());
        panel.add(btnNext);

        add(panel);
        loadQuestion();
        setVisible(true);

        startTimer(30);
    }

    void loadQuestion() {
        try (Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = st.executeQuery("SELECT * FROM quiz");
            rs.last();
            total = rs.getRow();
            rs.first();
            showQuestion();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading quiz: " + e.getMessage());
        }
    }

    void showQuestion() {
        try {
            lblQuestion.setText("Q" + (current + 1) + ": " + rs.getString("question_text"));
            options[0].setText(rs.getString("optionA"));
            options[1].setText(rs.getString("optionB"));
            options[2].setText(rs.getString("optionC"));
            options[3].setText(rs.getString("optionD"));
            bg.clearSelection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void nextQuestion() {
        try {
            String ans = "";
            for (JRadioButton rb : options)
                if (rb.isSelected()) ans = rb.getText();
            if (ans.equals(rs.getString("answer"))) score++;

            if (!rs.next()) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Quiz Completed!\nYour Score: " + score + "/" + total);
                dispose();
                new StudentFrame();
            } else {
                current++;
                showQuestion();
                startTimer(30);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void startTimer(int seconds) {
        if (timer != null) timer.stop();
        final int[] timeLeft = {seconds};
        lblTimer.setText("Time Left: " + timeLeft[0] + "s");
        timer = new Timer(1000, e -> {
            timeLeft[0]--;
            lblTimer.setText("Time Left: " + timeLeft[0] + "s");
            if (timeLeft[0] <= 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "Time's up for this question!");
                nextQuestion();
            }
        });
        timer.start();
    }
}
