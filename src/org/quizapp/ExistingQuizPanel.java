package org.quizapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ExistingQuizPanel extends JFrame {
    int teacherId;

    public ExistingQuizPanel(int teacherId) {
        this.teacherId = teacherId;

        setTitle("Existing Quizzes");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Top panel for Back button
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JButton btnBack = new JButton("Back");
        btnBack.setBackground(Color.GRAY);
        btnBack.setForeground(Color.WHITE);
        btnBack.addActionListener(e -> {
            dispose();
            new TeacherFrame();
        });
        topPanel.add(btnBack);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Quiz ID", "Title", "Duration (mins)", "Attended Students", "Average Score"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadQuizzes(model);
        setVisible(true);
    }

    void loadQuizzes(DefaultTableModel model) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT q.quiz_id, q.title, q.duration, COUNT(r.student_id) AS attended, AVG(r.score) AS avg_score " +
                         "FROM quiz q LEFT JOIN results r ON q.quiz_id = r.quiz_id " +
                         "WHERE q.created_by=? GROUP BY q.quiz_id";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, teacherId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int quizId = rs.getInt("quiz_id");
                String title = rs.getString("title");
                int duration = rs.getInt("duration");
                int attended = rs.getInt("attended");
                double avgScore = rs.getDouble("avg_score");
                model.addRow(new Object[]{quizId, title, duration, attended, avgScore});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading quizzes: " + e.getMessage());
        }
    }
}
