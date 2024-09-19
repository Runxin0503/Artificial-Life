package Screens;

import Constants.Constants;
import Constants.Constants.NeuralNet;
import Evolution.NN;
import Genome.node;
import Genome.synapse;

import javax.swing.*;
import java.awt.*;

public class GenomeFrame extends JFrame {

    private final JPanel panel;
    private NN NN;

    public void setGenome(NN NN) {
        this.NN = NN;
    }

    public GenomeFrame() throws HeadlessException {
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        this.setTitle("NEAT");
        this.setMinimumSize(new Dimension(1000, 700));
        this.setPreferredSize(new Dimension(1000, 700));
        this.setResizable(false);

        this.setLayout(new BorderLayout());


        UIManager.LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();
        try {
            UIManager.setLookAndFeel(looks[3].getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }


        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(1000, 100));
        menu.setLayout(new GridLayout(1, 6));

        JButton buttonB = new JButton("random weight");
        buttonB.addActionListener(e -> {
            NN.randomWeights(NeuralNet.mutationWeightRandomStrength);
            repaint();
        });
        menu.add(buttonB);

        JButton buttonZ = new JButton("weight shift");
        buttonZ.addActionListener(e -> {
            NN.shiftWeights(NeuralNet.mutationWeightShiftStrength);
            repaint();
        });
        menu.add(buttonZ);

        JButton buttonC = new JButton("Link mutate");
        buttonC.addActionListener(e -> {
            NN.mutateSynapse();
            repaint();
        });
        menu.add(buttonC);

        JButton buttonD = new JButton("Node mutate");
        buttonD.addActionListener(e -> {
            NN.mutateNode();
            repaint();
        });
        menu.add(buttonD);


        JButton buttonE = new JButton("on/off");
        buttonE.addActionListener(e -> {
//                genome.mutateSynapseToggle();
            repaint();
        });
        menu.add(buttonE);

        JButton buttonF = new JButton("Mutate");
        buttonF.addActionListener(e -> {
            NN.mutate();
            repaint();
        });
        menu.add(buttonF);

        JButton buttonG = new JButton("Exit");
        buttonG.addActionListener(e -> this.dispose());
        menu.add(buttonG);


        this.add(menu, BorderLayout.NORTH);

        this.panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.clearRect(0, 0, 10000, 10000);
                g.setColor(Color.black);
                g.fillRect(0, 0, 10000, 10000);
                double maxWeight = 1;
                for (synapse s : NN.synapses) if (s.weight > maxWeight) maxWeight = s.weight;

                for (synapse s : NN.synapses) {
                    if (s.enabled) paintConnection(s, (Graphics2D) g, maxWeight);
                }

                for (node n : NN.nodes) {
                    paintNode(n, (Graphics2D) g);
                }

                g.setColor(Color.green);
                for (node n : NN.nodes) {
                    if (n.isInput()) {
                        g.drawString(Constants.NeuralNet.String[n.innovationID], 2, (int) (this.getHeight() * n.y));
                    } else if (n.isOutput()) {
                        g.drawString(Constants.NeuralNet.String[n.innovationID], (int) (this.getWidth() * n.x) + 22, (int) (this.getHeight() * n.y));
                    }
                }
            }

            private void paintNode(node n, Graphics2D g) {
                if (n.latestInputSum > 0)
                    g.setColor(Color.green);
                else if (n.latestInputSum < 0)
                    g.setColor(Color.red);
                else
                    g.setColor(Color.gray);
                g.fillOval((int) (this.getWidth() * n.x) - 10,
                        (int) (this.getHeight() * n.y) - 10, 20, 20);
                if (!n.isOutput()) {
                    if (n.latestOutput > 0)
                        g.setColor(Color.green);
                    else if (n.latestOutput < 0)
                        g.setColor(Color.red);
                    else
                        g.setColor(Color.gray);
                    g.fillOval((int) (this.getWidth() * n.x) - 5,
                            (int) (this.getHeight() * n.y) - 5, 10, 10);
                }
            }

            private void paintConnection(synapse s, Graphics2D g, double maxWeight) {
                g.setColor(s.weight < 0 ? Color.red : Color.green);
                g.setStroke(new BasicStroke((float) (1 + 4 * Math.abs((s.weight) / maxWeight))));
                g.drawLine(
                        (int) (this.getWidth() * s.from.x),
                        (int) (this.getHeight() * s.from.y),
                        (int) (this.getWidth() * s.to.x),
                        (int) (this.getHeight() * s.to.y));
            }
        };
        this.add(panel, BorderLayout.CENTER);
    }

}
