package processors;

import processors.classes.MarkedImage;
import processors.classes.Region;
import processors.classes.YoloPair;
import utils.FileIO;
import utils.ProgressMonitor;

import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Created by Ins on 13.04.2019.
 */
public class Cropper implements Runnable {

    // size of resized image in pixels
    private final int size;

    private final FileIO fileIO;
    private final ProgressMonitor monitor;

    private YoloPair pair = null;

    public Cropper(YoloPair pair, int size, ProgressMonitor monitor) {
        this.pair = pair;
        this.size = size;
        this.fileIO = FileIO.getInstance();
        this.monitor = monitor;
    }

    @Override
    public void run() {
        crop(pair);
        monitor.increment();
    }

    private void crop(YoloPair pair) {
        List<MarkedImage> markedImages = new MarkedImage(pair).split(size);

        if (markedImages.size() > 0) {
            for (int i = 0; i < markedImages.size(); i++) {
                MarkedImage markedImage = markedImages.get(i);
                markedImage.resize(size);

                Region region = new Region(markedImage.getObjects(), new Rectangle(markedImage.getImg().getWidth(), markedImage.getImg().getHeight()));

                // saving data
                List<String> coords = region.toYoloList();

                String basePath = pair.getTxt().getParent() + fileIO.SEPARATOR;
                String txtPath = basePath + fileIO.getFileNameWithoutExtension(pair.getTxt()) + "_" + i + "." + fileIO.getFileExtension(pair.getTxt());
                String imgPath = basePath + fileIO.getFileNameWithoutExtension(pair.getImg()) + "_" + i + "." + fileIO.getFileExtension(pair.getImg());

                fileIO.saveTxt(coords, new File(txtPath));
                fileIO.saveImg(markedImage.getImg(), new File(imgPath));
            }
        }
    }
}
