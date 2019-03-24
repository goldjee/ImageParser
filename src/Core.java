import processors.Balancer;
import processors.Cropper;
import processors.classes.MarkedImage;
import utils.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 18.03.2019.
 */
public class Core {
    private final FileIO fileIO;
    private final Balancer balancer;

    List<MarkedImage> pairs;

    public Core() {
        fileIO = new FileIO();
        balancer = new Balancer(fileIO);
    }

    public void crop() {
        System.out.println("Cropping started");
        pairs = buildPairs(fileIO.BASE_DIR);

        for (MarkedImage pair : pairs) {
            Cropper cropper = new Cropper(fileIO);
            cropper.set(pair);
            cropper.setDaemon(true);
            cropper.start();
        }

        System.out.println("Cropping done");
    }

    public void balance() {
        System.out.println("Balancing started");

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

        System.out.println("Balancing done");
    }

    public void cleanup() {
        fileIO.clean(fileIO.PROCESSED_DIR);
        fileIO.clean(fileIO.REMOVED_DIR);
    }

    private List<MarkedImage> buildPairs(String inputDir) {
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
