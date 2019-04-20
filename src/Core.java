import processors.*;
import processors.classes.YoloPair;
import utils.Config;
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
class Core {
    private final FileIO fileIO;
    private final PairHandler pairHandler;
    private final ProgressMonitor monitor;

    public Core() {
        fileIO = FileIO.getInstance();
        pairHandler = new PairHandler();
        monitor = new ProgressMonitor();
    }

    public void start() {
        try {
            Config config = Config.getInstance();

            if (config.isClean()) {
                System.out.println("Cleanup started");
                clean(config.getCleanerSource());
            }

            if (config.isCrop()) {
                System.out.println("Cropping started");
                crop(config.getCropperSource(), config.getCropperSize(), config.getCropperTarget());
            }

            if (config.isRotate()) {
                System.out.println("Rotary augmentation started");
                rotate(config.getRotatorSource(), config.getRotatorAngle(), config.getRotatorSteps(), config.getRotatorTarget());
            }

            if (config.isGrayscale()) {
                System.out.println("Grayscaling augmentation started");
                grayscale(config.getGrayscalerSource(), config.getGrayscalerTarget());
            }

            if (config.isGenerate()) {
                System.out.println("Image generation started");
                generate(config.getGeneratorBackgrounds(), config.getGeneratorObjects(), config.getGeneratorTarget());
            }

            if (config.isRemove()) {
                System.out.println("Junk removal started");
                remove(config.getRemoverSource(), config.getRemoverType(), config.getRemoverTarget());
            }

            if (config.isBalance()) {
                System.out.println("Balancing started");
                balance(config.getBalancerSource(), config.getBalancerRatio(), config.getBalancerTarget());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void clean(String dirUrl) {
        fileIO.clean(dirUrl);
    }

    private void crop(String sourceUrl, int size, String targetUrl) {
        List<YoloPair> pairs = pairHandler.getPairs(sourceUrl, pairHandler.FILTER_MARKED);

        monitor.setCntAll(pairs.size());
        List<Runnable> tasks = new ArrayList<>(pairs.size());
        for (YoloPair pair : pairs) {
            tasks.add(new Cropper(pair, size, targetUrl, monitor));
        }

        execute(tasks);
    }

    private void rotate(String sourceUrl, double angleBounds, int steps, String targetUrl) {
        List<YoloPair> pairs = pairHandler.getPairs(sourceUrl, pairHandler.FILTER_MARKED);

        monitor.setCntAll(pairs.size());
        List<Runnable> tasks = new ArrayList<>(pairs.size());
        for (YoloPair pair : pairs) {
            tasks.add(new Rotator(pair, angleBounds, steps, targetUrl, monitor));
        }

        execute(tasks);
    }

    private void grayscale(String sourceUrl, String targetUrl) {
        List<YoloPair> pairs = pairHandler.getPairs(sourceUrl, pairHandler.FILTER_MARKED);

        monitor.setCntAll(pairs.size());
        List<Runnable> tasks = new ArrayList<>(pairs.size());
        for (YoloPair pair : pairs) {
            tasks.add(new Grayscaler(pair, targetUrl, monitor));
        }

        execute(tasks);
    }

    private void generate(String backgroundsUrl, String objectsUrl, String targetUrl) {
        //TODO: implement
    }

    private void balance(String sourceUrl, double ratio, String targetUrl) {
        List<YoloPair> pairs = pairHandler.getPairs(sourceUrl, pairHandler.FILTER_MARKED);

        if (pairs.size() > 0) {
            Balancer balancer = new Balancer(pairs, ratio, targetUrl, monitor);
            balancer.balance();

            waitTasks();
        }
    }

    private void remove(String sourceUrl, int type, String targetUrl) {
        List<YoloPair> pairs = new ArrayList<>(0);
        switch (type) {
            case 1:
                pairs = pairHandler.getPairs(sourceUrl, pairHandler.FILTER_UNMARKED);
                break;
            case 2:
                pairs = pairHandler.getPairs(sourceUrl, pairHandler.FILTER_EMPTY);
                break;
        }

        if (pairs.size() > 0) {
            monitor.setCntAll(pairs.size());

            List<Runnable> tasks = new ArrayList<>(pairs.size());
            for (YoloPair pair : pairs) {
                tasks.add(new Remover(pair, targetUrl, monitor));
            }

            execute(tasks);
        }
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

    private void execute(List<Runnable> runnables) {
        // pooling threads ensuring not to burn the machine
        ExecutorService es = Executors.newFixedThreadPool(8);
        for (Runnable runnable : runnables) {
            Thread t = new Thread(runnable);
            t.setDaemon(true);
            es.submit(t);
        }
        // wait for recent task to finish
        waitTasks();
        es.shutdown();
    }
}
