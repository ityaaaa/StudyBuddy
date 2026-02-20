import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

class StudyBuddyGUI extends JFrame {
    private int currentSeconds = 0;
    private boolean isPomodoroMode = false;
    private Timer timer;
    private JLabel timeLabel;
    private JTextField taskField;
    private FixedSizeImagePanel imagePanel; 
    private ComicBubbleLabel textBubble;
    private final Color BG_COLOR = new Color(255, 229, 217); 
    private final Color ACCENT_COLOR = new Color(50, 50, 50); 
    
    public StudyBuddyGUI() {
        super("Study Buddy");
        setSize(550, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        initHeader();
        initImageAndBubbleSection(); 
        JPanel buttonPanel = initButtons();
        add(buttonPanel, BorderLayout.SOUTH);
        timer = new Timer(1000, e -> onTimerTick());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initHeader() {
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        topPanel.setBackground(BG_COLOR);
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(BG_COLOR);
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2), 
                "Current Task", 
                TitledBorder.CENTER, 
                TitledBorder.TOP, 
                new Font("Comic Sans MS", Font.BOLD, 14)
        ));
        
        taskField = new JTextField(20);
        taskField.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        taskField.setHorizontalAlignment(JTextField.CENTER);
        taskField.setBorder(null);
        taskField.setBackground(BG_COLOR);
        inputPanel.add(taskField);
        timeLabel = new JLabel("00:00:00", JLabel.CENTER);
        timeLabel.setFont(new Font("Dialog", Font.BOLD, 50));
        timeLabel.setForeground(ACCENT_COLOR);
        topPanel.add(inputPanel);
        topPanel.add(timeLabel);
        add(topPanel, BorderLayout.NORTH);
    }

    private void initImageAndBubbleSection() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(BG_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        imagePanel = new FixedSizeImagePanel(300, 300); 
        imagePanel.setImagePath("snoopy1.gif"); 
        imagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        textBubble = new ComicBubbleLabel("Hi! I'm Snoopy! Ready to work?");
        textBubble.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(imagePanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20))); 
        centerPanel.add(textBubble);
        
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel initButtons() {
        JButton startBtn = createComicButton("Start", new Color(180, 240, 210));
        JButton stopBtn = createComicButton("Stop", new Color(255, 190, 190));
        JButton resetBtn = createComicButton("Reset", new Color(255, 245, 180));
        JButton pomodoroBtn = createComicButton("Pomodoro", new Color(255, 120, 120));
        JButton saveBtn = createComicButton("Save", new Color(180, 230, 255));
        JButton historyBtn = createComicButton("History", new Color(230, 230, 230));
        
        pomodoroBtn.setForeground(Color.BLACK);

        startBtn.addActionListener(e -> startAction());
        stopBtn.addActionListener(e -> stopAction());
        resetBtn.addActionListener(e -> resetAction());
        pomodoroBtn.addActionListener(e -> pomodoroAction());
        saveBtn.addActionListener(e -> saveAction());
        historyBtn.addActionListener(e -> showHistoryAction());

        JPanel btnPanel = new JPanel(new GridLayout(2, 3, 12, 12));
        btnPanel.setBackground(BG_COLOR);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        btnPanel.add(startBtn);
        btnPanel.add(stopBtn);
        btnPanel.add(resetBtn);
        btnPanel.add(pomodoroBtn);
        btnPanel.add(saveBtn);
        btnPanel.add(historyBtn);
        
        return btnPanel;
    }

    private void onTimerTick() {
        if (isPomodoroMode) {
            if (currentSeconds > 0) {
                currentSeconds--;
            } else {
                timer.stop();
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, "Pomodoro Complete! Time for a break.");
                
                imagePanel.setImagePath("snoopy3.gif");
                textBubble.setText("Good Job! Take a rest.");
                taskField.setEditable(true);
            }
        } else {
            currentSeconds++;
        }
        updateTimerLabel();
    }

    private void startAction() {
        if (!timer.isRunning()) {
            imagePanel.setImagePath("snoopy2.gif");
            textBubble.setText(isPomodoroMode ? "Pomodoro Focus Mode!" : "Studying... Don't distract me!");
            taskField.setEditable(false);
            timer.start();
        }
    }

    private void stopAction() {
        if (timer.isRunning()) {
            imagePanel.setImagePath("snoopy3.gif");
            textBubble.setText("Paused. zzz...");
            timer.stop();
        }
    }

    private void resetAction() {
        timer.stop();
        isPomodoroMode = false;
        currentSeconds = 0;
        updateTimerLabel();
        taskField.setEditable(true);
        timeLabel.setForeground(ACCENT_COLOR);
        
        imagePanel.setImagePath("snoopy1.gif");
        textBubble.setText("Reset! What's next?");
    }

    private void pomodoroAction() {
        timer.stop();
        isPomodoroMode = true;
        currentSeconds = 25 * 60;
        updateTimerLabel();
        timeLabel.setForeground(new Color(200, 50, 50));
        textBubble.setText("Ready for 25 minutes of focus?");
        
        imagePanel.setImagePath("snoopy1.gif");
        taskField.setText("Pomodoro Session");
    }

    private void saveAction() {
        String task = taskField.getText().trim();
        if (task.isEmpty()) task = "General Study";
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String duration = formatTime(isPomodoroMode ? (25 * 60 - currentSeconds) : currentSeconds);

        try (FileWriter fw = new FileWriter("study_log.txt", true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(String.format("[%s] %s - Duration: %s%n", date, task, duration));
            JOptionPane.showMessageDialog(this, "Session Saved in Journal!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving file (!)");
        }
    }

    private void showHistoryAction() {
        JTextArea textArea = new JTextArea(15, 40);
        textArea.setEditable(false);
        textArea.setFont(new Font("Comic Sans MS", Font.PLAIN, 12));
        textArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        try (Scanner scanner = new Scanner(new File("study_log.txt"))) {
            while (scanner.hasNextLine()) textArea.append(scanner.nextLine() + "\n");
        } catch (FileNotFoundException ex) {
            textArea.setText("No history found in journal.");
        }
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Study Journal", JOptionPane.PLAIN_MESSAGE);
    }

    private void updateTimerLabel() {
        timeLabel.setText(formatTime(currentSeconds));
    }

    private String formatTime(int totalSecs) {
        int hours = totalSecs / 3600;
        int minutes = (totalSecs % 3600) / 60;
        int seconds = totalSecs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private JButton createComicButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2)); 
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 45));
        return btn;
    }

    class FixedSizeImagePanel extends JPanel {
        private Image image;
        private int width, height;

        public FixedSizeImagePanel(int w, int h) {
            this.width = w;
            this.height = h;
            setOpaque(false);
            setPreferredSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
        }

        public void setImagePath(String path) {
            File f = new File(path);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(path);
                this.image = icon.getImage();
                if (this.image != null) {
                    this.image.flush();
                }
                repaint(); 
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                g.drawImage(image, 0, 0, width, height, this);
            }
        }
    }

    class ComicBubbleLabel extends JLabel {
        public ComicBubbleLabel(String text) {
           super(text, JLabel.CENTER);
           setFont(new Font("Comic Sans MS", Font.BOLD, 16));
           setBorder(new EmptyBorder(15, 25, 15, 25));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int strokeThickness = 3;
            int arcSize = 40;

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(strokeThickness/2, strokeThickness/2, 
                             getWidth()-strokeThickness, getHeight()-strokeThickness, 
                             arcSize, arcSize);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(strokeThickness)); 
            g2.drawRoundRect(strokeThickness/2, strokeThickness/2, 
                             getWidth()-strokeThickness, getHeight()-strokeThickness, 
                             arcSize, arcSize);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}

public class StudyBuddy {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.setProperty("awt.useSystemAAFontSettings","on"); 
            new StudyBuddyGUI();
        });
    }
}