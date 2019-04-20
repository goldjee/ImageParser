package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.stream.Collectors;

/**
 * Created by Ins on 19.04.2019.
 */
public class Config {
    private static volatile Config instance;

    public boolean isClean() {
        return clean;
    }

    public String getCleanerSource() {
        return cleanerSource;
    }

    public boolean isCrop() {
        return crop;
    }

    public int getCropperSize() {
        return cropperSize;
    }

    public String getCropperSource() {
        return cropperSource;
    }

    public String getCropperTarget() {
        return cropperTarget;
    }

    public boolean isRotate() {
        return rotate;
    }

    public double getRotatorAngle() {
        return rotatorAngle;
    }

    public int getRotatorSteps() {
        return rotatorSteps;
    }

    public String getRotatorSource() {
        return rotatorSource;
    }

    public String getRotatorTarget() {
        return rotatorTarget;
    }

    public boolean isFlip() {
        return flip;
    }

    public boolean isGrayscale() {
        return grayscale;
    }

    public String getGrayscalerSource() {
        return grayscalerSource;
    }

    public String getGrayscalerTarget() {
        return grayscalerTarget;
    }

    public boolean isGenerateCrop() {
        return generateCrop;
    }

    public String getGeneratorCropSource() {
        return generatorCropSource;
    }

    public String getGeneratorCropTarget() {
        return generatorCropTarget;
    }

    public boolean isGenerate() {
        return generate;
    }

    public double getGeneratorScaleFrom() {
        return generatorScaleFrom;
    }

    public double getGeneratorScaleTo() {
        return generatorScaleTo;
    }

    public int getGeneratorSteps() {
        return generatorSteps;
    }

    public double getGeneratorVisiblePart() {
        return generatorVisiblePart;
    }

    public String getGeneratorBackgrounds() {
        return generatorBackgrounds;
    }

    public String getGeneratorObjects() {
        return generatorObjects;
    }

    public String getGeneratorTarget() {
        return generatorTarget;
    }

    public boolean isBalance() {
        return balance;
    }

    public String getBalancerSource() {
        return balancerSource;
    }

    public String getBalancerTarget() {
        return balancerTarget;
    }

    public double getBalancerRatio() {
        return balancerRatio;
    }

    public boolean isRemove() {
        return remove;
    }

    public int getRemoverType() {
        return removerType;
    }

    public String getRemoverSource() {
        return removerSource;
    }

    public String getRemoverTarget() {
        return removerTarget;
    }

    // cleaner
    private boolean clean = false;
    private String cleanerSource = "img" + FileIO.SEPARATOR + "processed";

    // cropper
    private boolean crop = false;
    private int cropperSize = 608;
    private String cropperSource = "img";
    private String cropperTarget = "img" + FileIO.SEPARATOR + "processed";

    // rotator
    private boolean rotate = false;
    private double rotatorAngle = 2;
    private int rotatorSteps = 20;
    private String rotatorSource = "img" + FileIO.SEPARATOR + "processed";
    private String rotatorTarget = "img" + FileIO.SEPARATOR + "processed";

    // flipper
    private boolean flip = false;

    // grayscaler
    private boolean grayscale = false;
    private String grayscalerSource = "img" + FileIO.SEPARATOR + "processed";
    private String grayscalerTarget = "img" + FileIO.SEPARATOR + "processed";

    // generator cropper (crops images with transparent backgrounds to size of visible image)
    private boolean generateCrop = false;
    private String generatorCropSource = "img" + FileIO.SEPARATOR + "objects";
    private String generatorCropTarget = "img" + FileIO.SEPARATOR + "processed";

    // generator
    private boolean generate = false;
    private double generatorScaleFrom = 0.2;
    private double generatorScaleTo = 1.5;
    private int generatorSteps = 10;
    private double generatorVisiblePart = 0.3;
    private String generatorBackgrounds = "img" + FileIO.SEPARATOR + "backgrounds";
    private String generatorObjects = "img" + FileIO.SEPARATOR + "objects";
    private String generatorTarget = "img" + FileIO.SEPARATOR + "processed";

    // balancer
    private boolean balance = false;
    private String balancerSource = "img" + FileIO.SEPARATOR + "processed";
    private String balancerTarget = "img" + FileIO.SEPARATOR + "processed" + FileIO.SEPARATOR + "removed";
    private double balancerRatio = 1;

    // remover
    private boolean remove = false;
    // 1 - remove unmarked
    // 2 - remove empty
    private int removerType = 1;
    private String removerSource = "img" + FileIO.SEPARATOR + "processed";
    private String removerTarget = "img" + FileIO.SEPARATOR + "processed" + FileIO.SEPARATOR + "removed";


