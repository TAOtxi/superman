package cn.taotxi.Superman.gui;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.gui.YACLScreen;
import cn.taotxi.Superman.Superman;
import cn.taotxi.Superman.module.AFK.AFK_Gui;
import cn.taotxi.Superman.module.CountQuantity.CountQuantityGui;
import cn.taotxi.Superman.module.WorldTrigger.WorldTriggerGui;
import cn.taotxi.Superman.util.T;


public class ConfigScreen {
    public static Screen getConfigScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = 
            YetAnotherConfigLib.createBuilder()
                .title(T.tl("gui.config.title"))
                .save(() -> {
                    Superman.LOGGER.info("Config saved...");
                });

        // 实体统计相关模块
        ConfigCategory.Builder entityCategory = CountQuantityGui.createCountQuantityCategoryBuilder(parent);
        builder.category(entityCategory.build());

        // 世界定点触发相关模块
        ConfigCategory.Builder worldTriggerCategory = WorldTriggerGui.createWorldTriggerCategoryBuilder(parent);
        builder.category(worldTriggerCategory.build());

        // AFK 相关模块
        ConfigCategory.Builder afkCategory = AFK_Gui.createAFKCategoryBuilder(parent);
        builder.category(afkCategory.build());

        YetAnotherConfigLib yacl = builder.build();
        return yacl.generateScreen(parent);
    }
    
    public static void reload(YACLScreen screen, Screen parent) {
        Minecraft client = Minecraft.getInstance();
        try {
            int tab = screen.tabNavigationBar == null
                    ? 0
                    : screen.tabNavigationBar.getTabs().indexOf(screen.tabManager.getCurrentTab());
            if (tab == -1)
                tab = 0;
            screen.finishOrSave();
            screen.onClose(); // In case finishOrSave doesn't close it.
            YACLScreen newScreen = (YACLScreen) getConfigScreen(parent);
            newScreen.init(client, screen.width, screen.height);
            try {
                newScreen.tabNavigationBar.selectTab(tab, false);
            } catch (IndexOutOfBoundsException e) {
                Superman.LOGGER.warn(
                        "YACL reload hack attempted to select tab {} but max index was {}",
                        tab,
                        newScreen.tabNavigationBar.getTabs().size() - 1
                );
            }
            client.setScreen(newScreen);
        } catch (Exception e) {
            client.setScreen(parent);
            Superman.LOGGER.error("YACL reload hack failed with exception\n{}", e);
        }
    }


}
