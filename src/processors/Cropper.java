package processors;

import processors.classes.MarkedImage;
import processors.classes.MarkedObject;
import processors.classes.Region;
import utils.FileIO;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 16.03.2019.
 */
public class Cropper implements Runnable {

    // size resized image in pixels
    private static final int size = 320;

    private FileIO fileIO;

    private MarkedImage pair = null;

    public Cropper(FileIO fileIO) {
        this.fileIO = fileIO;
    }

    public void set(MarkedImage pair) {
        this.pair = pair;
    }

    @Override
    public void run() {
        crop(pair);
    }

    private void crop(MarkedImage pair) {
        List<String> txt = fileIO.readTxt(pair.getTxt());
        BufferedImage img = fileIO.readImg(pair.getImg());

        if (txt != null && img != null) {
            List<Region> regions = new ArrayList<>();
            if (txt.size() > 0) {
                // if there are some regions
                // let's mark them all
                for (String line : txt) {
                    MarkedObject o = new MarkedObject(line, img.getWidth(), img.getHeight());
                    Region region = new Region(o);
                    region.prepare(img.getWidth(), img.getHeight(), size);

                    regions.add(region);
                }

                // now we have a list of all square regions for every object
                // let's try to reduce it merging overlapping and near regions
                regions = merge(regions);
            }
            else {
                // if it's background image
                regions.add(new Region(img.getWidth(), img.getHeight(), size));
            }

            // fool proof
            if (regions.size() > 0) {
                // another fool proof
                for (Region r : regions) {
                    r.prepare(img.getWidth(), img.getHeight(), size);
                }

                // saving data
                for (int i = 0; i < regions.size(); i++) {
                    Region r = regions.get(i);
                    List<String> coords = r.toYoloList();

                    String basePath = pair.getTxt().getParent() + "\\";
                    String txtPath = basePath + fileIO.getFileNameWithoutExtension(pair.getTxt()) + "_" + i + "." + fileIO.getFileExtension(pair.getTxt());
                    String imgPath = basePath + fileIO.getFileNameWithoutExtension(pair.getImg()) + "_" + i + "." + fileIO.getFileExtension(pair.getImg());

                    BufferedImage cropped = img.getSubimage(r.tl().x, r.tl().y, r.width(), r.height());
                    cropped = resize(cropped);

                    fileIO.saveTxt(coords, new File(txtPath));
                    fileIO.saveImg(cropped, new File(imgPath));
                }
            }
        }
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

    private BufferedImage resize(BufferedImage img) {
        Image tmp = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(size, size, img.getType());
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resized;
    }
}
