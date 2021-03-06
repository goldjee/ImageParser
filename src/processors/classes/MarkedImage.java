package processors.classes;

import utils.FileIO;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 13.04.2019.
 */
public class MarkedImage {

    public BufferedImage getImg() {
        return img;
    }

    public List<MarkedObject> getObjects() {
        return objects;
    }

    private BufferedImage img;
    private List<MarkedObject> objects;

    public MarkedImage(YoloPair pair) {
        FileIO fileIO = FileIO.getInstance();

        img = fileIO.readImg(pair.getImg().getAbsolutePath());
        List<String> yoloLines = fileIO.readTxt(pair.getTxt().getAbsolutePath());

        objects = new ArrayList<>(yoloLines.size());
        for (String yoloLine : yoloLines) {
            objects.add(new MarkedObject(yoloLine, img.getWidth(), img.getHeight()));
        }
    }

    public MarkedImage(BufferedImage img, Region region) {
        this.img = img;

        objects = new ArrayList<>(region.markedObjects.size());
        for (String yoloLine : region.toYoloList()) {
            objects.add(new MarkedObject(yoloLine, img.getWidth(), img.getHeight()));
        }
    }

    private MarkedImage(BufferedImage img, List<MarkedObject> objects) {
        this.img = img;
        this.objects = objects;
    }

    // splits one marked image into a set
    // if there are no objects in the image, we'll crop it to given size
    public List<MarkedImage> split(int minSize) {
        List<MarkedImage> markedImages = new ArrayList<>();

        List<Region> regions = new ArrayList<>();
        if (objects.size() > 0) {
            // if there are some regions
            // let's mark them all
            for (MarkedObject object : objects) {
                Region region = new Region(object);
                region.prepare(img.getWidth(), img.getHeight(), minSize);

                regions.add(region);
            }

            // now we have a list of all square regions for every object
            // let's try to merge overlapping and near regions
            regions = merge(regions);
        }
        else {
            // if it's background image
            regions.add(new Region(img.getWidth(), img.getHeight(), minSize));
        }

        // fool proof
        if (regions.size() > 0) {
            // another fool proof
            for (Region r : regions) {
                r.prepare(img.getWidth(), img.getHeight(), minSize);
            }

            // saving data
            for (Region region : regions) {
                BufferedImage croppedImage = img.getSubimage(region.tl().x, region.tl().y, region.width(), region.height());
                markedImages.add(new MarkedImage(croppedImage, region));
            }
        }

        return markedImages;
    }

    private List<Region> merge(List<Region> regions) {
        List<Region> result = new ArrayList<>(regions);

        boolean foundIntersection = false;
        do {
            foundIntersection = false;
            for (int i = 0; i < result.size(); i++) {
                Region current = result.get(i);

                for (int j = i + 1; j < result.size(); j++) {
                    if (current.isNear(result.get(j))) {
                        foundIntersection = true;

                        current = current.merge(result.get(j));
                        result.remove(j--);
                    }
                }
                result.set(i, current);
            }
        }
        while (foundIntersection);

        return result;
    }

    // if image is smaller than given size, resizes it
    public void resize(int minSize) {
        double aspectRatio = (double) img.getWidth() / (double) img.getHeight();
        int newW = img.getWidth(),
            newH = img.getHeight();

        if (img.getWidth() <= img.getHeight() && img.getWidth() < minSize) {
            newW = minSize;
            newH = (int) ((double) newW / aspectRatio);
        }
        else if (img.getWidth() > img.getHeight() && img.getHeight() < minSize) {
            newH = minSize;
            newW = (int) ((double) newH * aspectRatio);
        }

        Image newImg = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(newW, newH, img.getType());
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(newImg, 0, 0, null);
        g2d.dispose();

        img = resized;
    }

    // rotates image preserving marks
    public MarkedImage rotate(double angle) {
        angle = Math.toRadians(angle);
        AffineTransform tx = AffineTransform.getRotateInstance(angle, img.getWidth() / 2, img.getHeight() / 2);

        return applyAffineTransformation(tx);
    }

    // flips image preserving marks
    // direction:
    // 0 - vertical
    // 1 - horizontal
    public MarkedImage flip(int direction) {
        AffineTransform tx;

        if (direction == 0)
            tx = AffineTransform.getScaleInstance(1, -1);
        else if (direction == 1)
            tx = AffineTransform.getScaleInstance(-1, 1);
        else return this;

        return applyAffineTransformation(tx);
    }

    public MarkedImage grayscale() {
        BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = newImage.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        List<MarkedObject> newObjects = new ArrayList<>(objects);

        return new MarkedImage(newImage, newObjects);
    }

    private MarkedImage applyAffineTransformation(AffineTransform tx) {
        BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g2d = newImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(img, tx, null);
        g2d.dispose();

        List<MarkedObject> newObjects = new ArrayList<>(objects.size());

        for (MarkedObject o : objects) {
            Shape shape = new RoundRectangle2D.Double(o.bounds.x, o.bounds.y, o.bounds.width, o.bounds.height, o.bounds.width / 4, o.bounds.height / 4);
            shape = tx.createTransformedShape(shape);
            Rectangle newBounds = shape.getBounds();

            // check if bounds are inside the image
            if (newBounds.x < 0) {
                newBounds.width -= Math.abs(newBounds.x);
                newBounds.x = 0;
            }
            if (newBounds.y < 0) {
                newBounds.height -= Math.abs(newBounds.y);
                newBounds.y = 0;
            }
            if (newBounds.x + newBounds.width > newImage.getWidth()) {
                newBounds.width -= Math.abs(newBounds.x + newBounds.width - newImage.getWidth());
            }
            if (newBounds.y + newBounds.height > newImage.getHeight()) {
                newBounds.height -= Math.abs(newBounds.y + newBounds.height - newImage.getHeight());
            }
            if (newBounds.x >= newImage.getWidth() || newBounds.y >= newImage.getHeight())
                continue;

            newObjects.add(new MarkedObject(o.c, newBounds));
        }

        return new MarkedImage(newImage, newObjects);
    }
}
