package cn.taotxi.Superman.module.AFK;

import java.util.List;

import cn.taotxi.Superman.util.MLogger;
import cn.taotxi.Superman.util.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

public class AFK {
    public static final String MODULE_NAME = "AFK";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static AFK_Config config = AFK_Config.load(AFK_Config.class, MODULE_NAME);
    private static boolean isOutOfMaxEntityCount = false;
    private static int runCmdIndex = 0;
    private static int nextRunTick = 0;

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        if (isOutOfMaxEntityCount || tickCounter % config.checkInterval != 0) {
            return;
        }
        if (!isOutOfMaxEntityCount) {
            isOutOfMaxEntityCount = isOutOfMaxCounts(client);
            nextRunTick = tickCounter;
        }
        if (isOutOfMaxEntityCount && tickCounter == nextRunTick) {
            String cmd = config.triggerCmds.get(runCmdIndex);
            nextRunTick = tickCounter + config.runInterval;
            Message.sendMessage(cmd);

            if (++runCmdIndex >= config.triggerCmds.size()) {
                runCmdIndex = 0;
                isOutOfMaxEntityCount = false;
            }
        }
    }

    private static boolean isOutOfMaxCounts(Minecraft client) {
        AABB box = client.player.getBoundingBox().inflate(128);
        List<Entity> entities = client.level.getEntities(client.player, box, (entity) -> {
            String type = entity.getType().getDescriptionId();
            if (config.triggerEntityTypes.contains(type)) {
                return true;
            }
            return false;
        });
        return entities.size() >= config.maxEntityCount;
    }
}
