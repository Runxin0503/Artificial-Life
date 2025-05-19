package Utils;

public class Rectangle {

    public double x,y;
    public double width,height;

    public Rectangle(double width, double height) {
        this.x = 0;
        this.y = 0;
        this.width = width;
        this.height = height;
    }

    public Rectangle(double x,double y,double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setRect(Rectangle newRect) {
        this.x = newRect.x;
        this.y = newRect.y;
        this.width = newRect.width;
        this.height = newRect.height;
    }

    public void scaleByWidth(double newWidth) {
        this.height = this.height * newWidth / this.width;
        this.width = newWidth;
    }

    public static double distFromRect(Rectangle a, Rectangle b) {
        double x1 = a.x, y1 = a.y, x1b = x1 + a.width, y1b = y1 + a.height, x2 = b.x, y2 = b.y, x2b = x2 + b.width, y2b = y2 + b.height;
        boolean left = x2b < x1;
        boolean right = x1b < x2;
        boolean bottom = y2b < y1;
        boolean top = y1b < y2;

        if (top && left) {
            return Math.sqrt((x1 - x2b) * (x1 - x2b) + (y1b - y2) * (y1b - y2));
        } else if (left && bottom) {
            return Math.sqrt((x1 - x2b) * (x1 - x2b) + (y1 - y2b) * (y1 - y2b));
        } else if (bottom && right) {
            return Math.sqrt((x1b - x2) * (x1b - x2) + (y1 - y2b) * (y1 - y2b));
        } else if (right && top) {
            return Math.sqrt((x1b - x2) * (x1b - x2) + (y1b - y2) * (y1b - y2));
        } else if (left) {
            return x1 - x2b;
        } else if (right) {
            return x2 - x1b;
        } else if (bottom) {
            return y1 - y2b;
        } else if (top) {
            return y2 - y1b;
        } else {
            // Rectangles intersect
            return 0;
        }
    }

    public double getMinX() {
        return x;
    }

    public double getMaxX() {
        return x + width;
    }

    public double getMinY() {
        return y;
    }

    public double getMaxY() {
        return y + height;
    }
}
