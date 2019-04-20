package utils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Ins on 16.03.2019.
 */
public class FileIO {
    private static volatile FileIO instance;

    public static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    private FileIO() {

    }

    public static FileIO getInstance() {
        FileIO localInstance = instance;
        if (instance == null) {
            synchronized (FileIO.class) {
                localInstance = instance;
                if (localInstance == null)
                    instance = localInstance = new FileIO();
            }
        }

        return localInstance;
    }

    public List<File> list(String dirUrl) {
        if (dirUrl != null) {
            return list(new File(checkDirUrl(dirUrl)), false);
        }
        else
            return new ArrayList<>();
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

    public void clean(String dirUrl) {
        if (dirUrl != null) {
            List<File> removeables = new ArrayList<>(list(new File(checkDirUrl(dirUrl)), true));

            for (File f : removeables) {
                if (f.exists())
                    f.delete();
            }
        }
    }

    // basic file and dir operations
    private String checkDirUrl(String url) {
        if (url != null && !url.endsWith(SEPARATOR))
            return url + SEPARATOR;
        else
            return url;
    }

    public void move(File file, String targetUrl) {
        File targetDir = new File(checkDirUrl(targetUrl));

        if (!targetDir.exists()) {
            targetDir.mkdir();
        }

        try {
            if (file.exists())
                Files.move(Paths.get(file.getAbsolutePath()), Paths.get(targetDir.getAbsolutePath() + SEPARATOR + file.getName()), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<File> list(File dir, boolean includeSubdirs) {
        List<File> files = new ArrayList<>();

        if (dir.exists() && dir.isDirectory()) {
            try (Stream<Path> stream = Files.list(dir.toPath())) {
                stream.forEach( entry -> {
                            if (!entry.toFile().isDirectory())
                                files.add(entry.toFile());
                            else if (includeSubdirs)
                                files.addAll(list(entry.toFile(), includeSubdirs));
                        }
                );
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        return files;
    }

    public BufferedImage readImg(String fileUrl) {
        BufferedImage img = null;

        File file = new File(fileUrl);
        try {
            if (file.exists())
                img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return img;
    }

    public List<String> readTxt(String fileUrl) {
        List<String> lines = new ArrayList<>();

        File file = new File(fileUrl);
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

    public void saveImg(BufferedImage image, String fileName, String targetUrl) {
        try {
            File out = new File(checkDirUrl(targetUrl) + SEPARATOR + fileName);

            if (!out.getParentFile().exists())
                out.getParentFile().mkdirs();

            // save .jpg without compression
            if (getFileExtension(out).equals("jpg")) {
                JPEGImageWriteParam jpegParam = new JPEGImageWriteParam(null);
                jpegParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                jpegParam.setCompressionQuality(1f);

                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                FileImageOutputStream fileImageOutputStream = new FileImageOutputStream(out);
                writer.setOutput(fileImageOutputStream);
                writer.write(null, new IIOImage(image, null, null), jpegParam);
                fileImageOutputStream.close();
                writer.dispose();
            }
            else
                ImageIO.write(image, getFileExtension(out), out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveTxt(List<String> lines, String fileName, String targetUrl) {
        try {
            File out = new File(checkDirUrl(targetUrl) + SEPARATOR + fileName);

            if (!out.getParentFile().exists())
                out.getParentFile().mkdirs();

            Files.write(Paths.get(out.getAbsolutePath()), lines, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
