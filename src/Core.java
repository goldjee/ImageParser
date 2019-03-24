import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 18.03.2019.
 */
public class Core {
    private final FileIO fileIO = new FileIO();
    private final Balancer balancer = new Balancer(fileIO);
//    private final Cropper cropper = new Cropper(fileIO);

    List<MarkedImage> pairs;

    public Core() {

    }

    public void crop() {
        System.out.println("Cropping started");
        pairs = buildPairs(true);

        for (MarkedImage pair : pairs) {
            Cropper cropper = new Cropper(fileIO);
            cropper.set(pair);
            cropper.run();
        }

        System.out.println("Cropping done");
    }

    public void balance() {
        System.out.println("Balancing started");

        // we'll try to balance dataset in processed dir
        pairs = buildPairs(false);

        if (pairs.size() > 0) {
            balancer.balance(pairs, false);
        }
        else {
            // if it's empty, okay. we'll try input dir
            pairs = buildPairs(true);
            // and copy results to output btw
            balancer.balance(pairs, true);
        }

        System.out.println("Balancing done");
    }

    public void cleanup() {
        fileIO.cleanup();
    }

    private List<MarkedImage> buildPairs(boolean inputDir) {
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
