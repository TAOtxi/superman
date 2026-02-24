package cn.taotxi.Superman.module.AFK;

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
import net.minecraft.network.chat.Component;
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
        if (!isOutOfMaxEntityCount && tickCounter % config.checkInterval != 0) {
            return;
        }
        if (!isOutOfMaxEntityCount) {
            isOutOfMaxEntityCount = isOutOfMaxCounts(client);
            nextRunTick = tickCounter;
        }
        if (isOutOfMaxEntityCount && tickCounter == nextRunTick) {
            String cmd = config.triggerCmds.get(runCmdIndex);
            LOGGER.info("Run command: " + cmd);
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
            String type = entity.getType().toShortString();
            if (config.triggerEntityTypes.contains(type)) {
                return true;
            }
            return false;
        });
        return entities.size() >= config.maxEntityCount;
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> afk = ClientCommandManager.literal("afk")
            .executes(AFK::showHelp)
            .then(ClientCommandManager.literal("help").executes(AFK::showHelp))
            .then(ClientCommandManager.literal("attack").executes(AFK::toggleAttack))
            .then(ClientCommandManager.literal("safeAFK").executes(AFK::toggleSafeAFK))
            .then(ClientCommandManager.literal("reload").executes(AFK::reloadConfig))
            .then(ClientCommandManager.literal("config").executes(AFK::openConfigGui));

            dispatcher.register(afk);
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("message.help", MODULE_NAME));
        return 1;
    }

    private static int reloadConfig(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("message.reload", MODULE_NAME));
        config = AFK_Config.load(AFK_Config.class, MODULE_NAME);
        return 1;
    }

    private static int openConfigGui(CommandContext<FabricClientCommandSource> context) {
        EventBus.post("openConfigGui", Map.of("title", T.t("afk.name")));
        return 1;
    }

    private static int toggleAttack(CommandContext<FabricClientCommandSource> context) {
        config.autoAttack = !config.autoAttack;
        context.getSource().sendFeedback(
            Component.literal(String.valueOf(config.autoAttack)));
        return 1;
    }

    private static int toggleSafeAFK(CommandContext<FabricClientCommandSource> context) {
        config.runCmdWhenTooManyEntities = !config.runCmdWhenTooManyEntities;
        context.getSource().sendFeedback(
            Component.literal(String.valueOf(config.runCmdWhenTooManyEntities)));
        return 1;
    }
}
