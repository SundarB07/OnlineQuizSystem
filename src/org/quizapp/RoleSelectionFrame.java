package org.quizapp;

import javax.swing.*;
import java.awt.*;

public class RoleSelectionFrame extends JFrame {

    public RoleSelectionFrame() {
        setTitle("Select Role");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel lblTitle = new JLabel("Select Your Role", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setBounds(50, 30, 300, 40);
        add(lblTitle);

        JButton btnTeacher = new JButton("Teacher");
        btnTeacher.setBounds(120, 90, 150, 40);
        btnTeacher.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnTeacher.setBackground(new Color(72, 132, 255));
        btnTeacher.setForeground(Color.WHITE);
        btnTeacher.setFocusPainted(false);
        btnTeacher.addActionListener(e -> {
            dispose();
            new LoginFrame("teacher");
        });
        add(btnTeacher);

        JButton btnStudent = new JButton("Student");
        btnStudent.setBounds(120, 150, 150, 40);
        btnStudent.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnStudent.setBackground(new Color(40, 167, 69));
        btnStudent.setForeground(Color.WHITE);
        btnStudent.setFocusPainted(false);
        btnStudent.addActionListener(e -> {
            dispose();
            new LoginFrame("student");
        });
        add(btnStudent);

        getContentPane().setBackground(new Color(240, 248, 255));
        setVisible(true);
    }
}
