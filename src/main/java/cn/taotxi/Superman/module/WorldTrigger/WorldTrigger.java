package cn.taotxi.Superman.module.WorldTrigger;
import java.util.ArrayList;

import java.util.List;

import cn.taotxi.Superman.util.MLogger;
import cn.taotxi.Superman.util.Message;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class WorldTrigger {
    public static final String MODULE_NAME = "worldtrigger";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    private static final List<Task> tasks = new ArrayList<>();
    public static WorldTriggerConfig config = WorldTriggerConfig.load(WorldTriggerConfig.class, MODULE_NAME);

    // TODO: 每次进入服务器都清空任务列表
    public static void registerTickEvents(Minecraft client, int tickCounter) {
        if (!config.enabled || client.isSingleplayer()) return;     // 单人模式下不触发

        String serverIp = client.getCurrentServer().ip;
        String worldName = client.level.dimension().location().toString();
        Vec3 playerPos = client.player.position();

        for (WorldTriggerConfig.TriggerItem item : config.triggerList) {
            if (tasks.stream().anyMatch(task -> task.item == item)) continue;

            if (!item.serverIp.equals(serverIp) && !item.serverIp.equals("*")) return;
            if (!item.worldName.equals(worldName) && !item.worldName.equals("*")) return;

            if (item.triggerRadius >= playerPos.distanceToSqr(new Vec3(item.triggerPosX, item.triggerPosY, item.triggerPosZ))) {
                tasks.add(new Task(item, item.runDelay + tickCounter));
            }
        }

        for (Task task : tasks) {
            if (task.runTick == tickCounter) {
                Message.sendMessage(task.item.commandList.get(task.runCommandIndex));
                task.runCommandIndex++;
                task.runTick += task.item.runInterval;
                
                // TODO: 空指针错误
                if (task.runCommandIndex >= task.item.commandList.size()) {
                    tasks.remove(task);
                }
            }
        }
    }
}

class Task {
    int runTick;
    int runCommandIndex;
    WorldTriggerConfig.TriggerItem item;
    Task(WorldTriggerConfig.TriggerItem item, int runTick) {
        this.item = item;
        this.runTick = runTick;
        this.runCommandIndex = 0;
    }
}