    private Config(String configUrl) {
        try {
            read(configUrl);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Using internal default config");
        }
    }

    public static Config getInstance(String configUrl) {
        Config localInstance = instance;
        if (instance == null) {
            synchronized (FileIO.class) {
                localInstance = instance;
                if (localInstance == null)
                    instance = localInstance = new Config(configUrl);
            }
        }

        return localInstance;
    }

    public static Config getInstance() throws Exception {
        if (instance == null)
            throw new Exception("Unexpectedly, config not loaded yet");
        else
            return instance;
    }

    private void read(String configUrl) throws Exception {
        FileIO fileIO = FileIO.getInstance();

        String config = fileIO.readTxt(configUrl).stream()
                .filter(s -> !s.trim().startsWith("#"))
                .collect(Collectors.joining(""));

        JsonObject configJson = (JsonObject) new JsonParser().parse(config);
        if (configJson.has("operations")) {
            JsonArray operationsJson = configJson.getAsJsonArray("operations");

            for (JsonElement e : operationsJson) {
                JsonObject operationJson = e.getAsJsonObject();

                if (operationJson.has("operation")) {
                    switch (operationJson.get("operation").getAsString()) {
                        case "clean":
                            if (operationJson.has("source")) {
                                clean = true;
                                cleanerSource = operationJson.get("source").getAsString();
                            }
                            break;
                        case "crop":
                            if (operationJson.has("source") && operationJson.has("target")) {
                                crop = true;
                                if (operationJson.has("size")) cropperSize = operationJson.get("size").getAsInt();
                                cropperSource = operationJson.get("source").getAsString();
                                cropperTarget = operationJson.get("target").getAsString();
                            }
                            break;
                        case "rotate":
                            if (operationJson.has("source") && operationJson.has("target")) {
                                rotate = true;
                                if (operationJson.has("angle")) rotatorAngle = operationJson.get("angle").getAsDouble();
                                if (operationJson.has("steps")) rotatorSteps = operationJson.get("steps").getAsInt();
                                rotatorSource = operationJson.get("source").getAsString();
                                rotatorTarget = operationJson.get("target").getAsString();
                            }
                            break;
                        case "grayscale":
                            if (operationJson.has("source") && operationJson.has("target")) {
                                grayscale = true;
                                grayscalerSource = operationJson.get("source").getAsString();
                                grayscalerTarget = operationJson.get("target").getAsString();
                            }
                            break;
                        case "generateCrop":
                            if (operationJson.has("source") && operationJson.has("target")) {
                                generateCrop = true;
                                generatorCropSource = operationJson.get("source").getAsString();
                                generatorCropTarget = operationJson.get("target").getAsString();
                            }
                            break;
                        case "generate":
                            if (operationJson.has("backgrounds") && operationJson.has("objects") && operationJson.has("target")) {
                                generate = true;
                                if (operationJson.has("scaleFrom")) generatorScaleFrom = operationJson.get("scaleFrom").getAsDouble();
                                if (operationJson.has("scaleTo")) generatorScaleTo = operationJson.get("scaleTo").getAsDouble();
                                if (operationJson.has("steps")) generatorSteps = operationJson.get("steps").getAsInt();
                                if (operationJson.has("visiblePart")) generatorVisiblePart = operationJson.get("visiblePart").getAsDouble();
                                generatorBackgrounds = operationJson.get("backgrounds").getAsString();
                                generatorObjects = operationJson.get("objects").getAsString();
                                grayscalerTarget = operationJson.get("target").getAsString();
                            }
                            break;
                        case "balance":
                            if (operationJson.has("source") && operationJson.has("target")) {
                                balance = true;
                                if (operationJson.has("balancerRatio")) balancerRatio = operationJson.get("balancerRatio").getAsDouble();
                                grayscalerSource = operationJson.get("source").getAsString();
                                grayscalerTarget = operationJson.get("target").getAsString();
                            }
                            break;
                        case "remove": {
                            if (operationJson.has("type") && operationJson.has("source") && operationJson.has("target")) {
                                remove = true;
                                switch (operationJson.get("type").getAsString()) {
                                    case "unmarked":
                                        removerType = 1;
                                        break;
                                    case "empty":
                                        removerType = 2;
                                        break;
                                }
                                removerSource = operationJson.get("source").getAsString();
                                removerTarget = operationJson.get("target").getAsString();
                            }
                            break;
                        }
                    }
                }
            }
        }
        else throw new Exception("Failed to read config: \"" + configUrl + "\"");
    }
}
