import javafx.util.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Created by Ins on 16.03.2019.
 */
public class Cropper implements Runnable {

    // size of bounding rect in pixels
    private static final int size = 320;
    private static final float offset = 0.2f; // relative offset of bounding boxes

    private FileIO fileIO;

    private Pair<File, File> pair = null;

    public Cropper(FileIO fileIO) {
        this.fileIO = fileIO;
    }

    public void set(Pair<File, File> pair) {
        this.pair = pair;
    }

    @Override
    public void run() {
        crop(pair);
    }

    private void crop(Pair<File, File> pair) {
        List<String> regions = fileIO.readTxt(pair.getKey());
        BufferedImage image = fileIO.readImg(pair.getValue());

        if (regions != null && image != null) {
            Rectangle bounds;
            List<Pair<Integer, Rectangle>> objects = txtToRegions(regions, image.getWidth(), image.getHeight());

            if (objects.size() > 0) {
                // if there are some regions

                bounds = square(getBounds(objects));
            }
            else {
                // if it's background image
                bounds = square(image.getWidth(), image.getHeight());
            }

            // finalizing cropping bounds
            bounds = fit(bounds, image.getWidth(), image.getHeight());

            // converting object data
            objects = transformRegions(objects, new Point(bounds.x, bounds.y));
            regions = regionsToTxt(objects, bounds.width, bounds.height);

            BufferedImage cropped = image.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
            cropped = resize(cropped);
            fileIO.saveImg(cropped, pair.getValue());
            fileIO.saveTxt(regions, pair.getKey());
        }
    }

    // utils
    private List<Pair<Integer, Rectangle>> txtToRegions(List<String> description, int maxW, int maxH) {
        List<Pair<Integer, Rectangle>> rects = new ArrayList<>();

        for (String line : description) {
            List<String> coords = new LinkedList<>(Arrays.asList(line.split(" ")));

            int id = Integer.parseInt(coords.get(0));
            int width = (int) (Double.parseDouble(coords.get(3)) * maxW);
            int height = (int) (Double.parseDouble(coords.get(4)) * maxH);

            int x = (int) (Double.parseDouble(coords.get(1)) * maxW - width / 2);
            int y = (int) (Double.parseDouble(coords.get(2)) * maxH - height / 2);

            Rectangle rect = new Rectangle(x, y, width, height);

            Pair<Integer, Rectangle> pair = new Pair<>(id, rect);
            rects.add(pair);
        }

        return rects;
    }

    private List<String> regionsToTxt(List<Pair<Integer, Rectangle>> regions, int maxW, int maxH) {
        List<String> description = new ArrayList<>();

        for (Pair<Integer, Rectangle> region : regions) {
            int id = region.getKey();
            float x = (float) region.getValue().getCenterX() / maxW;
            float y = (float) region.getValue().getCenterY() / maxH;
            float width = (float) region.getValue().width / maxW;
            float height = (float) region.getValue().height / maxH;

            description.add(id + " " + x + " " + y + " " + width + " " + height);
        }

        return description;
    }

    private boolean isOverlapping(Rectangle r1, Rectangle r2) {
        Point tr1 = new Point(r1.x + r1.width, r1.y);
        Point bl1 = new Point(r1.x, r1.y + r1.height);
        Point tr2 = new Point(r2.x + r2.width, r2.y);
        Point bl2 = new Point(r2.x, r2.y + r2.height);

        if (tr1.y < bl2.y || bl1.y > tr2.y)
            return false;
        if (tr1.x < bl2.x || bl1.x > tr2.x)
            return false;

        return true;
    }

    private Rectangle merge(Rectangle r1, Rectangle r2) {
        Point tl1 = new Point(r1.x, r1.y);
        Point br1 = new Point(r1.x + r1.width, r1.y + r1.height);
        Point tl2 = new Point(r2.x, r2.y);
        Point br2 = new Point(r2.x + r2.width, r2.y + r2.height);

        Point tl = new Point(Math.min(tl1.x, tl2.x), Math.min(tl1.y, tl2.y));
        Point br = new Point(Math.max(br1.x, br2.x), Math.max(br1.y, br2.y));

        return new Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y);
    }

    // makes rectangle square
    private Rectangle square(Rectangle r) {
        Rectangle bound = new Rectangle(r.x, r.y, r.width, r.height);

        // converting it to 1:1
        if (bound.width != bound.height) {
            bound.width = Math.max(Math.max(bound.width, bound.height), size);
            bound.height = bound.width;
        }

        bound.width *= (offset + 1);
        bound.height *= (offset + 1);

        // fitting centers
        if (r.getCenterX() != bound.getCenterX() || r.getCenterY() != bound.getCenterY()) {
            bound.x -= bound.getCenterX() - r.getCenterX();
            bound.y -= bound.getCenterY() - r.getCenterY();
        }

        return bound;
    }

    // creates square rectangle centered
    private Rectangle square(int maxW, int maxH) {
        return new Rectangle((maxW - size) / 2, (maxH - size) / 2, size, size);
    }

    private Rectangle fit(Rectangle r, int maxW, int maxH) {
        Rectangle rect = new Rectangle(r.x, r.y, r.width, r.height);

        // assuming rectangle is squared
        int s = Math.min(Math.min(maxW, maxH), rect.width);
        rect.width = s;
        rect.height = s;

        if (rect.width == maxW || rect.x < 0)
            rect.x = 0;
        if (rect.height == maxH || rect.y < 0)
            rect.y = 0;
        if (rect.x + rect.width > maxW)
            rect.x -= rect.x + rect.width - maxW;
        if (rect.y + rect.height > maxH)
            rect.y -= rect.y + rect.height - maxH;

        return rect;
    }

    private Rectangle getBounds(List<Pair<Integer, Rectangle>> rects) {
        Rectangle bounds = null;

        if (rects.size() > 0) {
            for (Pair<Integer, Rectangle> rect : rects) {
                if (bounds == null)
                    bounds = rect.getValue();
                else {
                    bounds = merge(bounds, rect.getValue());
                }
            }
        }

        return bounds;
    }

    // transforms cartesian coordinates
    private Point transformCoords(Point coords, Point p) {
        return new Point(coords.x - p.x, coords.y - p.y);
    }

    private List<Pair<Integer, Rectangle>> transformRegions(List<Pair<Integer, Rectangle>> rects, Point p) {
        List<Pair<Integer, Rectangle>> rectsNew = new ArrayList<>();
        for (Pair<Integer, Rectangle> rect : rects) {
            Point r = new Point(rect.getValue().x, rect.getValue().y);
            Point rNew = transformCoords(r, p);

            Pair<Integer, Rectangle> rectNew = new Pair<>(rect.getKey(), new Rectangle(rNew.x, rNew.y, rect.getValue().width, rect.getValue().height));
            rectsNew.add(rectNew);
        }

        return rectsNew;
    }

    private BufferedImage resize(BufferedImage img) {
        Image tmp = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(size, size, img.getType());
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resized;
    }
}
