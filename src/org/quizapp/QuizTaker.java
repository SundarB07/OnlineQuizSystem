package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizTaker extends JFrame {

    JLabel lblQuestion, lblTimer;
    JCheckBox[] options = new JCheckBox[4];
    JButton btnNext;
    ButtonGroup bg;

    int current = 0, score = 0, totalQuestions = 0, totalMarks = 0;
    Timer timer;
    int timeLeft = 0; // in seconds
    int quizId;

    List<Question> questions = new ArrayList<>();

    class Question {
        int qId;
        private int marks;
        public int getMarks() {return marks;}
        String questionText, optionA, optionB, optionC, optionD, correctOption; // correctOption can be null for multi-answer
        // constructor updated
        Question(int id, int m, String q, String a, String b, String c, String d, String correct) {
            qId = id;
            marks = m;
            questionText = q;
            optionA = a;
            optionB = b;
            optionC = c;
            optionD = d;
            correctOption = correct; // single-answer -> "A"/"B"/... ; multi-answer -> null
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
        
        JLabel lblSection = new JLabel();
        lblSection.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSection.setBounds(50, 30, 300, 25);
        add(lblSection);

        bg = new ButtonGroup();
        int y = 120;
        for (int i = 0; i < 4; i++) {
            options[i] = new JCheckBox();
            options[i].setBounds(70, y, 400, 25);
            options[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            options[i].setBackground(panel.getBackground());
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
        	String sql = "SELECT q.*, s.section_name, s.marks_per_question AS marks " +
                    "FROM questions q " +
                    "LEFT JOIN quiz_sections s ON q.section_id = s.section_id " +
                    "WHERE q.quiz_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();

            questions.clear();
//            total = 0;
            int quizDuration = 0;

            // get duration from quiz table
            Statement st = con.createStatement();
            ResultSet rsQuiz = st.executeQuery("SELECT duration FROM quiz WHERE quiz_id=" + quizId);
            if (rsQuiz.next()) quizDuration = rsQuiz.getInt("duration");

            while (rs.next()) {
                int qid = rs.getInt("q_id"); // note: your questions table used q_id as primary key
                questions.add(new Question(
                        qid,
                        rs.getInt("marks"),
                        rs.getString("question_text"),
                        rs.getString("optionA"),
                        rs.getString("optionB"),
                        rs.getString("optionC"),
                        rs.getString("optionD"),
                        rs.getString("correct_option") // may be null for multi-answer
                ));
            }
            totalQuestions = questions.size();
//            totalMarks = 0;
//            for (Question q : questions) {
//                totalMarks += q.getMarks();
//            }

            if (totalQuestions == 0) {
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
        if (current >= totalQuestions) return;
        Question q = questions.get(current);
        
        lblQuestion.setText("Q" + (current + 1) + ": " + q.questionText);
        options[0].setText(q.optionA);
        options[1].setText(q.optionB);
        options[2].setText(q.optionC);
        options[3].setText(q.optionD);
        // clear selections
        for (int i = 0; i < 4; i++) options[i].setSelected(false);
    }

    void nextQuestion() {
        Question q = questions.get(current);

        // get user’s selected options
        java.util.Set<String> userSelected = new java.util.HashSet<>();
        if (options[0].isSelected()) userSelected.add("A");
        if (options[1].isSelected()) userSelected.add("B");
        if (options[2].isSelected()) userSelected.add("C");
        if (options[3].isSelected()) userSelected.add("D");

        try (Connection con = DBConnection.getConnection()) {
            //  fetch marks_per_question from quiz_sections
            int marksPerQ = 1;
            PreparedStatement psMarks = con.prepareStatement(
                "SELECT s.marks_per_question FROM quiz_sections s JOIN questions q ON s.section_id = q.section_id WHERE q.q_id = ?"
            );
            psMarks.setInt(1, q.qId);
            ResultSet rsMarks = psMarks.executeQuery();
            if (rsMarks.next()) marksPerQ = rsMarks.getInt("marks_per_question");

            if (q.correctOption != null && !q.correctOption.trim().isEmpty()) {
                // single answer
                if (userSelected.size() == 1 && userSelected.contains(q.correctOption.toUpperCase())) {
                    score += marksPerQ; // add weighted marks
                }
            } else {
                // multi-answer
                String sql = "SELECT correct_option FROM question_answers WHERE q_id=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, q.qId);
                ResultSet rs = ps.executeQuery();

                java.util.Set<String> correctSet = new java.util.HashSet<>();
                while (rs.next()) {
                    correctSet.add(rs.getString("correct_option").toUpperCase());
                }

                if (userSelected.equals(correctSet)) {
                    score += marksPerQ; // add weighted marks
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        current++;
        if (current >= totalQuestions) {
            if (timer != null) timer.stop();
            int totalMarks = 0;
            try (Connection con = DBConnection.getConnection()) {
                PreparedStatement ps = con.prepareStatement(
                    "SELECT SUM(s.marks_per_question) AS total FROM quiz_sections s " +
                    "JOIN questions q ON s.section_id = q.section_id WHERE q.quiz_id = ?"
                );
                ps.setInt(1, quizId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) totalMarks = rs.getInt("total");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Quiz Completed!\nYour Score is " + score + "/" + totalMarks);

            try (Connection con = DBConnection.getConnection()) {
                String sql = "INSERT INTO results(student_id, quiz_id, score) VALUES(?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, 1); // TODO: replace with logged-in student ID
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
