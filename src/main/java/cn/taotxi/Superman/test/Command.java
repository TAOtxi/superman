package cn.taotxi.Superman.test;

import java.util.List;

import cn.taotxi.Superman.util.T;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;


public class Command {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> cq = ClientCommandManager.literal("cq").executes(context -> {
                context.getSource().sendFeedback(T.l("Called Show Command"));
                return 1;
            }).then(ClientCommandManager.literal("entity").executes(context -> {
                context.getSource().sendFeedback(T.l("Show entity information"));
                Minecraft client = context.getSource().getClient();
                
                LocalPlayer player = client.player;
                AABB box = player.getBoundingBox().inflate(10);
                List<Entity> list = client.level.getEntities(player, box);
                for (Entity entity : list) {
                    if (entity instanceof LocalPlayer) continue;
                }
                return 1;
            }));
            dispatcher.register(cq);
        });
    }
}
