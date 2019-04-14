package utils;

import processors.classes.YoloPair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ins on 27.03.2019.
 */
public class PairHandler {
    private final FileIO fileIO;

    public final int
            //                             txt objects
            FILTER_EMPTY = 0,           // ?   -
            FILTER_MARKED = 1,          // +   ?
            FILTER_MARKED_NONEMPTY = 2, // +   +
            FILTER_UNMARKED = 3;        // -   ?

    public PairHandler() {
        this.fileIO = FileIO.getInstance();
    }

    public List<YoloPair> getPairs(String inputDir) {
        List<YoloPair> pairs = new ArrayList<>();

        List<File> dirContents = fileIO.list(inputDir);

        // txts
        List<File> txts = fileIO.filterExtension(dirContents, "txt", true);
        // this map is needed to speed up mapping imgs to txts
        Map<String, File> txtMap = new HashMap<>(txts.size());
        for (File txt : txts) {
            txtMap.put(fileIO.getFileNameWithoutExtension(txt), txt);
        }

        // imgs
        List<File> imgs = fileIO.filterExtension(dirContents, "bmp", true);
        imgs.addAll(fileIO.filterExtension(dirContents, "png", true));
        imgs.addAll(fileIO.filterExtension(dirContents, "jpg", true));

        System.out.println("Building pairs");
        for (File img : imgs) {
            File txt = null;

            // trying to find a txt pair for given image
            if (txtMap.containsKey(fileIO.getFileNameWithoutExtension(img)))
                txt = txtMap.get(fileIO.getFileNameWithoutExtension(img));

            YoloPair pair;
            if (txt == null)
                pair = new YoloPair(img);
            else
                pair = new YoloPair(txt, img);

            pairs.add(pair);
        }
        System.out.println("Pairs built: " + pairs.size());

        return pairs;
    }

    public List<YoloPair> getPairs(String inputDir, int filter) {
        List<YoloPair> pairs = getPairs(inputDir);
        List<YoloPair> filtered = new ArrayList<>();

        for (YoloPair pair : pairs) {
            switch (filter) {
                case FILTER_EMPTY:
                    if (pair.isEmpty()) filtered.add(pair);
                    break;
                case FILTER_MARKED:
                    if (pair.isMarked()) filtered.add(pair);
                    break;
                case FILTER_MARKED_NONEMPTY:
                    if (pair.isMarkedWithObjects()) filtered.add(pair);
                    break;
                case FILTER_UNMARKED:
                    if (pair.isUnmarked()) filtered.add(pair);
            }
        }

        return filtered;
    }
}
