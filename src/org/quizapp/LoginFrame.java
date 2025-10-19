package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    JTextField txtUser;
    JPasswordField txtPass;
    JButton btnLogin;

    public LoginFrame() {
        setTitle("Online Quiz Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(240, 248, 255)); // soft blue background

        JLabel lblTitle = new JLabel("Online Quiz System", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBounds(80, 20, 250, 30);
        panel.add(lblTitle);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblUser.setBounds(60, 80, 100, 25);
        panel.add(lblUser);

        txtUser = new JTextField();
        txtUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUser.setBounds(160, 80, 180, 28);
        txtUser.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true));
        panel.add(txtUser);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPass.setBounds(60, 120, 100, 25);
        panel.add(lblPass);

        txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBounds(160, 120, 180, 28);
        txtPass.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true));
        panel.add(txtPass);

        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(70, 130, 180));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setBounds(140, 180, 120, 35);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> login());
        panel.add(btnLogin);

        add(panel);
        setVisible(true);
    }

    void login() {
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, txtUser.getText());
            ps.setString(2, new String(txtPass.getPassword()));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                JOptionPane.showMessageDialog(this, "Login successful as " + role);
                dispose();
                if (role.equals("teacher")) new TeacherFrame();
                else new StudentFrame();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
