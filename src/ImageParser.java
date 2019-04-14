/**
 * Created by Ins on 16.03.2019.
 */
public class ImageParser {

    public static void main(String[] args) {
        System.out.println("Started");

        boolean balance = false,
                crop = false,
                augmentRotate = false,
                augmentFlip = false,
                augmentGrayscale = false,
                cleanup = false;

        int cropSize = 320;
        double augmentAngleBounds = 2;
        int augmentSteps = 20;


        int removalType = 0;
        // 0 - no remove
        // 1 - remove unmarked
        // 2 - remove empty

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-c":
                case "-crop":
                    crop = true;
                    if (i + 1 < args.length && isInt(args[i + 1])) cropSize = Integer.parseInt(args[++i]);
                    break;
                case "-ar":
                case "-augmentRotate":
                    augmentRotate = true;
                    if (i + 2 < args.length) {
                        if (isDouble(args[i + 1]))
                            augmentAngleBounds = Double.parseDouble(args[++i]);
                        if (isInt(args[i + 1]))
                            augmentSteps = Integer.parseInt(args[++i]);
                    }
                    break;
                case "-af":
                case "-augmentFlip":
                    //TODO: investigate why flipping results in a black picture
//                    augmentFlip = true;
                    break;
                case "-ag":
                case "-augmentGrayscale":
                    augmentGrayscale = true;
                    break;
                case "-b":
                case "-balance":
                    balance = true;
                    break;
                case "-cl":
                case "-cleanup":
                    cleanup = true;
                    break;
                case "-ru":
                case "-remove_unmarked":
                    removalType = 1;
                    break;
                case "-re":
                case "-remove_empty":
                    removalType = 2;
                    break;
            }
        }

        Core core = new Core();

        // clean dirs before start
        if (cleanup)
            core.cleanup();

        if (crop){
            core.crop(cropSize);
        }

        if (augmentRotate || augmentFlip || augmentGrayscale) {
            core.augment(augmentRotate, augmentAngleBounds, augmentSteps, augmentFlip, augmentGrayscale);
        }

        switch (removalType) {
            case 1:
                core.removeUnmarked();
                break;
            case 2:
                core.removeEmpty();
                break;
            default:
                if (balance) core.balance();
        }

        System.out.println("Jobs done");
    }

    private static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private static boolean isDouble(String s) {
        try {
            Double.parseDouble(s);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }
}
