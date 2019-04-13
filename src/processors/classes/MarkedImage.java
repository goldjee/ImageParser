package processors.classes;

import utils.FileIO;

import java.awt.*;
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

        img = fileIO.readImg(pair.getImg());
        List<String> yoloLines = fileIO.readTxt(pair.getTxt());

        objects = new ArrayList<>(yoloLines.size());
        for (String yoloLine : yoloLines) {
            objects.add(new MarkedObject(yoloLine, img.getWidth(), img.getHeight()));
        }
    }

    private MarkedImage(BufferedImage img, Region region) {
        this.img = img;

        objects = new ArrayList<>(region.markedObjects.size());
        for (String yoloLine : region.toYoloList()) {
            objects.add(new MarkedObject(yoloLine, img.getWidth(), img.getHeight()));
        }
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
            for (int i = 0; i < regions.size(); i++) {
                Region region = regions.get(i);
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

        BufferedImage newImg;
        if (img.getWidth() <= img.getHeight() && img.getWidth() < minSize) {
            newW = minSize;
            newH = (int) ((double) newW / aspectRatio);
        }
        else if (img.getWidth() > img.getHeight() && img.getHeight() < minSize) {
            newH = minSize;
            newW = (int) ((double) newH * aspectRatio);
        }

        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(newW, newH, img.getType());
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        img = resized;
    }
}
