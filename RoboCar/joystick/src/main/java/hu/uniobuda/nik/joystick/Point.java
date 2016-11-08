package hu.uniobuda.nik.joystick;

/**
 * Created by Forisz on 07/05/16.
 */
public class Point {
    float x, y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public Point() {
        this.x = 0f;
        this.y = 0f;
    }

    @Override
    public String toString() {
        return "Point [x=" + x + ", y=" + y + "]";
    }
}
