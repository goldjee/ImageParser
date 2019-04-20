package processors;

import processors.classes.MarkedImage;
import processors.classes.YoloPair;
import utils.ProgressMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 13.04.2019.
 */
public class Grayscaler extends Worker {

    public Grayscaler(YoloPair pair, String targetUrl, ProgressMonitor monitor) {
        super(pair, "gra", targetUrl, monitor);
    }

    @Override
    protected void work() {
        MarkedImage source = new MarkedImage(pair);

        // we will work only images containing objects
        if (source.getObjects().size() > 0) {
            List<MarkedImage> augmented = new ArrayList<>();

            augmented.add(source.grayscale());

            if (augmented.size() > 0) {
                for (int i = 0; i < augmented.size(); i++) {
                    save(augmented.get(i), i);
                }
            }
        }
    }
}
