import utils.Config;

/**
 * Created by Ins on 16.03.2019.
 */
class ImageParser {

    public static void main(String[] args) {
        System.out.println("Started");

        String configPath = args[0].replace("\"", "");
        Config config = Config.getInstance(configPath);

        Core core = new Core();
        core.start();

        System.out.println("Jobs done");
    }
}
