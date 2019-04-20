package processors;

import processors.classes.YoloPair;
import utils.FileIO;
import utils.ProgressMonitor;

/**
 * Created by Ins on 20.04.2019.
 */
public class Remover implements Runnable {
    private final YoloPair pair;
    private final String targetUrl;

    private final ProgressMonitor monitor;

    public Remover(YoloPair pair, String targetUrl, ProgressMonitor monitor) {
        this.pair = pair;
        this.targetUrl = targetUrl;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        remove();
        monitor.increment();
    }

    private void remove() {
        FileIO fileIO = FileIO.getInstance();

        if (pair.getTxt() != null) fileIO.move(pair.getTxt(), targetUrl);
        if (pair.getImg() != null) fileIO.move(pair.getImg(), targetUrl);
    }
}
