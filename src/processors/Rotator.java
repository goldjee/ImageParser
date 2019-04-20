package processors;

import processors.classes.MarkedImage;
import processors.classes.YoloPair;
import utils.ProgressMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 13.04.2019.
 */
public class Rotator extends Worker {
    private final double angleBounds;
    private final int steps;

    public Rotator(YoloPair pair, double angleBounds, int steps, String targetUrl, ProgressMonitor monitor) {
        super(pair, "rot", targetUrl, monitor);
        this.angleBounds = angleBounds;
        this.steps = steps;
    }

    @Override
    protected void work() {
        MarkedImage source = new MarkedImage(pair);

        // we will work only images containing objects
        if (source.getObjects().size() > 0) {
            List<MarkedImage> augmented = new ArrayList<>(steps);

            double step = 2 * angleBounds / steps;
            double angle = (-1) * angleBounds;
            for (int i = 0; i < steps; i++) {
                augmented.add(source.rotate(angle));
                angle += step;
            }

            if (augmented.size() > 0) {
                for (int i = 0; i < augmented.size(); i++) {
                    save(augmented.get(i), i);
                }
            }
        }
    }
}
