package org.quizapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
     // Table setup (non-editable)
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Quiz ID", "Title", "Duration (mins)", "Attended Students", "Average Score"},
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // make table non-editable
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(100, 149, 237));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setSelectionForeground(Color.BLACK);

        // Adjust column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);   // Quiz ID
        table.getColumnModel().getColumn(1).setPreferredWidth(220);  // Title
        table.getColumnModel().getColumn(2).setPreferredWidth(90);   // Duration
        table.getColumnModel().getColumn(3).setPreferredWidth(150);  // Attended Students
        table.getColumnModel().getColumn(4).setPreferredWidth(130);  // Avg Score

        // Center-align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);


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
