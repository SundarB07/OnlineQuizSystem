package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizTaker extends JFrame {

    JLabel lblQuestion, lblTimer;
    JRadioButton[] options = new JRadioButton[4];
    JButton btnNext;
    ButtonGroup bg;

    int current = 0, score = 0, total = 0;
    Timer timer;
    int timeLeft = 0; // in seconds
    int quizId;

    List<Question> questions = new ArrayList<>();

    class Question {
        String questionText, optionA, optionB, optionC, optionD, correctOption;

        Question(String q, String a, String b, String c, String d, String correct) {
            questionText = q;
            optionA = a;
            optionB = b;
            optionC = c;
            optionD = d;
            correctOption = correct;
        }
    }

    public QuizTaker(int quizId) {
        this.quizId = quizId;

        setTitle("Quiz in Progress");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(250, 250, 255));

        lblTimer = new JLabel("Time Left: 0:00", SwingConstants.RIGHT);
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
        loadQuestions(); // load questions for this quiz
        setVisible(true);
    }

    void loadQuestions() {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT * FROM questions WHERE quiz_id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();

            questions.clear();
            total = 0;
            int quizDuration = 0;

            // get duration from quiz table
            Statement st = con.createStatement();
            ResultSet rsQuiz = st.executeQuery("SELECT duration FROM quiz WHERE quiz_id=" + quizId);
            if (rsQuiz.next()) quizDuration = rsQuiz.getInt("duration");

            while (rs.next()) {
                questions.add(new Question(
                        rs.getString("question_text"),
                        rs.getString("optionA"),
                        rs.getString("optionB"),
                        rs.getString("optionC"),
                        rs.getString("optionD"),
                        rs.getString("correct_option")
                ));
            }
            total = questions.size();
            if (total == 0) {
                JOptionPane.showMessageDialog(this, "No questions found for this quiz!");
                dispose();
                new StudentFrame();
                return;
            }

            timeLeft = quizDuration * 60; // convert minutes to seconds
            showQuestion();
            startTimer();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading quiz: " + e.getMessage());
        }
    }

    void showQuestion() {
        if (current >= total) return;
        Question q = questions.get(current);
        lblQuestion.setText("Q" + (current + 1) + ": " + q.questionText);
        options[0].setText(q.optionA);
        options[1].setText(q.optionB);
        options[2].setText(q.optionC);
        options[3].setText(q.optionD);
        bg.clearSelection();
    }

    void nextQuestion() {
        Question q = questions.get(current);
        String ans = "";
        for (JRadioButton rb : options)
            if (rb.isSelected()) ans = rb.getText();
        if (ans.equals(q.correctOption)) score++;

        current++;
        if (current >= total) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Quiz Completed!\nYour Score: " + score + "/" + total);

            // save result to DB
            try (Connection con = DBConnection.getConnection()) {
                String sql = "INSERT INTO results(student_id, quiz_id, score) VALUES(?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, 1); // TODO: replace with logged-in student_id
                ps.setInt(2, quizId);
                ps.setInt(3, score);
                ps.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            dispose();
            new StudentFrame();
        } else {
            showQuestion();
        }
    }

    void startTimer() {
        if (timer != null) timer.stop();
        timer = new Timer(1000, new ActionListener() {
            int remaining = timeLeft;

            @Override
            public void actionPerformed(ActionEvent e) {
                int mins = remaining / 60;
                int secs = remaining % 60;
                lblTimer.setText(String.format("Time Left: %d:%02d", mins, secs));
                remaining--;
                if (remaining < 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(null, "Time's up!");
                    dispose();
                    new StudentFrame();
                }
            }
        });
        timer.start();
    }
}
