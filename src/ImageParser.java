/**
 * Created by Ins on 16.03.2019.
 */
public class ImageParser {

    public static void main(String[] args) {
        System.out.println("Started");

        boolean balance = false,
                crop = false,
                cleanup = false;

        int removalType = 0;
        // 0 - no remove
        // 1 - remove unmarked
        // 2 - remove empty

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-c") || args[i].equals("-crop"))
                crop = true;
            if (args[i].equals("-b") || args[i].equals("-balance"))
                balance = true;
            if (args[i].equals("-cl") || args[i].equals("-cleanup"))
                cleanup = true;
            if (args[i].equals("-ru") || args[i].equals("-remove_unmarked"))
                removalType = 1;
            if (args[i].equals("-re") || args[i].equals("-remove_empty"))
                removalType = 2;
        }

        Core core = new Core();

        // clean dirs before start
        if (cleanup)
            core.cleanup();

        if (crop)
            core.crop();

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
}
