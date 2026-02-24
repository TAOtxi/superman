package cn.taotxi.Superman.module.WorldTrigger;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Superman.module.CountQuantity.CountQuantity;
import cn.taotxi.Superman.module.CountQuantity.CountQuantityConfig;
import cn.taotxi.Superman.util.EventBus;
import cn.taotxi.Superman.util.MLogger;
import cn.taotxi.Superman.util.Message;
import cn.taotxi.Superman.util.T;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
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

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> wt = ClientCommandManager.literal("worldtrigger")
            .executes(WorldTrigger::showHelp)
            .then(ClientCommandManager.literal("help").executes(WorldTrigger::showHelp))
            .then(ClientCommandManager.literal("reload").executes(WorldTrigger::reloadConfig))
            .then(ClientCommandManager.literal("toggle").executes(WorldTrigger::toggleModule))
            .then(ClientCommandManager.literal("config").executes(WorldTrigger::openConfigGui));

            dispatcher.register(wt);
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("message.help", MODULE_NAME));
        return 1;
    }

    private static int toggleModule(CommandContext<FabricClientCommandSource> context) {
        config.enabled = !config.enabled;
        config.save();
        context.getSource().sendFeedback(T.tl("message.toggle", MODULE_NAME, config.enabled));
        return 1;
    }

    private static int reloadConfig(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("message.reload", MODULE_NAME));
        config = WorldTriggerConfig.load(WorldTriggerConfig.class, MODULE_NAME);
        return 1;
    }

    private static int openConfigGui(CommandContext<FabricClientCommandSource> context) {
        EventBus.post("openConfigGui", Map.of("title", T.t("worldtrigger.name")));
        return 1;
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
