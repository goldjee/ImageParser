package processors;

import processors.classes.MarkedImage;
import processors.classes.Region;
import processors.classes.YoloPair;
import utils.FileIO;
import utils.ProgressMonitor;

import java.awt.*;
import java.util.List;

/**
 * Created by Ins on 20.04.2019.
 */
abstract class Worker implements Runnable {
    private final String operation;

    protected final YoloPair pair;
    private final String targetUrl;
    private final ProgressMonitor monitor;

    Worker(YoloPair pair, String operation, String targetUrl, ProgressMonitor monitor) {
        this.pair = pair;
        this.operation = operation;
        this.targetUrl = targetUrl;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        work();
        monitor.increment();
    }

    protected abstract void work();

    protected void save(MarkedImage augmented, int index) {
        Region region = new Region(augmented.getObjects(), new Rectangle(augmented.getImg().getWidth(), augmented.getImg().getHeight()));

        // saving data
        List<String> coords = region.toYoloList();

        FileIO fileIO = FileIO.getInstance();

        String txtName = fileIO.getFileNameWithoutExtension(pair.getTxt()) + "_" + operation + "_" + index + "." + fileIO.getFileExtension(pair.getTxt());
        String imgName = fileIO.getFileNameWithoutExtension(pair.getImg()) + "_" + operation + "_" + index + "." + fileIO.getFileExtension(pair.getImg());

        fileIO.saveTxt(coords, txtName, targetUrl);
        fileIO.saveImg(augmented.getImg(), imgName, targetUrl);
    }
}
