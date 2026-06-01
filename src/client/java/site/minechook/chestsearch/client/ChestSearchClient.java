package site.minechook.chestsearch.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ClientModInitializer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ChestSearchClient implements ClientModInitializer {

    public static class Config {
        public int color = 0x70FF796D;
        public boolean enabled = true;
    }

    public static int color = 0x70FF796D;
    public static boolean enabled = true;

    @Override
    public void onInitializeClient() {
        loadConfig();
    }

    public static void saveConfig() {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File configFile = new File("config/chestsearch.json");

        try (FileWriter writer = new FileWriter(configFile)) {
            Config config = new Config();
            config.color = color;
            config.enabled = enabled;
            gson.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());
        }
    }

    private static void loadConfig() {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final File configFile = new File("config/chestsearch.json");

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Config config = gson.fromJson(reader, Config.class);
                if (config != null) {
                    color = config.color;
                    enabled = config.enabled;
                }
            } catch (IOException e) {
                System.err.println("Failed to load config: " + e.getMessage());
            }
        } else {
            configFile.getParentFile().mkdirs();
            saveConfig();
        }
    }


}
