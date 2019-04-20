package processors;

import processors.classes.MarkedImage;
import processors.classes.MarkedObject;
import processors.classes.Region;
import utils.FileIO;
import utils.ProgressMonitor;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Ins on 20.04.2019.
 */
public class Generator implements Runnable {
    private final String objectUrl;
    private final List<String> backgroundUrls;
    private final double scaleFrom;
    private final double scaleTo;
    private final int steps;
    private final double visiblePart;
    private final String targetUrl;

    private final ProgressMonitor monitor;

    public Generator(String objectUrl, List<String> backgroundUrls, double scaleFrom, double scaleTo, int steps, double visiblePart, String targetUrl, ProgressMonitor monitor) {
        this.objectUrl = objectUrl;
        this.backgroundUrls = backgroundUrls;
        this.scaleFrom = scaleFrom;
        this.scaleTo = scaleTo;
        this.steps = steps;
        this.visiblePart = visiblePart;
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
        BufferedImage object = fileIO.readImg(objectUrl);

        for (int i = 0; i < backgroundUrls.size(); i++) {
            String backgroundUrl = backgroundUrls.get(i);
            BufferedImage background = fileIO.readImg(backgroundUrl);

            Random rnd = new Random();
            for (int j = 0; j < steps; j++) {
                double scale = scaleFrom + (scaleTo - scaleFrom) * rnd.nextDouble();
                BufferedImage scaledObject = scale(object, scale);

                int boundXFrom = (int) ((-1) * (1d - visiblePart) * (double) scaledObject.getWidth());
                int boundXTo = (int) ((double) background.getWidth() - visiblePart * (double) scaledObject.getWidth());
                int xFrom = Math.min(boundXFrom, boundXTo);
                int xTo = Math.max(boundXFrom, boundXTo);

                int boundYFrom = (int) (-2d / 3d * (double) scaledObject.getHeight());
                int boundYTo = (int) ((double) background.getHeight() - (double) scaledObject.getHeight() / 3d);
                int yFrom = Math.min(boundYFrom, boundYTo);
                int yTo = Math.max(boundYFrom, boundYTo);

                int x = rnd.nextInt(xTo - xFrom) + xFrom;
                int y = rnd.nextInt(yTo - yFrom) + yFrom;

                MarkedImage markedImage = placeObject(scaledObject, background, x, y);
                String name = fileIO.getFileNameWithoutExtension(new File(objectUrl)) +
                        fileIO.getFileNameWithoutExtension(new File(backgroundUrl)) +
                        "_gen_" + (i + j);

                save(markedImage, name);
            }
        }
    }

    private MarkedImage placeObject(BufferedImage obj, BufferedImage bck, int x, int y) {
        BufferedImage img = new BufferedImage(bck.getWidth(), bck.getHeight(), bck.getType());
        Rectangle imageRect = new Rectangle(bck.getWidth(), bck.getHeight());
        Rectangle objectRect = new Rectangle(obj.getWidth(), obj.getHeight());

        // calculate what part of object should be rendered
        Rectangle intersection = objectRect.intersection(new Rectangle(-x, -y, imageRect.width, imageRect.height));
        BufferedImage objSub = obj.getSubimage(intersection.x, intersection.y, intersection.width, intersection.height);
        // calculate bounds of subobject containing nonempty pixels
        // it is defined in coordinates relative to object image
        Rectangle objectBounds = getNonemptyBounds(objSub);
        // make coordinates relative to original object image and then to background
        objectBounds.x += intersection.x + x;
        objectBounds.y += intersection.y + y;

        Graphics2D g2d = img.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g2d.drawImage(bck, 0, 0, null);
        g2d.drawImage(obj, x, y, null);
        g2d.dispose();

        MarkedObject markedObject = new MarkedObject(0, objectBounds);
        List<MarkedObject> objects = new ArrayList<>();
        objects.add(markedObject);

        Region region = new Region(objects, new Rectangle(imageRect.x, imageRect.y, imageRect.width, imageRect.height));

        MarkedImage out = new MarkedImage(img, region);

        return out;
    }

    public static BufferedImage scale(BufferedImage img, double scale) {
        AffineTransform tx = AffineTransform.getScaleInstance(scale, scale);

        Rectangle bounds = new Rectangle(0, 0, img.getWidth(), img.getHeight());
        bounds = tx.createTransformedShape(bounds).getBounds();

        BufferedImage out = new BufferedImage(bounds.width, bounds.height, img.getType());
        Graphics2D g2d = out.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.drawImage(img, tx, null);
        g2d.dispose();

        return out;
    }

    private void save(MarkedImage markedImage, String name) {
        Region region = new Region(markedImage.getObjects(), new Rectangle(markedImage.getImg().getWidth(), markedImage.getImg().getHeight()));

        // saving data
        List<String> coords = region.toYoloList();

        FileIO fileIO = FileIO.getInstance();

        fileIO.saveTxt(coords, name + ".txt", targetUrl);
        fileIO.saveImg(markedImage.getImg(), name + ".jpg", targetUrl);
    }

    private Rectangle getNonemptyBounds(BufferedImage img) {
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

        return new Rectangle(tl.x, tl.y, Math.abs(br.x - tl.x), Math.abs(br.y - tl.y));
    }

    private boolean isTransparent(BufferedImage img, int x, int y) {
        int pixel = img.getRGB(x, y);
        return (pixel >> 24) == 0x00;
    }
}
