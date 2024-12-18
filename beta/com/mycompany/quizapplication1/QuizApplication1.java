package com.mycompany.quizapplication1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

@SuppressWarnings("unused")
public class QuizApplication1 {
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, List<Question>> quizzes = new HashMap<>();
    private static Map<String, Integer> studentResults = new HashMap<>();
    private static User loggedInUser;
    private static JFrame frame;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;
    private static JTextArea quizListArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Quiz Application");
            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);

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
    }

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

            if (users.containsKey(username)) {
                JOptionPane.showMessageDialog(frame, "User already exists.");
            } else {
                users.put(username, new User(username, password, userType));
                JOptionPane.showMessageDialog(frame, "Registration successful.");
                cardLayout.show(cardPanel, "Welcome");
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

    private static JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (users.containsKey(username) && users.get(username).getPassword().equals(password)) {
                loggedInUser = users.get(username);
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
    
    private static void refreshQuizList(JTextArea quizListArea) {
    quizListArea.setText("");  // Clear the area before updating

    if (quizzes.isEmpty()) {
        quizListArea.append("No quizzes available.\n");
    } else {
        // Display each quiz name in the text area
        for (String quizName : quizzes.keySet()) {
            quizListArea.append(quizName + "\n");
        }
    }
}

    private static JPanel createStudentPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    quizListArea = new JTextArea(10, 30); // Store reference to the quiz list area
    quizListArea.setEditable(false);

    JButton startQuizButton = new JButton("Start Quiz");
    startQuizButton.addActionListener(e -> {
        String quizName = JOptionPane.showInputDialog(frame, "Enter quiz name to take:");
        if (quizzes.containsKey(quizName)) {
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

    refreshQuizList(quizListArea); // Initial population of quiz list

    return panel;
}

    private static JPanel createTeacherPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    JButton createQuizButton = new JButton("Create Quiz");
    createQuizButton.addActionListener(e -> {
    String quizName = JOptionPane.showInputDialog(frame, "Enter quiz name:");
    List<Question> questions = new ArrayList<>();

    while (true) {
        String questionText = JOptionPane.showInputDialog(frame, "Enter question:");
        String[] options = new String[4];
        for (int i = 0; i < 4; i++) {
            options[i] = JOptionPane.showInputDialog(frame, "Enter option " + (i + 1) + ":");
        }
        int correctOption = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter correct option (1-4):"));
        questions.add(new Question(questionText, options, correctOption));

        int option = JOptionPane.showConfirmDialog(frame, "Add another question?", "Add Question", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.NO_OPTION) break;
    }

    // Add quiz to the quizzes map
    quizzes.put(quizName, questions);

    // Show confirmation
    JOptionPane.showMessageDialog(frame, "Quiz created successfully.");

    // Refresh the quiz list in the student panel
    refreshQuizList(quizListArea);
});

    JButton logoutButton = new JButton("Logout");
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

    private static void takeQuiz(String quizName) {
    List<Question> questions = quizzes.get(quizName);
    int score = 0;

    for (Question question : questions) {
        String answer = JOptionPane.showInputDialog(frame, question.getQuestion() +
                "\n1. " + question.getOptions()[0] +
                "\n2. " + question.getOptions()[1] +
                "\n3. " + question.getOptions()[2] +
                "\n4. " + question.getOptions()[3] +
                "\nEnter your choice (1-4):");
        int userChoice = Integer.parseInt(answer);
        if (userChoice == question.getCorrectOption()) {
            score++;
        }
    }

    // Store the result for the student
    studentResults.put(loggedInUser.getUsername() + "_" + quizName, score);

    // Show the result to the student
    String resultMessage = "Quiz completed! Your score: " + score + "/" + questions.size() +
                           "\n\nThank you for participating!";
    JOptionPane.showMessageDialog(frame, resultMessage, "Quiz Results", JOptionPane.INFORMATION_MESSAGE);
}

    static class Question {
        private String question;
        private String[] options;
        private int correctOption;

        public Question(String question, String[] options, int correctOption) {
            this.question = question;
            this.options = options;
            this.correctOption = correctOption;
        }

        public String getQuestion() {
            return question;
        }

        public String[] getOptions() {
            return options;
        }

        public int getCorrectOption() {
            return correctOption;
        }
    }

    static class User {
        private String username;
        private String password;
        private String userType;

        public User(String username, String password, String userType) {
            this.username = username;
            this.password = password;
            this.userType = userType;
        }

        public String getPassword() {
            return password;
        }

        public String getUserType() {
            return userType;
        }
        
        public String getUsername() {
    return username;
}

    }
}