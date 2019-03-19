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

    List<Pair<File, File>> pairs;

    public Core() {

    }

    public void balance() {
        System.out.println("Balancing started");
        pairs = buildPairs();
        if (pairs.size() > 0) {
            balancer.balance(pairs);
        }

        System.out.println("Balancing done");
    }

    public void crop() {
        System.out.println("Cropping started");
        pairs = buildPairs();

        for (Pair<File, File> pair : pairs) {
            Cropper cropper = new Cropper(fileIO);
            cropper.set(pair);
            cropper.run();
        }

        System.out.println("Cropping done");
    }

    private List<Pair<File, File>> buildPairs() {
        List<Pair<File, File>> pairs = new ArrayList<>();

        List<File> dirContents = fileIO.list();
        List<File> txts = fileIO.filterExtension(dirContents, "txt", true);
        List<File> imgs = fileIO.filterExtension(dirContents, "txt", false);

        for (File txt : txts) {
            for (File img : imgs) {
                if (fileIO.getFileNameWithoutExtension(txt).equals(fileIO.getFileNameWithoutExtension(img))) {
                    Pair<File, File> pair = new Pair(txt, img);
                    pairs.add(pair);
                    break;
                }
            }
        }

        return pairs;
    }
}
