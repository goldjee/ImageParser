package processors;

import utils.FileIO;
import utils.ProgressMonitor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by Ins on 19.04.2019.
 */
public class GeneratorCropper implements Runnable {
    private final String objectUrl;
    private final String targetUrl;

    private final ProgressMonitor monitor;

    public GeneratorCropper(String objectUrl, String targetUrl, ProgressMonitor monitor) {
        this.objectUrl = objectUrl;
        this.targetUrl = targetUrl;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        generate();
        monitor.increment();
    }

    private void generate() {
        FileIO fileIO = FileIO.getInstance();

        BufferedImage img = crop(fileIO.readImg(objectUrl));
        fileIO.saveImg(img, (new File(objectUrl)).getName(), targetUrl);
    }

    private BufferedImage crop(BufferedImage img) {
        // getting object bounding box
        // we assume background is transparent
        Point tl = new Point(img.getWidth() / 2, img.getHeight() / 2);
        Point br = new Point(img.getWidth() / 2, img.getHeight() / 2);
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                if (!isTransparent(img, i, j)) {
                    if (tl.x > i) tl.x = i;
                    if (tl.y > j) tl.y = j;

                    if (br.x < i) br.x = i;
                    if (br.y < j) br.y = j;
                }
            }
        }
        BufferedImage imgCropped = img.getSubimage(tl.x, tl.y, Math.abs(br.x - tl.x), Math.abs(br.y - tl.y));

        return imgCropped;
    }

    private boolean isTransparent(BufferedImage img, int x, int y) {
        int pixel = img.getRGB(x, y);
        return (pixel >> 24) == 0x00;
    }
}
