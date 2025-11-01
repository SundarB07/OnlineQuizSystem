package org.quizapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * QuizTaker with section tabs and per-section one-question-at-a-time behavior.
 * Preserves original timer and DB save logic as much as possible.
 */
public class QuizTaker extends JFrame {

    // Top-level UI
    JLabel lblTimer;
    JButton btnNext;
    JButton btnSubmit;
    JCheckBox[] optionChecks = new JCheckBox[4];
    JLabel lblQuestion;
    JLabel lblSectionTitle;

    // Timer & quiz info
    javax.swing.Timer timer;
    int timeLeft = 0; // seconds
    int quizId;

    // Data structures
    // SectionData holds questions for one section and current index
    class Question {
        int qId;
        private int marks;
        public int getMarks() { return marks; }
        String questionText, optionA, optionB, optionC, optionD, correctOption; // correctOption != null -> single-answer
        Question(int id, int m, String q, String a, String b, String c, String d, String correct) {
            qId = id;
            marks = m;
            questionText = q;
            optionA = a;
            optionB = b;
            optionC = c;
            optionD = d;
            correctOption = correct;
        }
    }

    class SectionData {
        int sectionId;
        String sectionName;
        int marksPerQuestion;
        List<Question> questions = new ArrayList<>();
        List<JButton> questionButtons = new ArrayList<>();
        int currentIndex = 0; // which question currently viewing in this section
        // preserves answers: q_id -> set of selected options (A/B/C/D)
//        int currentQuestionIndex = 0;
        Map<Integer, Set<String>> answers = new HashMap<>();
        SectionData(int id, String name, int marks) {
            sectionId = id;
            sectionName = name;
            marksPerQuestion = marks;
        }
    }

    // Maintain ordered list of sections
    List<SectionData> sections = new ArrayList<>();

    // UI components for tabbed view
    JTabbedPane tabbedPane;

    public QuizTaker(int quizId) {
        this.quizId = quizId;

        setTitle("Quiz in Progress");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Show instructions first (preserve old behavior)
        showInstructionsAndStart();
    }

    void showInstructionsAndStart() {
        // Build instruction text using quiz_sections info
        StringBuilder info = new StringBuilder();
        info.append("📘 Quiz Instructions:\n\n");
        info.append("This quiz contains multiple sections.\n\n");

        // Build a quick sections overview from DB
        try (Connection con = DBConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                "SELECT section_name, marks_per_question FROM quiz_sections WHERE quiz_id = ? ORDER BY section_id"
            );
            ps.setInt(1, quizId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                info.append("• ").append(rs.getString("section_name"))
                    .append(" – ").append(rs.getInt("marks_per_question"))
                    .append(" mark(s) per question\n");
            }
        } catch (Exception e) {
            // ignore, still show basic info
        }

        info.append("\nClick OK to start the quiz.");

