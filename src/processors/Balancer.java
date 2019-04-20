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
    private final List<YoloPair> pairs;
    private final double ratio;
    private final String targetUrl;

    private final ProgressMonitor monitor;

    public Balancer(List<YoloPair> pairs, double ratio, String targetUrl, ProgressMonitor monitor) {
        this.pairs = pairs;
        this.ratio = ratio;
        this.targetUrl = targetUrl;
        this.monitor = monitor;
    }

    public void balance() {
        List<YoloPair> backgrounds = new ArrayList<>();

        int objectCnt = 0, backgroundCnt = 0;
        for (YoloPair pair : pairs) {
            if (pair.getTxt().length() == 0) {
                backgroundCnt++;
                backgrounds.add(pair);
            }
            else objectCnt++;
        }
        System.out.println("Objects: " + objectCnt);
        System.out.println("Backgrounds: " + backgroundCnt);

        double ratio = (double) objectCnt / (double) backgroundCnt;
        if (ratio < this.ratio) {
            int toRemoveCnt = (int) ((double) objectCnt / this.ratio);
            monitor.setCntAll(toRemoveCnt);
            Random rnd = new Random();
            FileIO fileIO = FileIO.getInstance();

            for (int i = 0; i < toRemoveCnt; i++) {
                int idx = rnd.nextInt(backgrounds.size() - 1);

                YoloPair pair = backgrounds.get(idx);
                if (pair != null) {
                    fileIO.move(pair.getTxt(), targetUrl);
                    fileIO.move(pair.getImg(), targetUrl);

                    backgrounds.remove(pair);
                    monitor.increment();
                }
            }
        }
    }
}
