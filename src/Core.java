import processors.Augmentor;
import processors.Balancer;
import processors.Cropper;
import processors.classes.YoloPair;
import utils.FileIO;
import utils.PairHandler;
import utils.ProgressMonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Ins on 18.03.2019.
 */
public class Core {
    private final FileIO fileIO;
    private final PairHandler pairHandler;
    private final ProgressMonitor monitor;

    private volatile int cntDone = 0,
        cntAll = 0;

    public Core() {
        fileIO = FileIO.getInstance();
        pairHandler = new PairHandler();
        monitor = new ProgressMonitor();
    }

    public void crop(int size) {
        System.out.println("Cropping started");
        List<YoloPair> pairs = pairHandler.getPairs(fileIO.BASE_DIR, pairHandler.FILTER_MARKED);

        monitor.setCntAll(pairs.size());

        // pooling threads ensuring not to burn the machine
        ExecutorService es = Executors.newFixedThreadPool(8);
        List<Thread> croppers = new ArrayList<>(cntAll);
        for (YoloPair pair : pairs) {
            Cropper c = new Cropper(pair, size, monitor);
            Thread t = new Thread(c);
            t.setDaemon(true);
            croppers.add(t);

            es.submit(t);
        }
        // wait for recent task to finish
        waitTasks();
        es.shutdown();

        System.out.println("Cropping done");
    }

    public void augment(boolean augmentRotate, double angleBounds, int steps, boolean augmentFlip) {
        System.out.println("Augmentation started");
        List<YoloPair> pairs = pairHandler.getPairs(fileIO.PROCESSED_DIR, pairHandler.FILTER_MARKED);

        if (pairs.size() == 0) {
            pairs = pairHandler.getPairs(fileIO.BASE_DIR, pairHandler.FILTER_MARKED);
        }

        monitor.setCntAll(pairs.size());

        // pooling threads ensuring not to burn the machine
        ExecutorService es = Executors.newFixedThreadPool(8);
        List<Thread> augmentors = new ArrayList<>(cntAll);
        for (YoloPair pair : pairs) {
            Augmentor a = new Augmentor(pair, monitor);
            if (augmentRotate)
                a.setRotate(angleBounds, steps);
            if (augmentFlip)
                a.setFlip();

            Thread t = new Thread(a);
            t.setDaemon(true);
            augmentors.add(t);

            es.submit(t);
        }
        // wait for recent task to finish
        waitTasks();
        es.shutdown();

        System.out.println("Augmentation done");
    }

    public void balance() {
        System.out.println("Balancing started");
        Balancer balancer = new Balancer(monitor);

        // we'll try to balance dataset in processed dir
        List<YoloPair> pairs = pairHandler.getPairs(fileIO.PROCESSED_DIR, pairHandler.FILTER_MARKED);

        if (pairs.size() > 0) {
            balancer.balance(pairs, false);
        }
        else {
            // if it's empty, okay. we'll try base dir
            pairs = pairHandler.getPairs(fileIO.BASE_DIR, pairHandler.FILTER_MARKED);
            // and toProcessed results to output btw
            balancer.balance(pairs, true);
        }

        waitTasks();

        System.out.println("Balancing done");
    }

    public void removeEmpty() {
        System.out.println("Empty removal started");
        List<YoloPair> pairs = pairHandler.getPairs(fileIO.PROCESSED_DIR, pairHandler.FILTER_EMPTY);

        // if there are empty pairs in processed dir, we will remove them
        if (pairs.size() > 0) {
            monitor.setCntAll(pairs.size());

            for (YoloPair pair : pairs) {
                fileIO.toRemoved(pair.getImg());
                if (pair.getTxt() != null)
                    fileIO.toRemoved(pair.getTxt());

                monitor.increment();
            }
        }
        // or we can seek input dir and move marked to processed
        else {
            pairs = pairHandler.getPairs(fileIO.BASE_DIR, pairHandler.FILTER_MARKED_NONEMPTY);

            if (pairs.size() > 0) {
                monitor.setCntAll(pairs.size());

                for (YoloPair pair : pairs) {
                    fileIO.toProcessed(pair.getImg());
                    if (pair.getTxt() != null)
                        fileIO.toProcessed(pair.getTxt());

                    monitor.increment();
                }
            }
        }

        waitTasks();
        System.out.println("Empty removal done");
    }

    public void removeUnmarked() {
        System.out.println("Unmarked removal started");
        List<YoloPair> pairs = pairHandler.getPairs(fileIO.PROCESSED_DIR, pairHandler.FILTER_UNMARKED);

        // if there are unmarked pairs in processed dir, we will remove them
        if (pairs.size() > 0) {
            monitor.setCntAll(pairs.size());

            for (YoloPair pair : pairs) {
                fileIO.toRemoved(pair.getImg());
                if (pair.getTxt() != null)
                    fileIO.toRemoved(pair.getTxt());

                monitor.increment();
            }
        }
        // or we can seek input dir and move marked to processed
        else {
            pairs = pairHandler.getPairs(fileIO.BASE_DIR, pairHandler.FILTER_MARKED);

            if (pairs.size() > 0) {
                monitor.setCntAll(pairs.size());

                for (YoloPair pair : pairs) {
                    fileIO.toProcessed(pair.getImg());
                    if (pair.getTxt() != null)
                        fileIO.toProcessed(pair.getTxt());

                    monitor.increment();
                }
            }
        }

        waitTasks();
        System.out.println("Unmarked removal done");
    }

    public void cleanup() {
        System.out.println("Cleanup started");

        fileIO.clean(fileIO.PROCESSED_DIR);
        fileIO.clean(fileIO.REMOVED_DIR);

        System.out.println("Cleanup done");
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
}
