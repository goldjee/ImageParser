package processors.classes;

import java.io.File;

/**
 * Created by Ins on 24.03.2019.
 */
public class YoloPair {
    // this is essentially a pair of image and corresponding .txt in YOLO format

    private File img = null;
    private File txt = null;

    public YoloPair(File txt, File img) {
        this.txt = txt;
        this.img = img;
    }

    public YoloPair(File img) {
        this.img = img;
    }

    public File getTxt() {
        return txt;
    }

    public File getImg() {
        return img;
    }

    public boolean isEmpty() {
        return txt == null || txt.length() == 0;
    }

    public boolean isMarked() {
        return txt != null;
    }

    public boolean isMarkedWithObjects() {
        return txt != null && txt.length() != 0;
    }

    public boolean isUnmarked() {
        return txt == null;
    }
}
