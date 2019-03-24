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

        Cropper cropper = new Cropper(fileIO);

        for (int i = 0; i < pairs.size(); i++) {
            MarkedImage pair = pairs.get(i);
            cropper.crop(pair);
            System.out.print("\rCropped: " + (double) Math.round((double) i / pairs.size() * 10000) / 100 + "%");
            System.out.flush();
        }
        System.out.println();

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
