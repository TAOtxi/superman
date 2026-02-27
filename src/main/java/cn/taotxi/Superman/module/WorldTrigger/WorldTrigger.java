package cn.taotxi.Superman.module.WorldTrigger;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

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
        if (!config.enabled || config.triggerList.size() == 0) return;

        // 若任务队列不为空，跳过轮询频率限制
        if (tasks.size() == 0 && tickCounter % config.checkInterval != 0) return;

        String serverIp = client.isSingleplayer() ? "*" : client.getCurrentServer().ip; // 单人模式此属性不起作用
        String worldName = client.level.dimension().location().toString();
        Vec3 playerPos = client.player.position();

        // 只有在轮询频率到达时才检查触发条件
        if (tickCounter % config.checkInterval == 0) {
            for (WorldTriggerConfig.TriggerItem item : config.triggerList) {
                if (tasks.stream().anyMatch(task -> task.item == item)) continue;

                if (!item.serverIp.equals(serverIp) && !item.serverIp.equals("*")) return;
                if (!item.worldName.equals(worldName) && !item.worldName.equals("*")) return;

                double distance = playerPos.distanceTo(new Vec3(item.triggerPosX, item.triggerPosY, item.triggerPosZ));
                if ((item.type && distance <= item.triggerRadius) || 
                    (!item.type && distance >= item.triggerRadius)) {
                    LOGGER.info("Add task: Command{}, runInterval[{}]", item.commandList, item.runInterval);
                    tasks.add(new Task(item, item.runDelay + tickCounter));
                }
            }
        }

        for (int i=tasks.size()-1; i>=0; i--) {
            Task task = tasks.get(i);
            if (tickCounter >= task.runTick) {
                String command = task.item.commandList.get(task.runCommandIndex);
                LOGGER.info("Running task: Tick[{}], Command[{}]", tickCounter, command);
                Message.sendMessage(command);
                task.runCommandIndex++;
                task.runTick += task.item.runInterval;
                
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
