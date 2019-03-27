package utils;

import processors.classes.MarkedImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 27.03.2019.
 */
public class PairHandler {
    private final FileIO fileIO;

    public final int
            //                             txt objects
            FILTER_EMPTY = 0,           // ?   -
            FILTER_MARKED = 1,          // +   ?
            FILTER_MARKED_NONEMPTY = 2; // +   +

    public PairHandler(FileIO fileIO) {
        this.fileIO = fileIO;
    }

    public List<MarkedImage> getPairs(String inputDir) {
        List<MarkedImage> pairs = new ArrayList<>();

        List<File> dirContents = fileIO.list(inputDir);
        // txts
        List<File> txts = fileIO.filterExtension(dirContents, "txt", true);
        // imgs
        List<File> imgs = fileIO.filterExtension(dirContents, "bmp", true);
        imgs.addAll(fileIO.filterExtension(dirContents, "png", true));
        imgs.addAll(fileIO.filterExtension(dirContents, "jpg", true));

        for (File img : imgs) {
            File txt = null;

            // trying to find a txt pair for given image
            for (File t : txts) {
                if (fileIO.getFileNameWithoutExtension(t).equals(fileIO.getFileNameWithoutExtension(img))) {
                    txt = t;
                    break;
                }
            }

            MarkedImage pair;
            if (txt == null)
                pair = new MarkedImage(img);
            else
                pair = new MarkedImage(txt, img);

            pairs.add(pair);
        }

        return pairs;
    }

    public List<MarkedImage> getPairs(String inputDir, int filter) {
        List<MarkedImage> pairs = getPairs(inputDir);
        List<MarkedImage> filtered = new ArrayList<>();

        for (MarkedImage pair : pairs) {
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
            }
        }

        return filtered;
    }
}
