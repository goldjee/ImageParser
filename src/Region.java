import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 22.03.2019.
 */
public class Region {
    // it's a class that describes a bounding rectangle which contains a number of objects
    // all the coordinates stored are relative to original image

    private final int offset = 20;

    public List<Object> objects;
    public Rectangle bounds;

    public Region(int maxW, int maxH, int minSize) {
        this.objects = new ArrayList<>();
        this.bounds = new Rectangle((maxW - minSize) / 2, (maxH - minSize) / 2, minSize, minSize);
    }

    public Region(List<Object> objects, Rectangle bounds) {
        this.objects = objects;
        this.bounds = bounds;
    }

    public Region(Object o) {
        this.bounds = (Rectangle) o.bounds.clone();
        objects = new ArrayList<>();
        objects.add(o);
    }

    public void prepare(int maxW, int maxH, int minSize) {
        squarify(minSize);
        fit(maxW, maxH);
    }

    public boolean isNear(Region region) {
        if (isOverlapping(region))
            return true;

        if (Math.abs(br().x - region.bl().x) <= offset || (Math.abs(region.br().x - bl().x) <= offset))
            return true;

        if (Math.abs(br().y - region.tr().y) <= offset || (Math.abs(region.br().y - tr().y) <= offset))
            return true;

        return false;
    }

    // returns new Region whiich is a sum of this and given one
    public Region merge(Region region) {
        List<Object> objects = new ArrayList<>(this.objects);
        objects.addAll(region.objects);

        Point tl1 = tl();
        Point br1 = br();
        Point tl2 = region.tl();
        Point br2 = region.br();

        Point tl = new Point(Math.min(tl1.x, tl2.x), Math.min(tl1.y, tl2.y));
        Point br = new Point(Math.max(br1.x, br2.x), Math.max(br1.y, br2.y));

        Rectangle bounds = new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y);

        return new Region(objects, bounds);
    }

    // returns list of YOLO strings describing objects in region
    public List<String> toYoloList() {
        List<String> lines = new ArrayList<>(objects.size());

        for (Object o : objects) {
            String line = o.toYoloFormat(bounds);
            lines.add(line);
        }

        return lines;
    }

    public Point tl() {
        return new Point(bounds.x, bounds.y);
    }

    public Point tr() {
        return new Point(bounds.x + bounds.width, bounds.y);
    }

    public Point bl() {
        return new Point(bounds.x, bounds.y + bounds.height);
    }

    public Point br() {
        return new Point(bounds.x + bounds.width, bounds.y + bounds.height);
    }

    public int width() {
        return bounds.width;
    }

    public int height() {
        return bounds.height;
    }

    // makes bounds square. center of new bounds is preserved
    private void squarify(int minSize) {
        Rectangle newBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);

        // checking if bounds are actually bigger than the new size of image
        int origSize = Math.max(newBounds.width, newBounds.height);
        if (origSize > minSize)
            // so we should add some offset
            origSize += offset;

        // converting it to 1:1
        if (newBounds.width != newBounds.height) {
            newBounds.width = Math.max(origSize, minSize);
            newBounds.height = newBounds.width;
        }

        newBounds.width += offset;
        newBounds.height += offset;

        // fitting centers
        if (bounds.getCenterX() != newBounds.getCenterX() || bounds.getCenterY() != newBounds.getCenterY()) {
            newBounds.x -= newBounds.getCenterX() - bounds.getCenterX();
            newBounds.y -= newBounds.getCenterY() - bounds.getCenterY();
        }

        bounds = newBounds;
    }

    // fits bounding box into given dimensions
    private void fit(int maxW, int maxH) {
        Rectangle newBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height);

        // assuming rectangle is squared
        int s = Math.min(Math.min(maxW, maxH), newBounds.width);
        newBounds.width = s;
        newBounds.height = s;

        if (newBounds.width == maxW || newBounds.x < 0)
            newBounds.x = 0;
        if (newBounds.height == maxH || newBounds.y < 0)
            newBounds.y = 0;
        if (newBounds.x + newBounds.width > maxW)
            newBounds.x -= newBounds.x + newBounds.width - maxW;
        if (newBounds.y + newBounds.height > maxH)
            newBounds.y -= newBounds.y + newBounds.height - maxH;

        bounds = newBounds;
    }

    private boolean isOverlapping(Region region) {
        if (
                region.bl().x > tr().x ||
                region.bl().y < tr().y ||
                bl().x > region.tr().x ||
                bl().y < region.tr().y
            )
            return false;
        else
            return true;
    }
}
