package processors;

import processors.classes.YoloPair;
import utils.FileIO;
import utils.ProgressMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Ins on 16.03.2019.
 */
public class Balancer {

    // target object to background ratio
    private final float ratio = 1.0f;

    private final FileIO fileIO;
    private final ProgressMonitor monitor;

    public Balancer(ProgressMonitor monitor) {
        this.fileIO = FileIO.getInstance();
        this.monitor = monitor;
    }

    public void balance(List<YoloPair> pairs, boolean fromInput) {
        List<YoloPair> backgrounds = new ArrayList<>();

        // remains are pairs which should be preserved
        List<YoloPair> remains = new ArrayList<>(pairs);
        // removed are pairs pending for removal
        List<YoloPair> removed = new ArrayList<>();

        int objectCnt = 0, backgroundCnt = 0;
        for (YoloPair pair : remains) {
            if (pair.getTxt().length() == 0) {
                backgroundCnt++;
                backgrounds.add(pair);
            }
            else objectCnt++;
        }
        System.out.println("Objects: " + objectCnt);
        System.out.println("Backgrounds: " + backgroundCnt);

        float ratio = (float) objectCnt / backgroundCnt;
        if (ratio < this.ratio) {
            Random rnd = new Random();
            for (int i = 0; i < (int) ((float) objectCnt / this.ratio); i++) {
                int idx = rnd.nextInt(backgrounds.size() - 1);

                YoloPair pair = backgrounds.get(idx);
                if (pair != null) {
                    removed.add(pair);
                    remains.remove(pair);
                }
            }
        }

        // finally perform toProcessed and move operations
        if (fromInput) {
            monitor.setCntAll(remains.size());

            for (YoloPair pair : remains) {
                // toProcessed to output
                fileIO.toProcessed(pair.getTxt());
                fileIO.toProcessed(pair.getImg());

                monitor.increment();
            }
        }
        else {
            monitor.setCntAll(removed.size());

            for (YoloPair pair : removed) {
                fileIO.toRemoved(pair.getTxt());
                fileIO.toRemoved(pair.getImg());

                monitor.increment();
            }
        }
    }
}
