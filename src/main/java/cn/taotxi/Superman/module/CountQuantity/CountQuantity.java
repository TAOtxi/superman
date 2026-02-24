package cn.taotxi.Superman.module.CountQuantity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.taotxi.Superman.util.MLogger;
import cn.taotxi.Superman.util.Message;
import cn.taotxi.Superman.util.T;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import cn.taotxi.Superman.util.EventBus;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.AABB;

public class CountQuantity {
    public static final String MODULE_NAME = "countquantity";
    public static final MLogger LOGGER = new MLogger(MODULE_NAME);
    public static CountQuantityConfig config = CountQuantityConfig.load(CountQuantityConfig.class, MODULE_NAME);
    private static String summary = "";
    private static final List<CategoryStyle> categories = List.of(
            new CategoryStyle(MobCategory.MONSTER, "§4"),
            new CategoryStyle(MobCategory.CREATURE, "§2"),
            new CategoryStyle(MobCategory.AMBIENT, "§8"),
            new CategoryStyle(MobCategory.AXOLOTLS, "§f"),
            new CategoryStyle(MobCategory.UNDERGROUND_WATER_CREATURE, "§f"),
            new CategoryStyle(MobCategory.WATER_CREATURE, "§1"),
            new CategoryStyle(MobCategory.WATER_AMBIENT, "§3"),
            new CategoryStyle(MobCategory.MISC, "§f")
        );

    public static void init() {
    }

    public static void registerTickEvents(Minecraft client, int tickCounter) {
        if (!config.enabled) {
            return;
        }
        
        if (config.alwaysShowSummary) {
            if (tickCounter % config.updateEntitySummaryInterval == 0) {
                summary = getEntitySummary(client);
            }
            Message.actionBarMsg(summary);
        }
    }

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        LiteralArgumentBuilder<FabricClientCommandSource> cq = ClientCommandManager.literal("cq")
            .executes(CountQuantity::showHelp)
            .then(ClientCommandManager.literal("help").executes(CountQuantity::showHelp))
            .then(ClientCommandManager.literal("toggle").executes(CountQuantity::toggleModule))
            .then(ClientCommandManager.literal("reload").executes(CountQuantity::reloadConfig))
            .then(ClientCommandManager.literal("config").executes(CountQuantity::openConfigGui))
            .then(ClientCommandManager.literal("summary").executes(CountQuantity::toggleAlwaysShowSummary))
            .then(ClientCommandManager.literal("count")
                .then(ClientCommandManager.argument("topN", IntegerArgumentType.integer())
                .executes(CountQuantity::displayCountInfo)));

