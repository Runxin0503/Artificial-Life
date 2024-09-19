package Screens;

import Constants.Constants.WindowConstants;
import Constants.Constants.WorldConstants;
import Menu.MainMenu;
import World.PlayerGenome;
import World.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ZoomableWindow extends JFrame {

    public worldPanel worldPanel;
    private sidePanel controlPanel;
    public Settings settingsDialog;
    public Graph graph;
    private final World world;
    private JLabel tickRateTracker;
    private MainMenu mainMenu;

    public ZoomableWindow(World world, MainMenu mm) {
        this.world = world;
        setTitle("Flappy Bird Paradise");
        addKeyListener(new PlayerGenome());

        setSize(WindowConstants.worldWidth, WindowConstants.worldHeight);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        createGraph();
        createMenuBar();
        createControlPanel();
        worldPanel = new worldPanel(world, controlPanel);

        add(worldPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        controlPanel.setVisible(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                world.exists = false;
                int choice = JOptionPane.showConfirmDialog(
                        ZoomableWindow.this,
                        "Do you want to save the world before exiting?",
                        "Save World",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    String filename = world.name.endsWith("unnamed") ? JOptionPane.showInputDialog(
                            ZoomableWindow.this,
                            "Enter a name for the save file:",
                            "Save World",
                            JOptionPane.PLAIN_MESSAGE
                    ) : world.name;

                    if (filename == null) {
                        world.exists = true;
                        return;
                    }

                    System.out.println("Saving the World...\n" + filename);
                    try {
                        if (!filename.isEmpty()) world.name = filename;
                        World.save(world);
                        JOptionPane.showMessageDialog(ZoomableWindow.this, "World has been saved as " + world.name);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                        JOptionPane.showMessageDialog(ZoomableWindow.this, "Error saving world: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        world.exists = true;
                        return;
                    }
                    mm.reset();
                } else if (choice == JOptionPane.NO_OPTION) {
                    mm.reset();
                } else {
                    world.exists = true;
                }
            }
        });
    }

    @Override
    public void repaint() {
        super.repaint();

        tickRateTracker.setText("Tick Rate: " + Math.max(1, (int) Math.round(1000.0 / world.tickRate)) + "\tticks/sec");

        if (settingsDialog != null && settingsDialog.isVisible())
            settingsDialog.updateVariables();

        if (controlPanel.isVisible()) {
            controlPanel.updateVariables();
        }

        worldPanel.repaint();
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> {
            world.exists = false;
            try {
                World.save(world);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            world.exists = true;
        });
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(saveItem);
        fileMenu.add(exitItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleControlPanelItem = new JMenuItem("Graph Data Points");
        toggleControlPanelItem.addActionListener(e -> graph.setVisible(!graph.isVisible()));
        viewMenu.add(toggleControlPanelItem);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(e -> showSettingsDialog());
        editMenu.add(settingsItem);

        tickRateTracker = new JLabel("Tick Rate: -/sec");

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(editMenu);
        menuBar.add(tickRateTracker);

        setJMenuBar(menuBar);
    }

    private void createControlPanel() {
        controlPanel = new sidePanel(world);
        controlPanel.setPreferredSize(new Dimension(WindowConstants.controlPanelWidth, getHeight()));
    }

    private void createGraph() {
        graph = new Graph();
        for (String s : WorldConstants.Settings.reportInfo) graph.addDataSet(Graph.VALUES, s);
        for (String s : WorldConstants.Settings.countInfo) graph.addDataSet(Graph.ENTITIES, s);
        graph.addDataSet(Graph.ENTITIES, "Ticks/Sec");
        graph.setVisible(false);
    }

    private void createSettingsDialog() {
        settingsDialog = new Settings(this, "Settings", true, world);
    }

    private void showSettingsDialog() {
        if (settingsDialog == null) createSettingsDialog();
        settingsDialog.setVisible(true);
    }
}