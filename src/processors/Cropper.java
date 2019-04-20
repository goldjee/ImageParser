package processors;

import processors.classes.MarkedImage;
import processors.classes.YoloPair;
import utils.ProgressMonitor;

import java.util.List;

/**
 * Created by Ins on 13.04.2019.
 */
public class Cropper extends Worker {
    private final int size;

    public Cropper(YoloPair pair, int size, String targetUrl, ProgressMonitor monitor) {
        super(pair, "crp", targetUrl, monitor);
        this.size = size;
    }

    @Override
    protected void work() {
        List<MarkedImage> markedImages = new MarkedImage(pair).split(size);

        if (markedImages.size() > 0) {
            for (int i = 0; i < markedImages.size(); i++) {
                MarkedImage markedImage = markedImages.get(i);
                markedImage.resize(size);

                save(markedImage, i);
            }
        }
    }
}
