package Utils;

import java.awt.*;

public class main {
    public static void main(String[] args) {
        Rectangle a = new Rectangle(0, 0, 10, 10);  // from (0,0) to (10,10)
        Rectangle b = new Rectangle(5, 5, 10, 10);  // from (5,5) to (15,15)
        System.out.println(a.intersects(b));
    }
}
