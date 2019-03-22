import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Ins on 22.03.2019.
 */
public class Object {
    // it's an object. defined by object class and bounding rectangle
    // all the coordinates stored are relative to original image

    public int c;
    public Rectangle bounds;

    public Object(int c, Rectangle bounds) {
        this.c = c;
        this.bounds = bounds;
    }

    // creates object from string of YOLO format
    public Object(String line, int maxW, int maxH) {
        List<String> coords = new LinkedList<>(Arrays.asList(line.split(" ")));

        int width = (int) (Double.parseDouble(coords.get(3)) * maxW);
        int height = (int) (Double.parseDouble(coords.get(4)) * maxH);

        int x = (int) (Double.parseDouble(coords.get(1)) * maxW - width / 2);
        int y = (int) (Double.parseDouble(coords.get(2)) * maxH - height / 2);

        this.c = Integer.parseInt(coords.get(0));
        this.bounds = new Rectangle(x, y, width, height);
    }

    // returns YOLO string with coordinates related to some Rectangle
    public String toYoloFormat(Rectangle rect) {
        Object o = convertCoords(rect);

        float x = (float) o.bounds.getCenterX() / rect.width;
        float y = (float) o.bounds.getCenterY() / rect.height;
        float width = (float) o.bounds.width / rect.width;
        float height = (float) o.bounds.height / rect.height;

        return o.c + " " + x + " " + y + " " + width + " " + height;
    }

    // returns new Object with coordinates relative to given rectangle
    private Object convertCoords(Rectangle rect) {
        Rectangle newBounds = new Rectangle(bounds.x - rect.x, bounds.y - rect.y, bounds.width, bounds.height);

        return new Object(this.c, newBounds);
    }
}
