package processors;

import processors.classes.MarkedImage;
import processors.classes.Region;
import processors.classes.YoloPair;
import utils.FileIO;
import utils.ProgressMonitor;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 13.04.2019.
 */
public class Augmentor implements Runnable {
    private final FileIO fileIO;
    private final ProgressMonitor monitor;

    private YoloPair pair;

    private boolean rotate = false;
    private double angleBounds;
    private int steps;

    private boolean flip = false;

    private boolean grayscale = false;

    public Augmentor(YoloPair pair, ProgressMonitor monitor) {
        this.pair = pair;
        this.fileIO = FileIO.getInstance();
        this.monitor = monitor;
    }

    public void setRotate(double angleBounds, int steps) {
        this.rotate = true;
        this.angleBounds = angleBounds;
        this.steps = steps;
    }

    public void setFlip() {
        this.flip = true;
    }

    public void setGrayscale() {
        this.grayscale = true;
    }

    @Override
    public void run() {
        augment(pair);
        monitor.increment();
    }

    private void augment(YoloPair pair) {
        MarkedImage source = new MarkedImage(pair);

        // we will augment only images containing objects
        if (source.getObjects().size() > 0) {
            List<MarkedImage> augmented = new ArrayList<>(steps);
            List<MarkedImage> augmentationStep = new ArrayList<>();

            // rotational augmentations
            if (rotate) {
                double step = 2 * angleBounds / steps;
                double angle = (-1) * angleBounds;
                for (int i = 0; i < steps; i++) {
                    augmentationStep.add(source.rotate(angle));
                    angle += step;
                }
            }
            augmented.addAll(augmentationStep);

            augmentationStep.clear();
            // flip augmentations
            if (flip) {
                for (MarkedImage aug : augmented) {
                    augmentationStep.add(aug.flip(0));
                    augmentationStep.add(aug.flip(1));
                }

                augmentationStep.add(source.flip(0));
                augmentationStep.add(source.flip(1));
            }
            augmented.addAll(augmentationStep);

            augmentationStep.clear();
            if (grayscale) {
                for (MarkedImage aug : augmented) {
                    augmentationStep.add(aug.grayscale());
                    augmentationStep.add(aug.grayscale());
                }

                augmentationStep.add(source.grayscale());
            }
            augmented.addAll(augmentationStep);

            if (augmented.size() > 0) {
                for (int i = 0; i < augmented.size(); i++) {
                    MarkedImage markedImage = augmented.get(i);

                    Region region = new Region(markedImage.getObjects(), new Rectangle(markedImage.getImg().getWidth(), markedImage.getImg().getHeight()));

                    // saving data
                    List<String> coords = region.toYoloList();

                    String basePath = pair.getTxt().getParent() + FileIO.SEPARATOR;
                    String txtPath = basePath + fileIO.getFileNameWithoutExtension(pair.getTxt()) + "_aug_" + i + "." + fileIO.getFileExtension(pair.getTxt());
                    String imgPath = basePath + fileIO.getFileNameWithoutExtension(pair.getImg()) + "_aug_" + i + "." + fileIO.getFileExtension(pair.getImg());

                    fileIO.saveTxt(coords, new File(txtPath));
                    fileIO.saveImg(markedImage.getImg(), new File(imgPath));
                }
            }
        }
    }
}
