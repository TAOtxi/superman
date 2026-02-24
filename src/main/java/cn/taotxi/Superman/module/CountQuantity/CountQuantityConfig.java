package cn.taotxi.Superman.module.CountQuantity;

import cn.taotxi.Superman.common.BaseConfig;

public class CountQuantityConfig extends BaseConfig {
    public CountQuantityConfig(String moduleName) {
        super(moduleName);
        CONFIG_VERSION = getDefaultConfigVersion();
    }

    public boolean enabled = getDefaultEnabled();
    public int countRange = getDefaultCountRange();
    public int updateEntitySummaryInterval = getDefaultUpdateEntitySummaryInterval();
    public boolean alwaysShowSummary = getDefaultAlwaysShowSummary();

    public static boolean getDefaultEnabled() {
        return true;
    }

    public static int getDefaultCountRange() {
        return 128;
    }

    public static int getDefaultUpdateEntitySummaryInterval() {
        return 20 * 2;
    }

    public static boolean getDefaultAlwaysShowSummary() {
        return false;
    }

    public static String getDefaultConfigVersion() {
        return "1.0";
    }
}
