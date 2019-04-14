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

    private YoloPair pair = null;
    double angleBounds;
    int steps;

    public Augmentor(YoloPair pair, double angleBounds, int steps, ProgressMonitor monitor) {
        this.pair = pair;
        this.angleBounds = angleBounds;
        this.steps = steps;
        this.fileIO = FileIO.getInstance();
        this.monitor = monitor;
    }

    @Override
    public void run() {
        augment(pair);
        monitor.increment();
    }

    private void augment(YoloPair pair) {
        MarkedImage source = new MarkedImage(pair);
        List<MarkedImage> augmented = new ArrayList<>(steps);

        // we will augment only images containing objects
        if (source.getObjects().size() > 0) {
            // rotational augmentations
            double step = 2 * angleBounds / steps;
            double angle = (-1) * angleBounds;
            for (int i = 0; i < steps; i++) {
                augmented.add(source.rotate(angle));
                angle += step;
            }
        }

        if (augmented.size() > 0) {
            for (int i = 0; i < augmented.size(); i++) {
                MarkedImage markedImage = augmented.get(i);

                Region region = new Region(markedImage.getObjects(), new Rectangle(markedImage.getImg().getWidth(), markedImage.getImg().getHeight()));

                // saving data
                List<String> coords = region.toYoloList();

                String basePath = pair.getTxt().getParent() + fileIO.SEPARATOR;
                String txtPath = basePath + fileIO.getFileNameWithoutExtension(pair.getTxt()) + "_aug_" + i + "." + fileIO.getFileExtension(pair.getTxt());
                String imgPath = basePath + fileIO.getFileNameWithoutExtension(pair.getImg()) + "_aug_" + i + "." + fileIO.getFileExtension(pair.getImg());

                fileIO.saveTxt(coords, new File(txtPath));
                fileIO.saveImg(markedImage.getImg(), new File(imgPath));
            }
        }
    }
}
