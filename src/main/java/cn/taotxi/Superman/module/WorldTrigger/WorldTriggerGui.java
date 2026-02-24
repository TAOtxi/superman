package cn.taotxi.Superman.module.WorldTrigger;

import java.util.List;

import cn.taotxi.Superman.gui.ConfigScreen;
import cn.taotxi.Superman.gui.Factory;
import cn.taotxi.Superman.util.T;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;

public class WorldTriggerGui {
    public static ConfigCategory.Builder createWorldTriggerCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("worldtrigger.name"))
                .tooltip(T.tl("worldtrigger.desc"));

        category.option(Factory.addToggleOption(
            T.tl("module.enabled"),
            T.tl("module.enabled.desc"),
            WorldTriggerConfig.getDefaultEnabled(),
            () -> WorldTrigger.config.enabled,
            val -> WorldTrigger.config.enabled = val
        ));

        category.option(ButtonOption.createBuilder()
                .name(T.tl("worldtrigger.addBlock").withStyle(ChatFormatting.GREEN))
                .description(OptionDescription.of(T.tl("worldtrigger.addBlock.desc")))
                .action((yaclScreen, button) -> {
                    WorldTrigger.config.addItems();
                    WorldTrigger.config.save();
                    ConfigScreen.reload(yaclScreen, parent);
                })
                .build()
        );

        for (WorldTriggerConfig.TriggerItem item : WorldTrigger.config.triggerList) {
            OptionGroup.Builder group = OptionGroup.createBuilder()
                    .name(T.tl("worldtrigger.block.name"))
                    .description(OptionDescription.of(T.tl("worldtrigger.block.name.desc")));
                    
            group.option(Factory.addToggleOption(
                T.tl("worldtrigger.block.enabled"),
                T.tl("worldtrigger.block.enabled.desc"),
                item.enabled,
                () -> item.enabled,
                val -> item.enabled = val
            ));

            group.option(Option.<String>createBuilder()
                    .name(T.tl("worldtrigger.block.serverIp"))
                    .description(OptionDescription.of(T.tl("worldtrigger.block.serverIp.desc")))
                    .binding(
                        WorldTriggerConfig.TriggerItem.getDefaultServerIp(),
                        () -> item.serverIp,
                        val -> item.serverIp = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            group.option(Option.<String>createBuilder()
                    .name(T.tl("worldtrigger.block.worldName"))
                    .description(OptionDescription.of(T.tl("worldtrigger.block.worldName.desc")))
                    .binding(
                        WorldTriggerConfig.TriggerItem.getDefaultWorldName(),
                        () -> item.worldName,
                        val -> item.worldName = val
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            group.option(Option.<String>createBuilder()
                    .name(T.tl("worldtrigger.block.triggerPos"))
                    .description(OptionDescription.of(T.tl("worldtrigger.block.triggerPos.desc")))
                    .binding(
                        "0, 0, 0",
                        () -> String.format("%.2f, %.2f, %.2f", item.triggerPosX, item.triggerPosY, item.triggerPosZ),
                        val -> {
                            String[] pos = val.replace("，", ",")
                                              .replace(" ", "")
                                              .split(",");
                            if (pos.length != 3) return;
                            item.triggerPosX = Double.parseDouble(pos[0]);
                            item.triggerPosY = Double.parseDouble(pos[1]);
                            item.triggerPosZ = Double.parseDouble(pos[2]);
                        }
                    )
                    .controller(StringControllerBuilder::create)
                    .build()
            );

            group.option(Option.<Integer>createBuilder()
                    .name(T.tl("worldtrigger.block.triggerRadius"))
                    .description(OptionDescription.of(T.tl("worldtrigger.block.triggerRadius.desc")))
                    .binding(
                        WorldTriggerConfig.TriggerItem.getDefaultTriggerRadius(),
                        () -> item.triggerRadius,
                        val -> item.triggerRadius = Math.clamp(val, 1, Integer.MAX_VALUE)
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
            );

            group.option(Option.<Integer>createBuilder()
                    .name(T.tl("worldtrigger.block.runDelay"))
                    .description(OptionDescription.of(T.tl("worldtrigger.block.runDelay.desc")))
                    .binding(
                        WorldTriggerConfig.TriggerItem.getDefaultRunDelay(),
                        () -> item.runDelay,
                        val -> item.runDelay = Math.max(0, val)
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
            );
            
            group.option(Option.<Integer>createBuilder()
                    .name(T.tl("worldtrigger.block.runInterval"))
                    .description(OptionDescription.of(T.tl("worldtrigger.block.runInterval.desc")))
                    .binding(
                        WorldTriggerConfig.TriggerItem.getDefaultRunInterval(),
                        () -> item.runInterval,
                        val -> item.runInterval = Math.clamp(val, 1, Integer.MAX_VALUE) // TODO: 最小值可以设置为0
                    )
                    .controller(IntegerFieldControllerBuilder::create)
                    .build()
            );
            
            category.group(group.build());
            category.group(ListOption.<String>createBuilder()
                    .name(T.tl("worldtrigger.block.command.name"))
                    .description(OptionDescription.of(T.tl("worldtrigger.block.command.name.desc")))
                    .binding(
                        List.of(""),
                        () -> item.commandList,
                        val -> item.commandList = val
                    )
                    .initial("")
                    .controller(StringControllerBuilder::create)
                    .build()
            );
        }
        return category;
    }
}
