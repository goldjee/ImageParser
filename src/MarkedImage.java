import java.io.File;

/**
 * Created by Ins on 24.03.2019.
 */
public class MarkedImage {
    // this is essentially a pair of image and corresponding .txt in YOLO format

    private File img;
    private File txt;

    public MarkedImage(File txt, File img) {
        this.txt = txt;
        this.img = img;
    }

    public File getTxt() {
        return txt;
    }

    public File getImg() {
        return img;
    }
}
