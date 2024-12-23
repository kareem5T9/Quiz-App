package com.mycompany.quizapplication1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class QuizApplication1 {
    private static Connection connection;
    private static User loggedInUser;
    private static JFrame frame;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;
    private static JTextArea quizListArea;

    public static void main(String[] args) {
        try {
            // Initialize the database connection
            connection = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=quiz_application;encrypt=false;trustServerCertificate=true;user=sa;password=ahmed362004");

            SwingUtilities.invokeLater(() -> {
                frame = new JFrame("Quiz Application");
                cardLayout = new CardLayout();
                cardPanel = new JPanel(cardLayout);

                // Add different panels to the card layout
                cardPanel.add(createWelcomePanel(), "Welcome");
                cardPanel.add(createRegisterPanel(), "Register");
                cardPanel.add(createLoginPanel(), "Login");
                cardPanel.add(createStudentPanel(), "Student");
                cardPanel.add(createTeacherPanel(), "Teacher");

                frame.add(cardPanel);
                frame.setSize(1000, 600);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection error: " + e.getMessage());
        }
    }

    // Create and initialize the panels (Welcome, Register, Login, etc.)

    private static JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton registerButton = new JButton("Register");
        JButton loginButton = new JButton("Login");

        registerButton.addActionListener(e -> cardLayout.show(cardPanel, "Register"));
        loginButton.addActionListener(e -> cardLayout.show(cardPanel, "Login"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(registerButton);
        buttonPanel.add(loginButton);
        panel.add(new JLabel("Welcome to Quiz Application", JLabel.CENTER), BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER);

        return panel;
    }

    private static JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(5, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> userTypeBox = new JComboBox<>(new String[]{"student", "teacher"});
        JButton registerButton = new JButton("Register");

        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String userType = (String) userTypeBox.getSelectedItem();

            if (registerUser(username, password, userType)) {
                JOptionPane.showMessageDialog(frame, "Registration successful.");
                cardLayout.show(cardPanel, "Welcome");
            } else {
                JOptionPane.showMessageDialog(frame, "Registration failed. User may already exist.");
            }
        });

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("User Type:"));
        panel.add(userTypeBox);
        panel.add(new JLabel());
        panel.add(registerButton);

        return panel;
    }

    private static boolean registerUser(String username, String password, String userType) {
        try {
            String query = "INSERT INTO users (username, password, user_type) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.setString(3, userType);
                int rowsInserted = stmt.executeUpdate();
                return rowsInserted > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            loggedInUser = authenticateUser(username, password);
            if (loggedInUser != null) {
                if (loggedInUser.getUserType().equals("student")) {
                    cardLayout.show(cardPanel, "Student");
                } else {
                    cardLayout.show(cardPanel, "Teacher");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password.");
            }
        });

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);

        return panel;
    }

    private static User authenticateUser(String username, String password) {
        try {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new User(rs.getString("username"), rs.getString("password"), rs.getString("user_type"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JPanel createStudentPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    quizListArea = new JTextArea(10, 30);
    quizListArea.setEditable(false);

    JButton startQuizButton = new JButton("Start Quiz");
    startQuizButton.addActionListener(e -> {
        String quizName = JOptionPane.showInputDialog(frame, "Enter quiz name to take:");
        if (quizExists(quizName)) {
            takeQuiz(quizName);
        } else {
            JOptionPane.showMessageDialog(frame, "Quiz not found.");
        }
    });

    JButton logoutButton = new JButton("Logout");
    logoutButton.addActionListener(e -> {
        loggedInUser = null;
        cardLayout.show(cardPanel, "Welcome");
    });

    panel.add(new JLabel("Available Quizzes:", JLabel.CENTER), BorderLayout.NORTH);
    panel.add(new JScrollPane(quizListArea), BorderLayout.CENTER);
    JPanel bottomPanel = new JPanel();
    bottomPanel.add(startQuizButton);
    bottomPanel.add(logoutButton);
    panel.add(bottomPanel, BorderLayout.SOUTH);

    refreshQuizList(); // Call this method to refresh the quiz list

    return panel;
}

    private static boolean quizExists(String quizName) {
        try {
            String query = "SELECT * FROM quizzes WHERE name = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, quizName);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void refreshQuizList() {
    quizListArea.setText(""); // Clear the existing list
    try {
        String query = "SELECT * FROM quizzes";  // Get all available quizzes
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                quizListArea.append(rs.getString("name") + "\n");  // Display quiz names
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    private static void takeQuiz(String quizName) {
        try {
            String query = "SELECT * FROM questions WHERE quiz_id = (SELECT quiz_id FROM quizzes WHERE name = ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, quizName);
                ResultSet rs = stmt.executeQuery();
                int score = 0;
                while (rs.next()) {
                    String questionText = rs.getString("question_text");
                    String[] options = new String[] {
                        rs.getString("option1"),
                        rs.getString("option2"),
                        rs.getString("option3"),
                        rs.getString("option4")
                    };
                    int correctOption = rs.getInt("correct_option");
                    String answer = JOptionPane.showInputDialog(frame, questionText +
                            "\n1. " + options[0] +
                            "\n2. " + options[1] +
                            "\n3. " + options[2] +
                            "\n4. " + options[3]);

                    if (Integer.parseInt(answer) == correctOption) {
                        score++;
                    }
                }
                saveStudentResult(quizName, score);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void saveStudentResult(String quizName, int score) {
        try {
            String query = "INSERT INTO results (username, quiz_name, score) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, loggedInUser.getUsername());
                stmt.setString(2, quizName);
                stmt.setInt(3, score);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(frame, "Your score: " + score);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static JPanel createTeacherPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JButton createQuizButton = new JButton("Create Quiz");
        JButton logoutButton = new JButton("Logout");

        createQuizButton.addActionListener(e -> createQuiz());
        logoutButton.addActionListener(e -> {
            loggedInUser = null;
            cardLayout.show(cardPanel, "Welcome");
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(createQuizButton);
        bottomPanel.add(logoutButton);
        panel.add(bottomPanel, BorderLayout.CENTER);
        return panel;
    }

    private static void createQuiz() {
    String quizName = JOptionPane.showInputDialog(frame, "Enter quiz name:");
    if (quizName != null) {
        try {
            // Step 1: Insert the quiz name into the quizzes table
            String query = "INSERT INTO quizzes (name) VALUES (?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, quizName);
                stmt.executeUpdate();

                // Step 2: Retrieve the generated quiz ID
                ResultSet rs = stmt.getGeneratedKeys();
                int quizId = -1;
                if (rs.next()) {
                    quizId = rs.getInt(1);
                }

                // Step 3: Prompt the teacher to add questions
                boolean addingQuestions = true;
                while (addingQuestions) {
                    String questionText = JOptionPane.showInputDialog(frame, "Enter question:");
                    if (questionText == null || questionText.isEmpty()) {
                        addingQuestions = false; // Exit if the teacher cancels or leaves empty
                        continue;
                    }

                    String option1 = JOptionPane.showInputDialog(frame, "Enter option 1:");
                    String option2 = JOptionPane.showInputDialog(frame, "Enter option 2:");
                    String option3 = JOptionPane.showInputDialog(frame, "Enter option 3:");
                    String option4 = JOptionPane.showInputDialog(frame, "Enter option 4:");

                    String correctOptionString = JOptionPane.showInputDialog(frame, "Enter correct option number (1-4):");
                    int correctOption = Integer.parseInt(correctOptionString);

                    // Step 4: Insert the question and its options into the database
                    String questionQuery = "INSERT INTO questions (quiz_id, question_text, option1, option2, option3, option4, correct_option) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement questionStmt = connection.prepareStatement(questionQuery)) {
                        questionStmt.setInt(1, quizId); // Link the question to the quiz
                        questionStmt.setString(2, questionText);
                        questionStmt.setString(3, option1);
                        questionStmt.setString(4, option2);
                        questionStmt.setString(5, option3);
                        questionStmt.setString(6, option4);
                        questionStmt.setInt(7, correctOption);
                        questionStmt.executeUpdate();
                    }

                    // Ask if the teacher wants to add another question
                    int response = JOptionPane.showConfirmDialog(frame, "Do you want to add another question?", "Add Question", JOptionPane.YES_NO_OPTION);
                    if (response != JOptionPane.YES_OPTION) {
                        addingQuestions = false; // Exit the loop if the teacher is done
                    }
                }

                JOptionPane.showMessageDialog(frame, "Quiz and questions created successfully.");

                // Step 5: Refresh the quiz list after creating a new quiz
                refreshQuizList();  // Refresh quiz list after a new quiz is created
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error while creating quiz: " + e.getMessage());
        }
    }
}

    // User class (can be a nested class)
    static class User {
        private String username;
        private String password;
        private String userType;

        public User(String username, String password, String userType) {
            this.username = username;
            this.password = password;
            this.userType = userType;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getUserType() {
            return userType;
        }
    }
}