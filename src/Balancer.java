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

    public void balance(List<Pair<File, File>> pairs) {
        List<Pair<File, File>> backgrounds = new ArrayList<>();

        int objectCnt = 0, backgroundCnt = 0;
        for (Pair<File, File> pair : pairs) {
            if (pair.getKey().length() == 0) {
                backgroundCnt++;
                backgrounds.add(pair);
//                System.out.println(file.getName());
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
                    fileIO.remove(pair.getKey());
                    fileIO.remove(pair.getValue());

                    backgrounds.remove(pair.getKey());
                    backgrounds.remove(pair.getValue());
                }
            }
        }
    }
}
