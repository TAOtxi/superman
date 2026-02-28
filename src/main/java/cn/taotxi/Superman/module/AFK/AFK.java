package cn.taotxi.Superman.module.AFK;

import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import cn.taotxi.Superman.util.EventBus;
import cn.taotxi.Superman.util.ItemStackUtil;
import cn.taotxi.Superman.util.MLogger;
import cn.taotxi.Superman.util.Message;
import cn.taotxi.Superman.util.T;
import cn.taotxi.Superman.util.TickUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class AFK {
    public static final String MODULE_NAME = "AFK";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static AFK_Config config = AFK_Config.load(AFK_Config.class, MODULE_NAME);
    private static boolean isOutOfMaxEntityCount = false;
    private static int runCmdIndex = 0;
    private static int nextRunCmdTick = 0;
    private static int lastAttackTick = 0;

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        preventTooManyEntities(client, tickCounter);
        autoAttack(client, tickCounter);
    }

    private static void preventTooManyEntities(Minecraft client, int tickCounter) {
        if (!config.runCmdWhenTooManyEntities || config.triggerCmds.size() == 0) {
            return;
        }
        if (!isOutOfMaxEntityCount && tickCounter % config.checkInterval != 0) {
            return;
        }

        if (!isOutOfMaxEntityCount) {
            isOutOfMaxEntityCount = isOutOfMaxCounts(client);
            nextRunCmdTick = tickCounter;
        }
        // TODO: 切换世界或进入服务器时重置tick
        if (isOutOfMaxEntityCount && tickCounter >= nextRunCmdTick) {
            if (runCmdIndex >= config.triggerCmds.size()) {
                resetSafeAFKStatus(true);
            }
            nextRunCmdTick = tickCounter + config.runInterval;
            String cmd = config.triggerCmds.get(runCmdIndex);
            LOGGER.info("Run command: [{}] {}", runCmdIndex, cmd);
            Message.sendMessage(cmd);
            runCmdIndex++;
        }
    }

    public static void resetSafeAFKStatus(boolean checkAgain) {
        runCmdIndex = 0;
        nextRunCmdTick = 0;
        if (checkAgain) {
            isOutOfMaxEntityCount = isOutOfMaxCounts(Minecraft.getInstance());
        }
    }

    private static void autoAttack(Minecraft client, int tickCounter) {
        if (!config.autoAttack) return;
        
        // TODO: 使用tick指令降低tps时，Tick Event事件的回调频率也会跟随降低
        double adaptInterval = config.attackInterval * 20 / TickUtils.getAverageTps();
        if (tickCounter - lastAttackTick < adaptInterval) return;
        lastAttackTick = tickCounter;
        tryToAttack(client);
    }

    private static void tryToAttack(Minecraft client) {
        if (!client.player.isAlive()) {
            return;
        }
        ItemStack handItem = client.player.getMainHandItem();
        if (!handItem.isEmpty() &&
            handItem.isDamageableItem() &&
            handItem.getMaxDamage() - handItem.getDamageValue() <= config.safeDurability) {
            
            return;
        }

        Entity targetEntity = client.crosshairPickEntity;
        if (targetEntity != null) {
            client.gameMode.attack(client.player, targetEntity);
            client.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private static boolean isOutOfMaxCounts(Minecraft client) {
        if (client.level == null || client.player == null) {
            return false;
        }
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
        LiteralArgumentBuilder<FabricClientCommandSource> afk = ClientCommandManager.literal("afkk")
            .executes(AFK::showHelp)
            .then(ClientCommandManager.literal("help").executes(AFK::showHelp))
            .then(ClientCommandManager.literal("attack")
                .then(ClientCommandManager.literal("true").executes(context -> setAutoAttack(context, true)))
                .then(ClientCommandManager.literal("false").executes(context -> setAutoAttack(context, false)))
                .then(ClientCommandManager.literal("info").executes(AFK::showAttackInfo))
                .then(ClientCommandManager.argument("interval", IntegerArgumentType.integer(1))
                    .executes(AFK::setAttackInterval)))

            .then(ClientCommandManager.literal("safeAfk")
                .then(ClientCommandManager.literal("true").executes(context -> setSafeAFK(context, true)))
                .then(ClientCommandManager.literal("false").executes(context -> setSafeAFK(context, false)))
                .then(ClientCommandManager.argument("interval", IntegerArgumentType.integer(1))
                    .executes(AFK::setSafeCheckInterval)))

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

    private static int setAutoAttack(CommandContext<FabricClientCommandSource> context, boolean setValue) {
        if (config.autoAttack != setValue) {
            config.autoAttack = setValue;
            config.save();
        }
        context.getSource().sendFeedback(
            Component.literal(String.valueOf(config.autoAttack)));
        return 1;
    }

    private static int showAttackInfo(CommandContext<FabricClientCommandSource> context) {
        double avgTps = TickUtils.getAverageTps();
        double adaptInterval = config.attackInterval * 20 / avgTps;
        String tickInfo = String.format(String.format("Attack interval: %.2f, AvgTPS: %.2f\n", adaptInterval, avgTps));

        ItemStack handItem = context.getSource().getPlayer().getMainHandItem();
        String weaponInfo = String.format("Weapon: %s, Durability: %d\n", ItemStackUtil.getName(handItem), handItem.getMaxDamage() - handItem.getDamageValue());
        Entity targetEntity = context.getSource().getClient().crosshairPickEntity;
        String attackTargetInfo = String.format("Attack target: %s", targetEntity != null ? T.tt(targetEntity.getType().getDescriptionId()) : "None");
        context.getSource().sendFeedback(
            T.ls(tickInfo, weaponInfo, attackTargetInfo)
        );
        return 1;
    }

    private static int setSafeAFK(CommandContext<FabricClientCommandSource> context, boolean setValue) {
        if (config.runCmdWhenTooManyEntities != setValue) {
            config.runCmdWhenTooManyEntities = setValue;
            config.save();
        }
        context.getSource().sendFeedback(
            Component.literal(String.valueOf(config.runCmdWhenTooManyEntities)));
        return 1;
    }

    private static int setSafeCheckInterval(CommandContext<FabricClientCommandSource> context) {
        int interval = IntegerArgumentType.getInteger(context, "interval");
        config.checkInterval = interval;
        config.save();
        context.getSource().sendFeedback(
            T.tl("message.setInterval", interval));
        return 1;
    }

    private static int setAttackInterval(CommandContext<FabricClientCommandSource> context) {
        int interval = IntegerArgumentType.getInteger(context, "interval");
        config.attackInterval = interval;
        config.save();
        context.getSource().sendFeedback(
            T.tl("message.setInterval", interval));
        return 1;
    }
}
