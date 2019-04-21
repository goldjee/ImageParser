import processors.*;
import processors.classes.YoloPair;
import utils.Config;
import utils.FileIO;
import utils.PairHandler;
import utils.ProgressMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

            if (config.isGenerateCrop()) {
                System.out.println("Image generation (crop objects) started");
                generateCrop(config.getGeneratorCropSource(), config.getGeneratorCropTarget());
            }

            if (config.isGenerate()) {
                System.out.println("Image generation started");
                generate(config.getGeneratorObjects(),
                        config.getGeneratorBackgrounds(),
                        config.getGeneratorScaleFrom(),
                        config.getGeneratorScaleTo(),
                        config.getGeneratorSteps(),
                        config.getGeneratorVisiblePart(),
                        config.getGeneratorLimitPolicy(),
                        config.getGeneratorLimitOption(),
                        config.getGeneratorLimitValue(),
                        config.getGeneratorTarget());
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

    private void generateCrop(String sourceUrl, String targetUrl) {
        List<File> files = fileIO.filterExtension(fileIO.list(sourceUrl), "bmp", true);
        files.addAll(fileIO.filterExtension(fileIO.list(sourceUrl), "jpg", true));
        files.addAll(fileIO.filterExtension(fileIO.list(sourceUrl), "png", true));

        monitor.setCntAll(files.size());
        List<Runnable> tasks = new ArrayList<>(files.size());
        for (File file : files) {
            tasks.add(new GeneratorCropper(file.getAbsolutePath(), targetUrl, monitor));
        }

        execute(tasks);
    }

    private void generate(String objectsUrl, String backgroundsUrl, double scaleFrom, double scaleTo, int steps, double visiblePart, int limitPolicy, int limitOption, double limitValue, String targetUrl) {
        List<File> objects = fileIO.filterExtension(fileIO.list(objectsUrl), "bmp", true);
        objects.addAll(fileIO.filterExtension(fileIO.list(objectsUrl), "jpg", true));
        objects.addAll(fileIO.filterExtension(fileIO.list(objectsUrl), "png", true));

        List<File> backgrounds = fileIO.filterExtension(fileIO.list(backgroundsUrl), "bmp", true);
        backgrounds.addAll(fileIO.filterExtension(fileIO.list(backgroundsUrl), "jpg", true));
        backgrounds.addAll(fileIO.filterExtension(fileIO.list(backgroundsUrl), "png", true));

        List<String> backgroundUrls = new ArrayList<>(backgrounds.size());
        backgrounds.forEach(b -> backgroundUrls.add(b.getAbsolutePath()));

        if (limitPolicy != 0) {
            int limit = objects.size();
            // number
            if (limitOption == 1) limit = (int) Math.min(limitValue, objects.size());
            // percent
            else if (limitOption == 2) limit = (int) Math.min((limitValue * objects.size()), objects.size());

            List<File> objectsToProcess = new ArrayList<>();

            // top
            if (limitPolicy == 1) {
                objects.stream()
                    .limit(limit)
                    .forEach(o -> objectsToProcess.add(o));
            }
            // random
            else if (limitPolicy == 2) {
                Random rnd = new Random();
                for (int i = 0; i < limit; i++) {
                    File object = objects.get(rnd.nextInt(objects.size()));
                    if (!objectsToProcess.contains(object))
                        objectsToProcess.add(object);
                }
            }

            objects.clear();
            objects.addAll(objectsToProcess);
        }

        monitor.setCntAll(objects.size());
        List<Runnable> tasks = new ArrayList<>(objects.size());
        for (File object : objects) {
            tasks.add(new Generator(object.getAbsolutePath(), backgroundUrls, scaleFrom, scaleTo, steps, visiblePart, targetUrl, monitor));
        }

        execute(tasks);
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
