import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ins on 16.03.2019.
 */
public class FileIO {
    private static final String baseUri = System.getProperty("user.dir") + "\\img";
    private static final String removedUri = System.getProperty("user.dir") + "\\img\\removed";
    private static final String processedUri = System.getProperty("user.dir") + "\\img\\processed";

    public FileIO() {

    }

    private List<File> list(File dir, boolean includeSubdirs) {
        List<File> files = new ArrayList<>();

        if (dir.exists() && dir.isDirectory()) {
            for (File entry : dir.listFiles()) {
                if (!entry.isDirectory())
                    files.add(entry);
                else if (includeSubdirs)
                    files.addAll(list(entry, includeSubdirs));
            }
        }

        return files;
    }

    public List<File> list() {
        return list(new File(baseUri), false);
    }

    public List<File> filterExtension(List<File> files, String extension, boolean match) {
        List<File> filtered = new ArrayList<>();

        for (File file : files) {
            if (getFileExtension(file).equals(extension) == match) {
                filtered.add(file);
            }
        }

        return filtered;
    }

    public String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }

        return name.substring(lastIndexOf + 1);
    }

    public String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }

        return name.substring(0, lastIndexOf);
    }

    private void move(File file, File targetDir) {
        if (!targetDir.exists()) {
            targetDir.mkdir();
        }

        try {
//            System.out.println(file.getAbsolutePath());
//            System.out.println(targetDir.getAbsolutePath() + "\\" + file.getName());
            if (file.exists())
                Files.move(Paths.get(file.getAbsolutePath()), Paths.get(targetDir.getAbsolutePath() + "\\" + file.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(File file) {
        move(file, new File(removedUri));
    }

    public BufferedImage readImg(File file) {
        BufferedImage img = null;
        try {
            if (file.exists())
                img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    public List<String> readTxt(File file) {
        List<String> lines = new ArrayList<>();
        try {
            if (file.exists())
                lines.addAll(Files.readAllLines(Paths.get(file.getAbsolutePath())));
            else
                return null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    public void saveImg(BufferedImage image, File file) {
        try {
            File out = new File(processedUri + "\\" + file.getName());

            if (!out.getParentFile().exists())
                out.getParentFile().mkdirs();

            ImageIO.write(image, "jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTxt(List<String> lines, File file) {
        try {
            File out = new File(processedUri + "\\" + file.getName());

            if (!out.getParentFile().exists())
                out.getParentFile().mkdirs();

            Files.write(Paths.get(out.getAbsolutePath()), lines, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