            dispatcher.register(cq);
    }

    private static int showHelp(CommandContext<FabricClientCommandSource> context) {
        return 1;
    }

    private static int openConfigGui(CommandContext<FabricClientCommandSource> context) {
        EventBus.post("openConfigGui", Map.of("title", T.t("countquantity.name")));
        return 1;
    }

    private static int reloadConfig(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(T.tl("message.reload", MODULE_NAME));
        config = CountQuantityConfig.load(CountQuantityConfig.class, MODULE_NAME);
        return 1;
    }

    private static int toggleModule(CommandContext<FabricClientCommandSource> context) {
        config.enabled = !config.enabled;
        config.save();
        context.getSource().sendFeedback(T.tl("message.toggle", MODULE_NAME, config.enabled));
        return 1;
    }
    private static int toggleAlwaysShowSummary(CommandContext<FabricClientCommandSource> context) {
        config.alwaysShowSummary = !config.alwaysShowSummary;
        context.getSource().sendFeedback(
            Component.literal(String.valueOf(config.alwaysShowSummary)));
        return 1;
    }

    private static int displayCountInfo(CommandContext<FabricClientCommandSource> context) {
        int topN = IntegerArgumentType.getInteger(context, "topN");
        if (topN != -1 && topN < 1) return 0;

        ClientLevel world = context.getSource().getWorld();
        LocalPlayer player = context.getSource().getPlayer();
        List<Entity> entities = world.getEntities(player, player.getBoundingBox().inflate(config.countRange));
        Map<String, Integer> entityCount = entities.stream()
            .collect(Collectors.toMap(e -> e.getType().getDescriptionId(), e -> 1, Integer::sum));
        List<Map.Entry<String, Integer>> sortedEntityCount = entityCount.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .collect(Collectors.toList());

        if (topN == -1) topN = sortedEntityCount.size();
        topN = Math.min(topN, sortedEntityCount.size());
        for (int i = 0; i < topN; i++) {
            Map.Entry<String, Integer> entry = sortedEntityCount.get(i);
            Message.chatMsg(Component.translatable(entry.getKey()).append(": " + entry.getValue()));
        }
        return 1;
    }

    // TODO: 忽略一些实体，比如戴南瓜头的敌对生物
    private static String getEntitySummary(Minecraft client) {
        ClientLevel world = client.level;
        LocalPlayer player = client.player;
        List<Entity> entities = world.getEntities(player, player.getBoundingBox().inflate(config.countRange));
        
        var summary = getEntitySummary(entities);
        Map<String, Integer> categoryCount = summary.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().values().stream().mapToInt(List::size).sum()));

        StringBuilder sb = new StringBuilder();
        for (CategoryStyle category : categories) {
            String categoryName = category.categoryName;
            int currentCounts = categoryCount.getOrDefault(categoryName, 0);
            sb.append(category.toString(currentCounts) + "§7,");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static Map<String, Map<String, List<Entity>>> getEntitySummary(List<Entity> entities) {
        Map<String, Map<String, List<Entity>>> summary = new HashMap<>();
        for (Entity entity : entities) {
            var type = entity.getType();
            MobCategory category = type.getCategory();
            String entityId = type.getDescriptionId();
            if (!summary.containsKey(category.getName())) {
                summary.put(category.getName(), new HashMap<>());
            }
            if (!summary.get(category.getName()).containsKey(entityId)) {
                summary.get(category.getName()).put(entityId, new ArrayList<>());
            }
            summary.get(category.getName()).get(entityId).add(entity);
        }
        return summary;
    }
}

class CategoryStyle {
    public String categoryName;
    public int currentCounts;
    public int maxCounts;
    public String maxCountsStyle;

    public CategoryStyle(MobCategory category) {
        setStyle(category.getName(), 0, category.getMaxInstancesPerChunk(), "§f");
    }

    public CategoryStyle(MobCategory category, String maxCountsStyle) {
        setStyle(category.getName(), 0, category.getMaxInstancesPerChunk(), maxCountsStyle);
    }

    public CategoryStyle(String categoryName, int maxCounts, String maxCountsStyle) {
        setStyle(categoryName, 0, maxCounts, maxCountsStyle);
    }

    public CategoryStyle(String categoryName, int currentCounts, int maxCounts, String maxCountsStyle) {
        setStyle(categoryName, currentCounts, maxCounts, maxCountsStyle);
    }

    public void setStyle(String categoryName, int currentCounts, int maxCounts, String maxCountsStyle) {
        this.categoryName = categoryName;
        this.currentCounts = currentCounts;
        this.maxCounts = maxCounts;
        this.maxCountsStyle = maxCountsStyle;
    }

    public String toString(int currentCounts) {
        this.currentCounts = currentCounts;
        return toString();
    }

    public String toString() {
        if (currentCounts == 0) return "§7-/" + maxCountsStyle + maxCounts;
        String currentCountsStyle = "";
        if (currentCounts <= maxCounts * 0.5) {
            currentCountsStyle = "§2";
        } else if (currentCounts < maxCounts) {
            currentCountsStyle = "§e";
        } else if (currentCounts == maxCounts) {
            currentCountsStyle = "§c";
        } else {
            currentCountsStyle = "§d";
        }

        return currentCountsStyle + currentCounts + "§7/" + maxCountsStyle + maxCounts;
    }
}