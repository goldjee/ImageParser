package processors;

import processors.classes.MarkedImage;
import utils.FileIO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Ins on 16.03.2019.
 */
public class Balancer {

    // target object to background ratio
    private static final float ratio = 1.0f;

    private final FileIO fileIO;

    public Balancer(FileIO fileIO) {
        this.fileIO = fileIO;
    }

    public void balance(List<MarkedImage> pairs, boolean fromInput) {
        List<MarkedImage> backgrounds = new ArrayList<>();

        // remains are pairs which should be preserved
        List<MarkedImage> remains = new ArrayList<>(pairs);
        // removed are pairs pending for removal
        List<MarkedImage> removed = new ArrayList<>();

        int objectCnt = 0, backgroundCnt = 0;
        for (MarkedImage pair : remains) {
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

                MarkedImage pair = backgrounds.get(idx);
                if (pair != null) {
                    removed.add(pair);
                    remains.remove(pair);
                }
            }
        }

        // finally perform toProcessed and move operations
        if (fromInput) {
            for (MarkedImage pair : remains) {
                // toProcessed to output
                fileIO.toProcessed(pair.getTxt());
                fileIO.toProcessed(pair.getImg());
            }
        }
        else {
            for (MarkedImage pair : removed) {
                fileIO.toRemoved(pair.getTxt());
                fileIO.toRemoved(pair.getImg());
            }
        }
    }
}
