package cn.taotxi.Superman.common;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import cn.taotxi.Superman.Superman;
import cn.taotxi.Superman.util.Debouncer;

public class BaseConfig {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File configDir = new File(FabricLoader.getInstance().getConfigDir().toFile(), Superman.MOD_ID);
    public transient String MODULE_NAME;
    public transient Debouncer debouncer;
    public String CONFIG_VERSION = getDefaultConfigVersion();

    public BaseConfig(String moduleName) {
        this.MODULE_NAME = moduleName;
    }

    public static <T extends BaseConfig> T load(Class<T> clazz, String moduleName) {
        Superman.LOGGER.info("Loading config file for module {}", moduleName);
        if (!configDir.exists()) {
            Superman.LOGGER.info("Config directory does not exist, creating...");
            configDir.mkdirs();
        }

        File configFile = new File(configDir, moduleName + ".json");
        // TODO: 加载逻辑待优化
        try {
            if (!configFile.exists()) {
                T config = clazz.getDeclaredConstructor(String.class).newInstance(moduleName);
                Superman.LOGGER.info("Config file does not exist, creating {}", configFile.getPath());
                try (FileWriter writer = new FileWriter(configFile)) {
                    gson.toJson(config, writer);
                } catch (IOException e) {
                    Superman.LOGGER.error("[{}] Can not save config file the first time", moduleName, e);
                }
                return config;
            } else {
                try (FileReader reader = new FileReader(configFile)) {
                    Superman.LOGGER.info("[{}] Loading config file {}", moduleName, configFile.getPath());
                    T config = gson.fromJson(reader, clazz);

                    // override config version if not match
                    if (config.CONFIG_VERSION == null || !config.CONFIG_VERSION.equals(T.getDefaultConfigVersion())) {
                        Superman.LOGGER.info("[{}] Config version {} does not match, override to {}", moduleName, config.CONFIG_VERSION, T.getDefaultConfigVersion());
                        T defaultConfig = clazz.getDeclaredConstructor(String.class).newInstance(moduleName);
                        defaultConfig.save();
                        return defaultConfig;
                    }
                    config.MODULE_NAME = moduleName;
                    return config;
                } catch (IOException e) {
                    Superman.LOGGER.error("[{}] Can not load config file, create new one", moduleName, e);
                    T config = clazz.getDeclaredConstructor(String.class).newInstance(moduleName);
                    return config;
                }
            }
        } catch (Exception e) {
            Superman.LOGGER.error("[{}] Can not load config file", moduleName, e);
            try {
                return clazz.getDeclaredConstructor(String.class).newInstance(moduleName);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create config instance", ex);
            }
        }
    }

    public void remove() {
        File configFile = new File(configDir, MODULE_NAME + ".json");
        if (configFile.exists()) {
            configFile.delete();
            Superman.LOGGER.info("[{}] Config file deleted from {}", MODULE_NAME, configFile.getPath());
        } else {
            Superman.LOGGER.info("[{}] Config file does not exist, nothing to delete", MODULE_NAME);
        }
    }

    private void doSave() {
        if (!configDir.exists()) {
            Superman.LOGGER.info("Config directory does not exist, creating {}", configDir.getPath());
            configDir.mkdirs();
        }
        File configFile = new File(configDir, MODULE_NAME + ".json");
        try (FileWriter writer = new FileWriter(configFile)) {
            Superman.LOGGER.info("[{}] Saving config file {}", MODULE_NAME, configFile.getPath());
            gson.toJson(this, writer);
        } catch (IOException e) {
            Superman.LOGGER.error("[{}] Can not save config file", MODULE_NAME, e);
        }
    }

    public void save() {
        if (debouncer == null) {
            debouncer = new Debouncer(1000);
        }
        debouncer.debounce(() -> {
            doSave();
        });
    }

    public static String getDefaultConfigVersion() {
        return "1.0";
    }
}