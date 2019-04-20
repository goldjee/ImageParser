package processors;

import processors.classes.MarkedImage;
import processors.classes.YoloPair;
import utils.ProgressMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 13.04.2019.
 */
public class Flipper extends Worker {

    public Flipper(YoloPair pair, double angleBounds, int steps, String targetUrl, ProgressMonitor monitor) {
        super(pair, "flp", targetUrl, monitor);
    }

    @Override
    protected void work() {
        MarkedImage source = new MarkedImage(pair);

        // we will work only images containing objects
        if (source.getObjects().size() > 0) {
            List<MarkedImage> augmented = new ArrayList<>();

            augmented.add(source.flip(0));
            augmented.add(source.flip(1));

            if (augmented.size() > 0) {
                for (int i = 0; i < augmented.size(); i++) {
                    save(augmented.get(i), i);
                }
            }
        }
    }
}
