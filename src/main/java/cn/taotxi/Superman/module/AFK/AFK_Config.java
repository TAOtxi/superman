package cn.taotxi.Superman.module.AFK;

import java.util.List;

import cn.taotxi.Superman.common.BaseConfig;

public class AFK_Config extends BaseConfig {
    public AFK_Config(String moduleName) {
        super(moduleName);
        CONFIG_VERSION = getDefaultConfigVersion();
    }
    public boolean autoAttack = getDefaultAutoAttack();
    public int attackInterval = getDefaultAttackInterval();
    public boolean runCmdWhenTooManyEntities = getDefaultRunCmdWhenTooManyEntities();
    public int maxEntityCount = getDefaultMaxEntityCount();
    public int checkInterval = getDefaultCheckInterval();
    public List<String> triggerEntityTypes = List.of(
        "minecraft:zombified_piglin",
        "minecraft:wither_skeleton"
    );
    public int runInterval = getDefaultRunInterval();
    public List<String> triggerCmds = List.of();

    public static boolean getDefaultAutoAttack() {
        return false;
    }
    public static int getDefaultAttackInterval() {
        return 10 * 1;
    }
    public static boolean getDefaultRunCmdWhenTooManyEntities() {
        return true;
    }
    public static int getDefaultMaxEntityCount() {
        return 2000;
    }
    public static int getDefaultCheckInterval() {
        return 20 * 5;
    }
    public static int getDefaultRunInterval() {
        return 20 * 1;
    }
    public static String getDefaultConfigVersion() {
        return "1.0";
    }
    
    
}
