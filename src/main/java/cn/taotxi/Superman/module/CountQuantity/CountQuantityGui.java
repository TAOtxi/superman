package cn.taotxi.Superman.module.CountQuantity;

import java.util.List;

import cn.taotxi.Superman.gui.ConfigScreen;
import cn.taotxi.Superman.gui.Factory;
import cn.taotxi.Superman.module.WorldTrigger.WorldTrigger;
import cn.taotxi.Superman.module.WorldTrigger.WorldTriggerConfig;
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

public class CountQuantityGui {
    public static ConfigCategory.Builder createCountQuantityCategoryBuilder(Screen parent) {
        ConfigCategory.Builder category = 
            ConfigCategory.createBuilder()
                .name(T.tl("countquantity.name"))
                .tooltip(T.tl("countquantity.desc"));

        category.option(Factory.addToggleOption(
            T.tl("module.enabled"),
            T.tl("module.enabled.desc"),
            CountQuantityConfig.getDefaultEnabled(),
            () -> CountQuantity.config.enabled,
            val -> CountQuantity.config.enabled = val
        ));

        category.option(Option.<Integer>createBuilder()
            .name(T.tl("countquantity.countRange"))
            .description(OptionDescription.of(T.tl("countquantity.countRange.desc")))
            .binding(CountQuantityConfig.getDefaultCountRange(),
                () -> CountQuantity.config.countRange,
                val -> CountQuantity.config.countRange = val)
            .controller(IntegerFieldControllerBuilder::create)
            .build());

        category.option(Option.<Integer>createBuilder()
            .name(T.tl("countquantity.updateEntitySummaryInterval"))
            .description(OptionDescription.of(T.tl("countquantity.updateEntitySummaryInterval.desc")))
            .binding(CountQuantityConfig.getDefaultUpdateEntitySummaryInterval(),
                () -> CountQuantity.config.updateEntitySummaryInterval,
                val -> CountQuantity.config.updateEntitySummaryInterval = val)
            .controller(IntegerFieldControllerBuilder::create)
            .build());

        category.option(Factory.addToggleOption(
            T.tl("countquantity.alwaysShowSummary"),
            T.tl("countquantity.alwaysShowSummary.desc"),
            CountQuantityConfig.getDefaultAlwaysShowSummary(),
            () -> CountQuantity.config.alwaysShowSummary,
            val -> CountQuantity.config.alwaysShowSummary = val
        ));
        return category;
    }
}
