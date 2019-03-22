import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Ins on 16.03.2019.
 */
public class Balancer {

    // target object to background ratio
    private static final float ratio = 1.0f;

    private FileIO fileIO;

    public Balancer(FileIO fileIO) {
        this.fileIO = fileIO;
    }

    public void balance(List<Pair<File, File>> pairs, boolean fromInput) {
        List<Pair<File, File>> backgrounds = new ArrayList<>();

        // remains are pairs which should be preserved
        List<Pair<File, File>> remains = new ArrayList<>(pairs);
        // removed are pairs pending for removal
        List<Pair<File, File>> removed = new ArrayList<>();

        int objectCnt = 0, backgroundCnt = 0;
        for (Pair<File, File> pair : remains) {
            if (pair.getKey().length() == 0) {
                backgroundCnt++;
                backgrounds.add(pair);
            }
            else objectCnt++;
        }
        System.out.println("Objects: " + objectCnt);
        System.out.println("Backgrounds: " + backgroundCnt);

        float ratio = (float) objectCnt / backgroundCnt;
        if (ratio < this.ratio) {
            Random rnd = new Random();
            for (int i = 0; i < (int) ((float) objectCnt / this.ratio); i++) {
                int idx = rnd.nextInt(backgrounds.size() - 1);

                Pair<File, File> pair = backgrounds.get(idx);
                if (pair != null) {
                    removed.add(pair);
                    remains.remove(pair);
                }
            }
        }

        // finally perform copy and move operations
        if (fromInput) {
            for (Pair<File, File> pair : remains) {
                // copy to output
                fileIO.copy(pair.getKey());
                fileIO.copy(pair.getValue());
            }
        }
        else {
            for (Pair<File, File> pair : removed) {
                fileIO.remove(pair.getKey());
                fileIO.remove(pair.getValue());
            }
        }
    }
}
