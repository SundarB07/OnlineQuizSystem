package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    JTextField txtUser;
    JPasswordField txtPass;
    JButton btnLogin;
    String role;

    public LoginFrame(String role) {
        this.role = role;
        setTitle(role.substring(0,1).toUpperCase() + role.substring(1) + " Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(240, 248, 255));

        JLabel lblTitle = new JLabel(role.substring(0,1).toUpperCase() + role.substring(1) + " Login", SwingConstants.CENTER);
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
        panel.add(txtUser);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblPass.setBounds(60, 120, 100, 25);
        panel.add(lblPass);

        txtPass = new JPasswordField();
        txtPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPass.setBounds(160, 120, 180, 28);
        panel.add(txtPass);

        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(70, 130, 180));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBounds(140, 180, 120, 35);
        btnLogin.addActionListener(e -> login());
        panel.add(btnLogin);

        add(panel);
        setVisible(true);
    }

    void login() {
        try (Connection con = DBConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username=? AND password=? AND role=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, txtUser.getText());
            ps.setString(2, new String(txtPass.getPassword()));
            ps.setString(3, role);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                dispose();
                if (role.equals("teacher")) new TeacherFrame();
                else new StudentFrame();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password for " + role);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
