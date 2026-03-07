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
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;

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

    private static String getEntitySummary(Minecraft client) {
        ClientLevel world = client.level;
        LocalPlayer player = client.player;
        List<Entity> entities = world.getEntities(
            player, 
            player.getBoundingBox().inflate(config.countRange), 
            CountQuantity::isCounted
        );
        
        var summary = getSummary(entities);
        Map<String, Integer> categoryCount = summary.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().values().stream().mapToInt(List::size).sum()));
        StringBuilder sb = new StringBuilder();
        for (CategoryStyle category : categories) {
            String categoryName = category.categoryName;
            int currentCounts = categoryCount.getOrDefault(categoryName, 0);
            sb.append(category.toString(currentCounts) + "§7,");
        }
        sb.setLength(sb.length() - 3);
        return sb.toString();
    }

    public static Map<String, Map<String, List<Entity>>> getSummary(List<Entity> entities) {
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

    private static boolean isCounted(Entity entity) {
        // 客户端无法获取 persistenceRequired 真实数据，已弃用。
        // if (entity instanceof Mob mob && !mob.isPersistenceRequired()) {
        //     return true;
        // }

        // 末影龙部件
        if (entity instanceof EnderDragonPart) {
            return false;
        }

        // 不是生物，直接返回
        if (!(entity instanceof Mob mob)) {
            return true;
        }
        
        // 下列检测是无奈之举，因为指令生成的带名字或南瓜头等等的生物，
        // 若无特别指定，否则其 persistenceRequired 仍为 false，会被计入生物上限之中。
        ItemStack headEquipment = mob.getItemBySlot(EquipmentSlot.HEAD);
        // 检测是否戴南瓜头
        if (!headEquipment.isEmpty() && 
            headEquipment.getItemHolder().getRegisteredName().equals("minecraft:carved_pumpkin")) {
            return false;
        }
        // 被重命名过
        if (mob.hasCustomName()) {
            return false;
        }
        // 特定生物
        // String type = mob.getType().toShortString();
        // List<String> ignoreEntityTypes = List.of("warden", "shulker", "ender_dragon", "wither");
        // if (ignoreEntityTypes.contains(type)) {
        //     return false;
        // }
        
        // 远古守卫者
        if (mob instanceof ElderGuardian) {
            return false;
        }

        // 手持方块的末影人
        if (mob instanceof EnderMan enderMan && enderMan.getCarriedBlock() != null) {
            return false;
        }

        // 骑乘其他生物的实体
        if (mob.isPassenger()) {
            return false;
        }

        // TODO: 待完善剩余的其他情况，比如捡起过物品，繁殖过，结构生物等等。。。客户端完成希望不大
        // return false;

        return true;
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