package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class QuizCreationPanel extends JFrame {
    int teacherId;
    JTextField txtQuizTitle, txtDuration;
    JTextField txtNumSections;
    JButton btnAddSections;
    JPanel panelSections;
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
        
        JLabel lblNumSections = new JLabel("No. of Sections:");
        lblNumSections.setBounds(100, 150, 120, 25);
        add(lblNumSections);

        txtNumSections = new JTextField();
        txtNumSections.setBounds(220, 150, 50, 28);
        add(txtNumSections);
        
        panelSections = new JPanel();
        panelSections.setLayout(new GridLayout(0, 4, 10, 10));
        panelSections.setBounds(100, 200, 500, 120);
        add(panelSections);
        
        txtNumSections.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = txtNumSections.getText().trim();
                if (!text.isEmpty()) {
                    try {
                        int n = Integer.parseInt(text);
                        panelSections.removeAll();
                        for (int i = 1; i <= n; i++) {
                            char sectionChar = (char) ('A' + i - 1);
                            panelSections.add(new JLabel("Section " + sectionChar + ":"));
                            JTextField txtSectionName = new JTextField("Section " + sectionChar);
                            txtSectionName.setEditable(false);
                            panelSections.add(txtSectionName);
                            panelSections.add(new JLabel("Marks per Q:"));
                            panelSections.add(new JTextField());
                        }
                        panelSections.revalidate();
                        panelSections.repaint();
                    } catch (NumberFormatException ex) {
                        // ignore invalid input
                    }
                }
                panelSections.revalidate();
                panelSections.repaint();
                getContentPane().revalidate();
                getContentPane().repaint();

            }
        });
        

        JButton btnCreate = new JButton("Create Quiz");
        btnCreate.setBounds(400, 100, 150, 30);
        btnCreate.addActionListener(e -> createQuiz());
        add(btnCreate);

        JButton btnBack = new JButton("Back");
        btnBack.setBounds(50, 320, 100, 30);
        btnBack.setBackground(Color.GRAY);
        btnBack.setForeground(Color.WHITE);
        btnBack.addActionListener(e -> {
            dispose();
            new TeacherFrame();
        });
        add(btnBack);

        setVisible(true);
        revalidate();
        repaint();

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

            if (quizId != -1) {
                for (int i = 0; i < panelSections.getComponentCount(); i += 4) {
                    JLabel lblName = (JLabel) panelSections.getComponent(i);
                    JTextField txtName = (JTextField) panelSections.getComponent(i + 1);
                    JLabel lblMarks = (JLabel) panelSections.getComponent(i + 2);
                    JTextField txtMarks = (JTextField) panelSections.getComponent(i + 3);

                    String sectionName = txtName.getText();
                    int marks = Integer.parseInt(txtMarks.getText());

                    String secSQL = "INSERT INTO quiz_sections (quiz_id, section_name, marks_per_question) VALUES (?, ?, ?)";
                    PreparedStatement ps2 = con.prepareStatement(secSQL);
                    ps2.setInt(1, quizId);
                    ps2.setString(2, sectionName);
                    ps2.setInt(3, marks);
                    ps2.executeUpdate();
                }
            }

            if (followQuestionInput && quizId != -1) {
                dispose();
                new QuestionInputPanel(quizId); // go to question input
            } else {
                JOptionPane.showMessageDialog(this, "Quiz created successfully!\nQuiz ID: " + quizId);
            }

        } catch (SQLIntegrityConstraintViolationException dupEx) {
            JOptionPane.showMessageDialog(this,
                "Quiz title already exists! Please choose a different name.",
                "Duplicate Title",
                JOptionPane.WARNING_MESSAGE);
        } catch (SQLException sqle) {
            JOptionPane.showMessageDialog(this,
                "Database error: " + sqle.getMessage(),
                "SQL Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Unexpected error: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }


}
