package World;
import Constants.Constants;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerGenome implements KeyListener, Runnable {

    private static final List<Integer> pressedKeys = new ArrayList<>();
    private static final Object lock = new Object();
    private static boolean running = true;
    private static final int UPDATE_INTERVAL_MS = 1;

    public PlayerGenome() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        synchronized (lock) {
//            System.out.println("PRESSED KEY!!!");
            if (!pressedKeys.contains(e.getKeyCode())) {
                pressedKeys.add(e.getKeyCode());
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        synchronized (lock) {
            pressedKeys.remove((Integer) e.getKeyCode());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used, but must be implemented for KeyListener
    }

    @Override
    public void run() {
        while (running) {
            synchronized (lock) {
            }
            try {
                Thread.sleep(UPDATE_INTERVAL_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static double[] getOutput() {
        synchronized (lock) {
            double[] output = new double[Constants.NeuralNet.outputNum];
            for(int i : pressedKeys) {
                if(i==KeyEvent.VK_W) output[0]=1;
                else if(i==KeyEvent.VK_S) output[1]=1;
                else if(i==KeyEvent.VK_A) output[2]=1;
                else if(i==KeyEvent.VK_D) output[3]=1;
                else if(i==KeyEvent.VK_1) output[4]=1;
                else if(i==KeyEvent.VK_2) output[5]=1;
                else if(i==KeyEvent.VK_CONTROL) output[6]=1;
                else if(i==KeyEvent.VK_UP) output[7]=0.1;
                else if(i==KeyEvent.VK_DOWN) output[7]=-0.1;
                else if(i==KeyEvent.VK_SPACE) output[8]=1;
                else if(i==KeyEvent.VK_SHIFT) output[9]=1;
            }
//            System.out.println(Arrays.toString(output));
            return output;
        }
    }
}
