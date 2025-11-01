package org.quizapp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentFrame extends JFrame {
    JTable table; 
    int studentId;

    public StudentFrame() {
        this(1); // temporary default until login connection done
    }

    public StudentFrame(int studentId) {
        this.studentId = studentId;

        setTitle("Student Portal");
        setSize(800, 600);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ====== Top Panel with Logout ======
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("Available Quizzes", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(220, 53, 69)); // soft red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setContentAreaFilled(true);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            dispose();
            new RoleSelectionFrame();
        });


        topPanel.add(lblTitle, BorderLayout.CENTER);
        topPanel.add(btnLogout, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // ====== Table ======
     // Table setup (non-editable)
        DefaultTableModel model = new DefaultTableModel(new String[]{"Quiz ID", "Quiz Name", "Duration (min)"}, 0) {
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
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // Quiz ID small
        table.getColumnModel().getColumn(1).setPreferredWidth(250); // Quiz Name wide
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Duration small

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBounds(50, 80, 600, 300);
        add(scrollPane);

        // ====== Start Quiz Button ======
        JButton btnStart = new JButton("Start Quiz");
        btnStart.setBackground(new Color(72, 132, 255));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnStart.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a quiz to start!");
                return;
            }

            int quizId = (int) table.getValueAt(selectedRow, 0);
            try (Connection con = DBConnection.getConnection()) {
                String secSql = "SELECT section_name, marks_per_question FROM quiz_sections WHERE quiz_id = ?";
                PreparedStatement ps = con.prepareStatement(secSql);
                ps.setInt(1, quizId);
                ResultSet rs = ps.executeQuery();

                StringBuilder info = new StringBuilder("📘 Quiz Instructions:\n\n");
                info.append("This quiz contains multiple sections.\n\n");

                while (rs.next()) {
                    info.append("• ")
                        .append(rs.getString("section_name"))
                        .append(" – ")
                        .append(rs.getInt("marks_per_question"))
                        .append(" mark(s) per question\n");
                }

                info.append("\nClick OK to start the quiz.");
                JOptionPane.showMessageDialog(this, info.toString(), "Quiz Instructions", JOptionPane.INFORMATION_MESSAGE);

                dispose();
                new QuizTaker(quizId);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading quiz info: " + ex.getMessage());
            }

        });

        add(btnStart, BorderLayout.SOUTH);
        loadQuizzes(model);
        setVisible(true);
    }

    void loadQuizzes(DefaultTableModel model) {
        try (Connection con = DBConnection.getConnection()) {
            String sql = "SELECT quiz_id, title, duration FROM quiz";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("quiz_id");
                String title = rs.getString("title");
                int duration = rs.getInt("duration");
                model.addRow(new Object[]{id, title, duration});
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading quizzes: " + e.getMessage());
        }
    }
}