        // After OK -> initialize UI and load data & timer
        initUI();
        loadSectionsAndQuestions();
        if (sections.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sections/questions found for this quiz!");
            dispose();
            new StudentFrame();
            return;
        }
        setupTabs();
        loadQuizDurationAndStartTimer();
        setVisible(true);
    }

    void initUI() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        // Top panel: title + timer + submit
        JPanel topPanel = new JPanel(new BorderLayout());
        lblSectionTitle = new JLabel("", SwingConstants.LEFT);
        lblSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSectionTitle.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        topPanel.add(lblSectionTitle, BorderLayout.WEST);

        lblTimer = new JLabel("Time Left: 0:00", SwingConstants.RIGHT);
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTimer.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        topPanel.add(lblTimer, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center: TabbedPane for sections
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // Bottom panel: question label + options + controls
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(null);
        bottomPanel.setPreferredSize(new Dimension(800, 280));

        lblQuestion = new JLabel("", SwingConstants.LEFT);
        lblQuestion.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblQuestion.setBounds(20, 10, 740, 40);
        bottomPanel.add(lblQuestion);

        int y = 60;
        for (int i = 0; i < 4; i++) {
            optionChecks[i] = new JCheckBox();
            optionChecks[i].setBounds(40, y, 700, 30);
            optionChecks[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            optionChecks[i].setBackground(bottomPanel.getBackground());
            final int idx = i;
            optionChecks[i].addActionListener(ev -> {
                // update answer store for currently selected section & question
                storeCurrentSelectionsInModel();
                updateSubmitButtonState();
            });
            bottomPanel.add(optionChecks[i]);
            y += 40;
        }

        // Next button (moves to next question in the current section)
        btnNext = new JButton("Next");
        btnNext.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnNext.setBackground(new Color(72, 132, 255));
        btnNext.setForeground(Color.WHITE);
        btnNext.setBounds(300, 220, 120, 40);
        btnNext.addActionListener(e -> onNextClicked());
        bottomPanel.add(btnNext);

        // Submit button (disabled until all questions answered)
        btnSubmit = new JButton("Submit");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSubmit.setBackground(new Color(40, 167, 69));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBounds(440, 220, 120, 40);
        btnSubmit.setEnabled(false);
        btnSubmit.addActionListener(e -> onSubmitClicked());
        bottomPanel.add(btnSubmit);

        add(bottomPanel, BorderLayout.SOUTH);

        // Tab change listener to load appropriate question for selected section
     // Tab change listener to load appropriate question for selected section
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx >= 0 && idx < sections.size()) {
                SectionData sd = sections.get(idx);
                lblSectionTitle.setText(sd.sectionName);

                // find the section panel for this tab
                JPanel sectionPanel = (JPanel) tabbedPane.getComponentAt(idx);
                sectionPanel.removeAll();

                // create nav panel using the BUTTONS we already stored in sd.questionButtons
                JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

                // if questionButtons wasn't filled earlier (defensive), create and store them now
                if (sd.questionButtons == null || sd.questionButtons.isEmpty()) {
                    sd.questionButtons = new ArrayList<>();
                    for (int i = 0; i < sd.questions.size(); i++) {
                        JButton qBtnNew = new JButton("Q" + (i + 1));
                        qBtnNew.setMargin(new Insets(2, 8, 2, 8));
                        final int questionIndex = i;
                        qBtnNew.addActionListener(ev -> {
                            sd.currentIndex = questionIndex;
                            loadQuestionIntoUI(sd);
                        });
                        qBtnNew.setBackground(Color.LIGHT_GRAY);
                        sd.questionButtons.add(qBtnNew);
                    }
                }

                // add the stored buttons to the nav panel (reuse same objects)
                for (JButton storedBtn : sd.questionButtons) {
                    navPanel.add(storedBtn);
                }

                sectionPanel.add(navPanel, BorderLayout.NORTH);
                // show the current question in the UI area
                loadQuestionIntoUI(sd);
                sectionPanel.revalidate();
                sectionPanel.repaint();
            }
        });


    }

    void loadSectionsAndQuestions() {
        sections.clear();
        try (Connection con = DBConnection.getConnection()) {
            // 1) load sections for this quiz (ordered)
            PreparedStatement psSec = con.prepareStatement(
                "SELECT section_id, section_name, marks_per_question FROM quiz_sections WHERE quiz_id = ? ORDER BY section_id"
            );
            psSec.setInt(1, quizId);
            ResultSet rsSec = psSec.executeQuery();
            while (rsSec.next()) {
                SectionData sd = new SectionData(
                    rsSec.getInt("section_id"),
                    rsSec.getString("section_name"),
                    rsSec.getInt("marks_per_question")
                );
                sections.add(sd);
            }

            // 2) for each section, load its questions
            PreparedStatement psQ = con.prepareStatement(
                "SELECT q_id, question_text, optionA, optionB, optionC, optionD, correct_option, q.marks " +
                "FROM questions q WHERE q.section_id = ? ORDER BY q.q_id"
            );
            for (SectionData sd : sections) {
                psQ.setInt(1, sd.sectionId);
                ResultSet rsQ = psQ.executeQuery();
                while (rsQ.next()) {
                    Question q = new Question(
                        rsQ.getInt("q_id"),
                        rsQ.getInt("marks"),
                        rsQ.getString("question_text"),
                        rsQ.getString("optionA"),
                        rsQ.getString("optionB"),
                        rsQ.getString("optionC"),
                        rsQ.getString("optionD"),
                        rsQ.getString("correct_option")
                    );
                    sd.questions.add(q);
                }
            }

            // count total questions (for completion check)
            int totalQuestions = 0;
            for (SectionData sd : sections) totalQuestions += sd.questions.size();
            if (totalQuestions == 0) {
                // nothing to show
            }
         // After loading sections & questions, add navigation buttons for each section
            for (SectionData sd : sections) {
                JPanel sectionPanel = new JPanel(new BorderLayout());

                // Navigation panel: buttons for each question
                JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
                for (int i = 0; i < sd.questions.size(); i++) {
                    JButton qBtn = new JButton("Q" + (i + 1));
                    qBtn.setMargin(new Insets(2, 8, 2, 8));
                    qBtn.setBackground(Color.LIGHT_GRAY); // default: not answered
                    final int questionIndex = i;

                    qBtn.addActionListener(e -> {
                        sd.currentIndex = questionIndex;
                        loadQuestionIntoUI(sd);
                    });
                    sd.questionButtons.add(qBtn);
                    navPanel.add(qBtn);
                }

                sectionPanel.add(navPanel, BorderLayout.NORTH);
                tabbedPane.addTab(sd.sectionName, sectionPanel);
            }

            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading quiz data: " + e.getMessage());
        }
        
    }

    void setupTabs() {
        tabbedPane.removeAll();
        for (SectionData sd : sections) {
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            // We'll use the central UI (lblQuestion + checkboxes) for all tabs, so panel can be empty or contain info.
            // But to show something sensible on tab area, put a placeholder:
            JLabel place = new JLabel("Section: " + sd.sectionName + "  (Questions: " + sd.questions.size() + ")", SwingConstants.CENTER);
            place.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            panel.add(place, BorderLayout.CENTER);
            tabbedPane.addTab(sd.sectionName, panel);
        }
        // select first tab
        if (!sections.isEmpty()) {
            tabbedPane.setSelectedIndex(0);
            lblSectionTitle.setText(sections.get(0).sectionName);
            loadQuestionIntoUI(sections.get(0));
        }
        updateSubmitButtonState();
    }

    void loadQuizDurationAndStartTimer() {
        // same as before: get duration from quiz table
        try (Connection con = DBConnection.getConnection()) {
            Statement st = con.createStatement();
            ResultSet rsQuiz = st.executeQuery("SELECT duration FROM quiz WHERE quiz_id=" + quizId);
            int quizDuration = 0;
            if (rsQuiz.next()) quizDuration = rsQuiz.getInt("duration");
            timeLeft = quizDuration * 60;
        } catch (Exception e) {
            e.printStackTrace();
            timeLeft = 0;
        }
        startTimer();
    }

    void loadQuestionIntoUI(SectionData sd) {
        // ensure currentIndex within bounds
        if (sd.questions.isEmpty()) {
            lblQuestion.setText("No questions in this section.");
            for (int i = 0; i < 4; i++) {
                optionChecks[i].setText("");
                optionChecks[i].setSelected(false);
                optionChecks[i].setEnabled(false);
            }
            btnNext.setEnabled(false);
            return;
        }

        if (sd.currentIndex < 0) sd.currentIndex = 0;
        if (sd.currentIndex >= sd.questions.size()) sd.currentIndex = sd.questions.size() - 1;

        Question q = sd.questions.get(sd.currentIndex);
        lblQuestion.setText("Q" + (sd.currentIndex + 1) + ": " + q.questionText);

        optionChecks[0].setText(q.optionA != null ? "A. " + q.optionA : "A.");
        optionChecks[1].setText(q.optionB != null ? "B. " + q.optionB : "B.");
        optionChecks[2].setText(q.optionC != null ? "C. " + q.optionC : "C.");
        optionChecks[3].setText(q.optionD != null ? "D. " + q.optionD : "D.");

        // enable checkboxes
        for (int i = 0; i < 4; i++) {
            optionChecks[i].setEnabled(true);
        }

        // load previously selected answers for this question, if any
        Set<String> saved = sd.answers.get(q.qId);
        for (int i = 0; i < 4; i++) optionChecks[i].setSelected(false);
        if (saved != null) {
            if (saved.contains("A")) optionChecks[0].setSelected(true);
            if (saved.contains("B")) optionChecks[1].setSelected(true);
            if (saved.contains("C")) optionChecks[2].setSelected(true);
            if (saved.contains("D")) optionChecks[3].setSelected(true);
        }

        // Next button enabled unless this is the last question of the last section.
        btnNext.setEnabled(true);
    }

    void storeCurrentSelectionsInModel() {
        int si = tabbedPane.getSelectedIndex();
        if (si < 0 || si >= sections.size()) return;
        SectionData sd = sections.get(si);
        if (sd.questions.isEmpty()) return;
        Question q = sd.questions.get(sd.currentIndex);

        Set<String> sel = new HashSet<>();
        if (optionChecks[0].isSelected()) sel.add("A");
        if (optionChecks[1].isSelected()) sel.add("B");
        if (optionChecks[2].isSelected()) sel.add("C");
        if (optionChecks[3].isSelected()) sel.add("D");

        if (sel.isEmpty()) {
            sd.answers.remove(q.qId);
        } else {
            sd.answers.put(q.qId, sel);
        }

        // Update the color of that question button
        if (sd.currentIndex < sd.questionButtons.size()) {
            JButton qBtn = sd.questionButtons.get(sd.currentIndex);
            if (sel.isEmpty()) {
                qBtn.setBackground(Color.LIGHT_GRAY); // not answered
            } else {
                qBtn.setBackground(new Color(102, 255, 102)); // green for answered
            }
        }
    }


    void onNextClicked() {
        // save current selections
        storeCurrentSelectionsInModel();
        updateSubmitButtonState();

        int si = tabbedPane.getSelectedIndex();
        if (si < 0 || si >= sections.size()) return;
        SectionData sd = sections.get(si);

        // move to next question in this section
        if (sd.currentIndex < sd.questions.size() - 1) {
            sd.currentIndex++;
            loadQuestionIntoUI(sd);
            return;
        } else {
            // finished this section: automatically move to next section (if exists)
            if (si < sections.size() - 1) {
                tabbedPane.setSelectedIndex(si + 1);
                // when tab change listener fires, it will load the question
                return;
            } else {
                // This is last question of last section. If all answered, enable submit (should already be enabled).
                updateSubmitButtonState();
                JOptionPane.showMessageDialog(this, "You reached the last question of the quiz. Click Submit to finish.");
            }
        }
    }

    void updateSubmitButtonState() {
        // Check that every question across all sections has at least one selected answer
        for (SectionData sd : sections) {
            for (Question q : sd.questions) {
                Set<String> s = sd.answers.get(q.qId);
                if (s == null || s.isEmpty()) {
                    btnSubmit.setEnabled(false);
                    return;
                }
            }
        }
        // All questions answered
        btnSubmit.setEnabled(true);
    }

    void onSubmitClicked() {
        // Save the currently shown selections first
        storeCurrentSelectionsInModel();

        // verify again
        for (SectionData sd : sections) {
            for (Question q : sd.questions) {
                Set<String> s = sd.answers.get(q.qId);
                if (s == null || s.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please answer all questions before submitting!");
                    return;
                }
            }
        }

        // stop timer
        if (timer != null) timer.stop();

        // Calculate score similar to your existing logic
        int score = 0;
        int totalMarks = 0;

        try (Connection con = DBConnection.getConnection()) {
            // compute total marks using quiz_sections as before
            PreparedStatement psTotal = con.prepareStatement(
                "SELECT SUM(s.marks_per_question) AS total FROM quiz_sections s " +
                "JOIN questions q ON s.section_id = q.section_id WHERE q.quiz_id = ?"
            );
            psTotal.setInt(1, quizId);
            ResultSet rsTot = psTotal.executeQuery();
            if (rsTot.next()) totalMarks = rsTot.getInt("total");

            // For each question, compute marks per question (from quiz_sections)
            PreparedStatement psMarks = con.prepareStatement(
                "SELECT s.marks_per_question FROM quiz_sections s JOIN questions q ON s.section_id = q.section_id WHERE q.q_id = ?"
            );

            PreparedStatement psMulti = con.prepareStatement(
                "SELECT correct_option FROM question_answers WHERE q_id=?"
            );

            for (SectionData sd : sections) {
                for (Question q : sd.questions) {
                    // get marks per question
                    int marksPerQ = 1;
                    psMarks.setInt(1, q.qId);
                    ResultSet rsM = psMarks.executeQuery();
                    if (rsM.next()) marksPerQ = rsM.getInt("marks_per_question");

                    Set<String> userSelected = sd.answers.get(q.qId);
                    if (userSelected == null) userSelected = new HashSet<>();

                    if (q.correctOption != null && !q.correctOption.trim().isEmpty()) {
                        // single answer question
                        if (userSelected.size() == 1 && userSelected.contains(q.correctOption.toUpperCase())) {
                            score += marksPerQ;
                        }
                    } else {
                        // multi-answer question: fetch correct options set
                        psMulti.setInt(1, q.qId);
                        ResultSet rsAns = psMulti.executeQuery();
                        Set<String> correctSet = new HashSet<>();
                        while (rsAns.next()) {
                            correctSet.add(rsAns.getString("correct_option").toUpperCase());
                        }
                        if (userSelected.equals(correctSet)) {
                            score += marksPerQ;
                        }
                    }
                }
            }

            // Save result (same as before; student_id currently hardcoded to 1)
            PreparedStatement psSave = con.prepareStatement(
                "INSERT INTO results(student_id, quiz_id, score) VALUES(?, ?, ?)"
            );
            psSave.setInt(1, 1); // TODO: replace with logged-in student ID
            psSave.setInt(2, quizId);
            psSave.setInt(3, score);
            psSave.executeUpdate();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while submitting: " + ex.getMessage());
            return;
        }

        JOptionPane.showMessageDialog(this, "Quiz Completed!\nYour Score is " + score + "/" + totalMarks);
        dispose();
        new StudentFrame();
    }

    void startTimer() {
        if (timer != null) timer.stop();
        timer = new Timer(1000, new ActionListener() {
            int remaining = timeLeft;

            @Override
            public void actionPerformed(ActionEvent e) {
                int mins = remaining / 60;
                int secs = remaining % 60;
                lblTimer.setText(String.format("Time Left: %d:%02d", mins, secs));
                remaining--;
                if (remaining < 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(null, "Time's up!");
                    dispose();
                    new StudentFrame();
                }
            }
        });
        timer.start();
    }
}
