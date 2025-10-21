package org.quizapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentFrame extends JFrame {

    JTable table;
    DefaultTableModel model;

    public StudentFrame() {
        setTitle("Student Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Available Quizzes", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(lblTitle, BorderLayout.NORTH);

        // Table for quizzes
        String[] columns = {"Quiz ID", "Title", "Duration (mins)"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnStart = new JButton("Start Quiz");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnStart.setBackground(new Color(46, 140, 90));
        btnStart.setForeground(Color.WHITE);
        btnStart.addActionListener(e -> startSelectedQuiz());
        add(btnStart, BorderLayout.SOUTH);

        loadQuizzes();

        setVisible(true);
    }

    void loadQuizzes() {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT quiz_id, title, duration FROM quiz";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("quiz_id"),
                        rs.getString("title"),
                        rs.getInt("duration")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading quizzes: " + e.getMessage());
        }
    }

    void startSelectedQuiz() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a quiz to start.");
            return;
        }
        int quizId = (int) model.getValueAt(row, 0);
        dispose();
        new QuizTaker(quizId); // pass selected quiz
    }
}
