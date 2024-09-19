package Screens;

import Constants.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Graph extends JFrame {
    private final HashMap<Integer, HashMap<String, DataSet>> dataSets = new HashMap<Integer, HashMap<String, DataSet>>();
    private final ArrayList<Color> Colors = new ArrayList<Color>();
    private double scaleX = 1.0;
    private double scaleY = 1.0;
    private double shiftX = 0.0;
    private int latestTime = 0;
    public static final int VALUES = 1;
    public static final int ENTITIES = 2;
    private int datapointLength = 0;
    public int intervalOfAccepting = 1;
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final JPanel legendPanel;

    public Graph() {
        setTitle("Graph");
        for (int i = 0; i < 156; i++)
            for (int j = 0; j < 156; j++) for (int k = 0; k < 156; k++) Colors.add(new Color(i, j, k));
        for (int i : new int[]{1, 2}) dataSets.put(i, new HashMap<>());
        setSize(Constants.WindowConstants.graphWidth, Constants.WindowConstants.graphHeight);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        setLocationRelativeTo(null);
        setResizable(false);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        legendPanel = new JPanel();
        legendPanel.setLayout(new GridBagLayout());
        add(legendPanel, BorderLayout.EAST);

        JMenuBar menuBar = new JMenuBar();

        JMenu selection = new JMenu("Selection");
        JMenuItem entity = new JMenuItem("#");
        entity.addActionListener(e -> {
            for (HashMap<String, DataSet> temp : dataSets.values())
                for (DataSet ds : temp.values()) ds.getCheckBox().setSelected(false);
            for (String str : new String[]{"# Creatures", "# Eggs", "# Corpses", "# Berries"})
                dataSets.get(Graph.ENTITIES).get(str.toLowerCase()).getCheckBox().setSelected(true);
        });
        selection.add(entity);
        JMenuItem reproduce = new JMenuItem("Reproduction");
        reproduce.addActionListener(e -> {
            for (HashMap<String, DataSet> temp : dataSets.values())
                for (DataSet ds : temp.values()) ds.getCheckBox().setSelected(false);
            for (String str : new String[]{"Reproduction Cost", "Offspring Investment", "Incubation Time", "Maturity"})
                dataSets.get(Graph.VALUES).get("avg" + str.toLowerCase()).getCheckBox().setSelected(true);
        });
        selection.add(reproduce);
        JMenuItem Combat = new JMenuItem("Combat");
        Combat.addActionListener(e -> {
            for (HashMap<String, DataSet> temp : dataSets.values())
                for (DataSet ds : temp.values()) ds.getCheckBox().setSelected(false);
            for (String str : new String[]{"Strength", "Armour", "Health", "MaxHealth"})
                dataSets.get(Graph.VALUES).get("avg" + str.toLowerCase()).getCheckBox().setSelected(true);
        });
        selection.add(Combat);
        JMenuItem Diet = new JMenuItem("Diet");
        Diet.addActionListener(e -> {
            for (HashMap<String, DataSet> temp : dataSets.values())
                for (DataSet ds : temp.values()) ds.getCheckBox().setSelected(false);
            for (String str : new String[]{"Herbivore Affinity", "Carnivore Affinity", "Diet Value"})
                dataSets.get(Graph.VALUES).get("avg" + str.toLowerCase()).getCheckBox().setSelected(true);
            for (String str : new String[]{"# Carnivore", "# Herbivore"})
                dataSets.get(Graph.ENTITIES).get(str.toLowerCase()).getCheckBox().setSelected(true);
        });
        selection.add(Diet);
        JMenuItem Movement = new JMenuItem("Movement");
        Movement.addActionListener(e -> {
            for (HashMap<String, DataSet> temp : dataSets.values())
                for (DataSet ds : temp.values()) ds.getCheckBox().setSelected(false);
            for (String str : new String[]{"Size", "MinSize", "MaxSize", "Force", "Speed", "Energy", "MaxEnergy"})
                dataSets.get(Graph.VALUES).get("avg" + str.toLowerCase()).getCheckBox().setSelected(true);
        });
        selection.add(Movement);

        menuBar.add(selection);

        setJMenuBar(menuBar);
    }

    public void addDataSet(int type, String str) {
        Color c = Colors.remove((int) (Math.random() * Colors.size()));

        if (type == VALUES) {
            JCheckBox checkBox1 = new JCheckBox();
            checkBox1.setSelected(false);
            checkBox1.setForeground(c);
            checkBox1.addItemListener(e -> repaint());
            gbc.gridx = 0;
            legendPanel.add(checkBox1, gbc);
            JCheckBox checkBox2 = new JCheckBox();
            checkBox2.setSelected(false);
            checkBox2.setForeground(c);
            checkBox2.addItemListener(e -> repaint());
            gbc.gridx++;
            legendPanel.add(checkBox2, gbc);
            JCheckBox checkBox3 = new JCheckBox(str);
            checkBox3.setSelected(false);
            checkBox3.setForeground(c);
            checkBox3.addItemListener(e -> repaint());
            gbc.gridx++;
            legendPanel.add(checkBox3, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            dataSets.get(type).put("min" + str.toLowerCase(), new DataSet(c, checkBox1));
            dataSets.get(type).put("avg" + str.toLowerCase(), new DataSet(c, checkBox2));
            dataSets.get(type).put("max" + str.toLowerCase(), new DataSet(c, checkBox3));
        } else if (type == ENTITIES) {
            JCheckBox checkBox = new JCheckBox(str);
            checkBox.setSelected(false);
            checkBox.setForeground(c);
            checkBox.addItemListener(e -> repaint());
            gbc.gridx = 2;
            legendPanel.add(checkBox, gbc);
            gbc.gridx = 0;
            gbc.gridy++;
            dataSets.get(type).put(str.toLowerCase(), new DataSet(c, checkBox));
        } else {
            System.out.println("ERROR");
        }
    }

    public void updateDataSets() {
        datapointLength++;
        if (datapointLength > Constants.WindowConstants.graphMaxDataSize) {
            System.out.println("Culling");
            for (HashMap<String, DataSet> temp : dataSets.values()) {
                for (DataSet ds : temp.values()) ds.cullHalf();
            }
            datapointLength /= 2;
            intervalOfAccepting *= 2;
        }
    }

    public void addPoint(int type, String str, double y, int x) {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            System.out.println("ERROR ON TYPE " + type + "  AND VAL " + str);
            return;
        }
        dataSets.get(type).get(str.toLowerCase()).addPoint(x, y);
        latestTime = x;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawGraph(g);
    }

    private void calculateScaling() {
        double maxY = 1;
        double minX = Double.MAX_VALUE;

        for (HashMap<String, DataSet> stuff : dataSets.values()) {
            for (DataSet dataSet : stuff.values()) {
                if (!dataSet.getCheckBox().isSelected()) continue;
                for (Point2D p : dataSet.getPoints()) {
                    if (p.getY() > maxY) maxY = p.getY();
                    if (p.getX() < minX) minX = p.getX();
                }
            }
        }

        scaleX = (Constants.WindowConstants.graphWidth * 0.75 - 100) / latestTime;
        scaleY = (Constants.WindowConstants.graphHeight * 0.75 - 100) / maxY;
        shiftX = minX;
    }

    private void drawGraph(Graphics g) {
        try {
            calculateScaling();
        } catch (Exception ignored) {
        }

        int width = getWidth();
        int height = getHeight();

        // Draw axes
        g.drawLine(50, 50, 50, height - 50);
        g.drawLine(50, height - 50, width - 50, height - 50);

        // Draw labels
        g.setColor(Color.BLACK);
        g.drawString("Time", width / 2, height - 20);
        drawVerticalString(g, "Values", 20, height / 2);

        // Draw numerical labels
        drawXLabels(g, width, height);
        drawYLabels(g, height);

        for (HashMap<String, DataSet> stuff : dataSets.values()) {
            for (DataSet dataSet : stuff.values()) {
                if (!dataSet.getCheckBox().isSelected()) continue;
                g.setColor(dataSet.getColor());
                List<Point2D> points = new ArrayList<Point2D>(dataSet.getPoints());

//                int interval = Math.max(1, points.size() / 1000); // Render every interval-th point

                Point lastPoint = null;
                for (int i = 0; i < points.size(); i++) {
                    Point nowPoint = scalePoint(points.get(i));
                    if (lastPoint == null) {
                        lastPoint = nowPoint;
                        continue;
                    }
                    g.drawLine(50 + lastPoint.x, height - 50 - lastPoint.y, 50 + nowPoint.x, height - 50 - nowPoint.y);
                    lastPoint = nowPoint;
                }
            }
        }
    }


    private void drawVerticalString(Graphics g, String text, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(text);
        int height = fm.getHeight();
        g2d.translate((float) x, (float) y);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(text, -width / 2, height / 2);
        g2d.rotate(Math.PI / 2);
        g2d.translate(-(float) x, -(float) y);
    }

    private void drawXLabels(Graphics g, int width, int height) {
        int maxX = (int) (width / scaleX);
        int interval = Math.max(maxX / 10, 1);  // Avoid division by zero and ensure at least one label
        for (int i = (int) Math.round(shiftX); i <= maxX; i += interval) {
            int x = (int) (50 + (i - shiftX) * scaleX);
            g.drawLine(x, height - 50, x, height - 45);
            g.drawString(Integer.toString(i), x - 5, height - 30);
        }
    }

    private void drawYLabels(Graphics g, int height) {
        double maxY = (height / scaleY);  // Avoid division by zero and ensure at least one label
        for (double i = 0; i <= maxY; i += maxY * 0.1) {
            int y = (int) Math.round(height - 50 - i * scaleY);
            g.drawLine(45, y, 50, y);
            g.drawString(Double.toString((int) (i * 100) / 100.0), 20, y + 5);
        }
    }

    private Point scalePoint(Point2D p) {
        return new Point(Math.max(0, (int) Math.round((p.getX() - shiftX) * scaleX)), (int) Math.round(p.getY() * scaleY));
    }

    private class DataSet {
        private final List<Point2D> points;
        private final Color color;
        private final JCheckBox checkBox;

        public DataSet(Color color, JCheckBox checkBox) {
            this.color = color;
            this.points = new ArrayList<>();
            this.checkBox = checkBox;
        }

        public void addPoint(double x, double y) {

            points.add(new Point2D.Double(x, y));
        }

        public void cullHalf() {
            for (int i = points.size() - 1; i >= 0; i--) {
                if (i % 2 == 0) points.remove(i);
            }
        }

        public List<Point2D> getPoints() {
            return points;
        }

        public Color getColor() {
            return color;
        }

        public JCheckBox getCheckBox() {
            return checkBox;
        }
    }
}
