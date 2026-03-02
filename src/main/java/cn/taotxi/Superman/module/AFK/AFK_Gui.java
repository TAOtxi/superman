package cn.taotxi.Superman.module.AFK;

import java.util.List;

import cn.taotxi.Superman.gui.Factory;
import cn.taotxi.Superman.util.T;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import net.minecraft.client.gui.screens.Screen;

public class AFK_Gui {
    public static ConfigCategory.Builder createAFKCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("afk.name"))
                .tooltip(T.tl("afk.desc"));

        OptionGroup.Builder autoAttackGroup = OptionGroup.createBuilder()
                    .name(T.tl("afk.autoAttack.group.name"))
                    .description(OptionDescription.of(T.tl("afk.autoAttack.group.desc")));

        autoAttackGroup.option(Factory.addToggleOption(
            T.tl("afk.autoAttack.enabled"),
            T.tl("afk.autoAttack.desc"),
            AFK_Config.getDefaultAutoAttack(),
            () -> AFK.config.autoAttack,
            val -> AFK.config.autoAttack = val
        ));

        autoAttackGroup.option(Option.<Integer>createBuilder()
            .name(T.tl("afk.attackInterval"))
            .description(OptionDescription.of(T.tl("afk.attackInterval.desc")))
            .binding(AFK_Config.getDefaultAttackInterval(),
                () -> AFK.config.attackInterval,
                val -> AFK.config.attackInterval = val)
            .controller(IntegerFieldControllerBuilder::create)
            .build());

        autoAttackGroup.option(Factory.addToggleOption(
            T.tl("afk.safeAttack.enabled"),
            T.tl("afk.safeAttack.desc"),
            AFK_Config.getDefaultSafeAttack(),
            () -> AFK.config.safeAttack,
            val -> AFK.config.safeAttack = val
        ));

        autoAttackGroup.option(Option.<Integer>createBuilder()
            .name(T.tl("afk.safeDurability"))
            .description(OptionDescription.of(T.tl("afk.safeDurability.desc")))
            .binding(AFK_Config.getDefaultSafeDurability(),
                () -> AFK.config.safeDurability,
                val -> AFK.config.safeDurability = val)
            .controller(IntegerFieldControllerBuilder::create)
            .build());

        category.group(autoAttackGroup.build());

        OptionGroup.Builder detectEntityCountGroup = OptionGroup.createBuilder()
                    .name(T.tl("afk.detectEntityCount.group.name"))
                    .description(OptionDescription.of(T.tl("afk.detectEntityCount.group.desc")));

        detectEntityCountGroup.option(Factory.addToggleOption(
            T.tl("afk.runCmdWhenTooManyEntities.enabled"),
            T.tl("afk.runCmdWhenTooManyEntities.desc"),
            AFK_Config.getDefaultRunCmdWhenTooManyEntities(),
            () -> AFK.config.runCmdWhenTooManyEntities,
            val -> AFK.config.runCmdWhenTooManyEntities = val
        ));

        detectEntityCountGroup.option(Option.<Integer>createBuilder()
            .name(T.tl("afk.safeTps"))
            .description(OptionDescription.of(T.tl("afk.safeTps.desc")))
            .binding(AFK_Config.getDefaultSafeTps(),
                () -> AFK.config.safeTps,
                val -> AFK.config.safeTps = val)
            .controller(IntegerFieldControllerBuilder::create)
            .build());

        detectEntityCountGroup.option(Option.<Integer>createBuilder()
            .name(T.tl("afk.maxEntityCount"))
            .description(OptionDescription.of(T.tl("afk.maxEntityCount.desc")))
            .binding(AFK_Config.getDefaultMaxEntityCount(),
                () -> AFK.config.maxEntityCount,
                val -> AFK.config.maxEntityCount = val)
            .controller(IntegerFieldControllerBuilder::create)
            .build());

        detectEntityCountGroup.option(Option.<Integer>createBuilder()
            .name(T.tl("afk.checkInterval"))
            .description(OptionDescription.of(T.tl("afk.checkInterval.desc")))
            .binding(AFK_Config.getDefaultCheckInterval(),
                () -> AFK.config.checkInterval,
                val -> AFK.config.checkInterval = val)
            .controller(IntegerFieldControllerBuilder::create)
            .build());

        detectEntityCountGroup.option(Option.<Integer>createBuilder()
            .name(T.tl("afk.runInterval"))
            .description(OptionDescription.of(T.tl("afk.runInterval.desc")))
            .binding(AFK_Config.getDefaultRunInterval(),
                () -> AFK.config.runInterval,
                val -> AFK.config.runInterval = val)
            .controller(IntegerFieldControllerBuilder::create)
            .build());

        category.group(detectEntityCountGroup.build());

        category.group(ListOption.<String>createBuilder()
            .name(T.tl("afk.triggerEntityTypes"))
            .description(OptionDescription.of(T.tl("afk.triggerEntityTypes.desc")))
            .binding(
                AFK_Config.getDefaultTriggerEntityTypes(),
                () -> AFK.config.triggerEntityTypes,
                // val -> AFK.config.triggerEntityTypes = StringUtils.withDefaultNameSpace(val) // TODO: getter和setter不匹配，yacl会警告。
                val -> AFK.config.triggerEntityTypes = val
            )
            .initial("")
            .controller(StringControllerBuilder::create)
            .build());

        category.group(ListOption.<String>createBuilder()
            .name(T.tl("afk.triggerCmds"))
            .description(OptionDescription.of(T.tl("afk.triggerCmds.desc")))
            .binding(
                List.of(),
                () -> AFK.config.triggerCmds,
                val -> AFK.config.triggerCmds = val
            )
            .initial("")
            .controller(StringControllerBuilder::create)
            .build());
        
        return category;
    }
}
