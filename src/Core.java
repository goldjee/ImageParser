import processors.Balancer;
import processors.Cropper;
import processors.classes.MarkedImage;
import utils.FileIO;
import utils.ProgressMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ins on 18.03.2019.
 */
public class Core {
    private final FileIO fileIO;
    private final ProgressMonitor monitor;

    List<MarkedImage> pairs;

    private volatile int cntDone = 0,
        cntAll = 0;

    public Core() {
        fileIO = new FileIO();
        monitor = new ProgressMonitor();
    }

    public void crop() {
        System.out.println("Cropping started");
        pairs = buildPairs(fileIO.BASE_DIR);

        monitor.setCntAll(pairs.size());

        // pooling threads ensurin not to burn the machine
        ExecutorService es = Executors.newFixedThreadPool(8);
        List<Thread> croppers = new ArrayList<>(cntAll);
        for (MarkedImage pair : pairs) {
            Cropper c = new Cropper(pair, fileIO, monitor);
            Thread t = new Thread(c);
            t.setDaemon(true);
            croppers.add(t);
//            t.start();

            es.submit(t);
        }
        // wait for recent task to finish
        waitTasks();
        es.shutdown();

        System.out.println("Cropping done");
    }

    public void balance() {
        System.out.println("Balancing started");
        Balancer balancer = new Balancer(fileIO, monitor);

        // we'll try to balance dataset in processed dir
        pairs = buildPairs(fileIO.PROCESSED_DIR);

        if (pairs.size() > 0) {
            balancer.balance(pairs, false);
        }
        else {
            // if it's empty, okay. we'll try base dir
            pairs = buildPairs(fileIO.BASE_DIR);
            // and toProcessed results to output btw
            balancer.balance(pairs, true);
        }

        waitTasks();

        System.out.println("Balancing done");
    }

    public void cleanup() {
        fileIO.clean(fileIO.PROCESSED_DIR);
        fileIO.clean(fileIO.REMOVED_DIR);
    }

    private void waitTasks() {
        while (!monitor.isFinished()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<MarkedImage> buildPairs(String inputDir) {
//        System.out.println("Listing directory");
        List<MarkedImage> pairs = new ArrayList<>();

        List<File> dirContents = fileIO.list(inputDir);
        List<File> txts = fileIO.filterExtension(dirContents, "txt", true);
        List<File> imgs = fileIO.filterExtension(dirContents, "txt", false);

        for (File txt : txts) {
            for (File img : imgs) {
                if (fileIO.getFileNameWithoutExtension(txt).equals(fileIO.getFileNameWithoutExtension(img))) {
                    MarkedImage pair = new MarkedImage(txt, img);
                    pairs.add(pair);
                    break;
                }
            }
        }

        return pairs;
    }
}
