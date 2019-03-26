/**
 * Created by Ins on 16.03.2019.
 */
public class ImageParser {

    public static void main(String[] args) {
        System.out.println("Started");

        boolean balance = false,
                crop = false,
                cleanup = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-c") || args[i].equals("-crop"))
                crop = true;
            if (args[i].equals("-b") || args[i].equals("-balance"))
                balance = true;
            if (args[i].equals("-cl") || args[i].equals("-cleanup"))
                cleanup = true;
        }

        Core core = new Core();

        // clean dirs before start
        if (cleanup)
            core.cleanup();

        if (crop)
            core.crop();

        if (balance)
            core.balance();

        System.out.println("Jobs done");
    }
}
