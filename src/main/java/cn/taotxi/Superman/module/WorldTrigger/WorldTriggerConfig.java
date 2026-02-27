package cn.taotxi.Superman.module.WorldTrigger;

import java.util.ArrayList;
import java.util.List;

import cn.taotxi.Superman.common.BaseConfig;

public class WorldTriggerConfig extends BaseConfig {
    public WorldTriggerConfig(String moduleName) {
        super(moduleName);
        CONFIG_VERSION = getDefaultConfigVersion();
    }
    
    public boolean enabled = getDefaultEnabled();
    public List<TriggerItem> triggerList = new ArrayList<>();

    public static boolean getDefaultEnabled() {
        return true;
    }

    public void addItems() {
        triggerList.add(new TriggerItem());
    }

    // TODO: 设置命令的触发次数
    class TriggerItem {
        public boolean enabled = getDefaultEnabled();
        public String serverIp = getDefaultServerIp();
        public String worldName = getDefaultWorldName();
        public int triggerPosX = 0;
        public int triggerPosY = 0;
        public int triggerPosZ = 0;
        public int triggerRadius = getDefaultTriggerRadius();
        public int runDelay = getDefaultRunDelay();
        public int runInterval = getDefaultRunInterval();
        public List<String> commandList = List.of("");
        
        public static boolean getDefaultEnabled() {
            return false;
        }
        public static String getDefaultServerIp() {
            return "*";
        }
        public static String getDefaultWorldName() {
            return "overworld";
        }
        public static int getDefaultTriggerRadius() {
            return 10;
        }
        public static int getDefaultRunDelay() {
            return 20 * 10;
        }
        public static int getDefaultRunInterval() {
            return 20 * 5;
        }
        public static String getDefaultConfigVersion() {
            return "1.0";
        }
    }
}


