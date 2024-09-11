package Menu;

import Constants.Constants.*;
import Constants.Constants.WindowConstants;
import World.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.concurrent.ExecutorService;

public class MainMenu extends JFrame {
    private worldPointer worldPointer;
    private JPanel mainMenu;
    private JPanel loadScreen;
    private JScrollPane scrollPane;
    private ExecutorService executorService;

    public MainMenu(worldPointer worldPointer, ExecutorService executorService) {
        setTitle("Main Menu");
        setSize(WindowConstants.menuWidth, WindowConstants.menuHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setResizable(false);

        this.worldPointer = worldPointer;
        this.executorService = executorService;

        // Create panel for the buttons
        JPanel background = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(ImageConstants.menuBackground, 0, 0, getWidth(), getHeight(), this);
            }
        };

        createMainMenu();
        createLoadScreen();

        background.add(mainMenu);
        background.add(loadScreen);

        // Add the background panel to the frame
        add(background);

        // Make the frame visible
        setVisible(true);
    }

    private void createMainMenu() {
        mainMenu = new JPanel();
        mainMenu.setOpaque(false);
        mainMenu.setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(new ImageIcon(ImageConstants.titleCard));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel authorLabel = new JLabel("By Author Name");
        authorLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        authorLabel.setForeground(Color.WHITE); // Adjust text color as needed
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(Box.createVerticalStrut(120));
        titlePanel.add(titleLabel);
        titlePanel.add(authorLabel);

        // Create panel for the buttons
        JPanel menuButtonPanel = new JPanel();
        menuButtonPanel.setOpaque(false); // Make the button panel transparent
        menuButtonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around buttons
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        menuButtonPanel.add(Box.createVerticalStrut(70), gbc); // Add 40 units of vertical space
        gbc.gridy++;

        // New Game button
        JButton newGameButton = addCustomButton("New World", 150, 40, ImageConstants.button, ImageConstants.buttonHover, ImageConstants.buttonPressed);
        newGameButton.addActionListener(e -> {
            worldPointer.world = new World(MainMenu.this, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + " unnamed", executorService);
            setVisible(false);
        });

        // Load from Save button
        JButton loadButton = addCustomButton("Load from Save", 150, 40, ImageConstants.button, ImageConstants.buttonHover, ImageConstants.buttonPressed);
        loadButton.setPreferredSize(new Dimension(150, 40)); // Set button size
        loadButton.addActionListener(e -> {
            updateTree();
            loadScreen.setVisible(true);
            mainMenu.setVisible(false);
        });

        // Exit button
        JButton exitButton = addCustomButton("Exit Game", 150, 40, ImageConstants.button, ImageConstants.buttonHover, ImageConstants.buttonPressed);
        exitButton.setPreferredSize(new Dimension(150, 40)); // Set button size
        exitButton.addActionListener(e -> {
            System.exit(0);
        });

        // Add buttons to the button panel
        menuButtonPanel.add(newGameButton, gbc);
        gbc.gridy++;
        menuButtonPanel.add(loadButton, gbc);
        gbc.gridy++;
        menuButtonPanel.add(exitButton, gbc);

        // Add the panels for the main screen
        mainMenu.add(titlePanel, BorderLayout.NORTH);
        mainMenu.add(menuButtonPanel, BorderLayout.CENTER);
    }

    private void createLoadScreen() {
        loadScreen = new JPanel();
        loadScreen.setOpaque(false);
        loadScreen.setLayout(new BoxLayout(loadScreen, BoxLayout.Y_AXIS));

        JButton backButton = addCustomButton("Back", 150, 40, ImageConstants.button, ImageConstants.buttonHover, ImageConstants.buttonPressed);
        backButton.addActionListener(e -> {
            loadScreen.setVisible(false);
            mainMenu.setVisible(true);
        });

        JPanel background = new JPanel(new GridBagLayout()) {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(ImageConstants.scrollPanel, 0, 0, getWidth(), getHeight(), this);
            }
        };
        background.setOpaque(false);
        background.setPreferredSize(new Dimension(WindowConstants.loadBarWidth, WindowConstants.loadBarHeight));

        updateTree();
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(WindowConstants.loadBarWidth, WindowConstants.loadBarHeight - 30));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        background.add(scrollPane, gbc);

        // Add components to loadScreen
        loadScreen.add(Box.createVerticalStrut(100));
        loadScreen.add(background);
        loadScreen.add(Box.createVerticalStrut(40));
        loadScreen.add(backButton);
        loadScreen.setVisible(false);
    }

    public void updateTree() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("worlds");
        addChildren(root, new File("resources/worlds"));
        JTree tree = new JTree(root);
        tree.setOpaque(false);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            {
                setOpaque(false);
                setBackgroundNonSelectionColor(new Color(0, 0, 0, 0)); // Make background transparent
            }
        });
        tree.addTreeSelectionListener(e -> {
            TreePath path = e.getNewLeadSelectionPath();
            if (path != null) {
                StringBuilder pathBuilder = new StringBuilder();
                for (Object obj : path.getPath()) {
                    pathBuilder.append(obj.toString()).append(File.separator);
                }
                File selectedFile = new File(pathBuilder.toString());

                if (selectedFile != null && selectedFile.getName().endsWith(".ser")) {
                    try {
                        worldPointer.world = World.read(MainMenu.this, new File("resources/" + selectedFile.getPath()), executorService); // Load the game when the file button is clicked
                        loadScreen.setVisible(false);
                        mainMenu.setVisible(true);
                        GridList.reset();
                        setVisible(false);
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        if (scrollPane == null) scrollPane = new JScrollPane(tree);
        else {
            scrollPane.setViewportView(tree);
        }
    }

    private void addChildren(DefaultMutableTreeNode node, File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(f.getName());
                node.add(childNode);
                if (f.isDirectory()) {
                    addChildren(childNode, f);
                }
            }
        }
    }

    private JButton addCustomButton(String text, int width, int height, Image image, Image hoverImage, Image pressedImage) {
        JButton result = new JButton(text);
        result.setPreferredSize(new Dimension(width, height));
        result.setHorizontalTextPosition(JButton.CENTER);
        result.setVerticalTextPosition(JButton.CENTER);
        result.setIcon(new ImageIcon(image.getScaledInstance(width, height, ImageConstants.ResizeConstant)));
        result.setRolloverIcon(new ImageIcon(hoverImage.getScaledInstance(width, height, ImageConstants.ResizeConstant)));
        result.setPressedIcon(new ImageIcon(pressedImage.getScaledInstance(width, height, ImageConstants.ResizeConstant)));
        result.setBorderPainted(false);
        result.setContentAreaFilled(false);
        result.setFocusPainted(false);
        result.setOpaque(false);
        return result;
    }

    public void reset() {
        setVisible(true);
        worldPointer.world.window.dispose();
        worldPointer.world = null;
    }
}