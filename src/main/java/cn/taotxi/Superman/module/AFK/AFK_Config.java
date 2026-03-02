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
    public boolean safeAttack = getDefaultSafeAttack();
    public boolean isAttackWhitelist = getDefaultIsAttackWhitelist();
    public int safeDurability = getDefaultSafeDurability();
    public List<String> attackList = getDefaultAttackList();
    public boolean runCmdWhenTooManyEntities = getDefaultRunCmdWhenTooManyEntities();
    public int safeTps = getDefaultSafeTps();
    public int safeEntityCount = getDefaultSafeEntityCount();
    public int checkInterval = getDefaultCheckInterval();
    public boolean isTriggerWhitelist = getDefaultIsTriggerWhitelist();
    public List<String> triggerEntityTypes = getDefaultTriggerEntityTypes();
    public int runInterval = getDefaultRunInterval();
    public List<String> triggerCmds = List.of();

    public static boolean getDefaultAutoAttack() {
        return false;
    }
    public static int getDefaultAttackInterval() {
        return 10 * 1;
    }
    public static boolean getDefaultSafeAttack() {
        return true;
    }
    public static boolean getDefaultIsAttackWhitelist() {
        return false;
    }
    public static List<String> getDefaultAttackList() {
        return List.of(
            "entity.minecraft.player"
        );
    }
    public static int getDefaultSafeDurability() {
        return 10;
    }
    public static boolean getDefaultRunCmdWhenTooManyEntities() {
        return true;
    }
    public static int getDefaultSafeTps() {
        return 5;
    }
    public static int getDefaultSafeEntityCount() {
        return 2000;
    }
    public static int getDefaultCheckInterval() {
        return 20 * 5;
    }
    public static int getDefaultRunInterval() {
        return 20 * 1;
    }
    public static boolean getDefaultIsTriggerWhitelist() {
        return true;
    }
    public static List<String> getDefaultTriggerEntityTypes() {
        return List.of(
            "entity.minecraft.zombified_piglin",
            "entity.minecraft.wither_skeleton"
        );
    }
    public static String getDefaultConfigVersion() {
        return "0.0.2";
    }
    
    
}
