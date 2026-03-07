package cn.taotxi.Superman;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.isxander.yacl3.gui.YACLScreen;

import cn.taotxi.Superman.gui.ConfigScreen;
import cn.taotxi.Superman.module.AFK.AFK;
import cn.taotxi.Superman.module.CountQuantity.CountQuantity;
import cn.taotxi.Superman.module.WorldTrigger.WorldTrigger;
import cn.taotxi.Superman.util.EventBus;
import cn.taotxi.Superman.test.TestCommand;

public class Superman implements ModInitializer {
	public static final String MOD_ID = "superman";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static int tickCounter = 0;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

        LOGGER.info("Initialize Superman !");
		registerSomeEvents();
        registerCommand();
        registerTickEvents();
        TestCommand.register();
	}

    private static void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) {
                return;
            }
            tickCounter++;
            EventBus.checkQueue();
            CountQuantity.registerTickEvents(client, tickCounter);
            WorldTrigger.registerTickEvents(client, tickCounter);
            AFK.registerTickEvents(client, tickCounter);
        });
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            AFK.registerCommand(dispatcher, registryAccess);
            CountQuantity.registerCommand(dispatcher, registryAccess);
            WorldTrigger.registerCommand(dispatcher, registryAccess);
        });
    }

    private void registerSomeEvents() {
        // ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
        //     Message.sendMessage("Screen: " + screen.getClass().getSimpleName());
        // });

        EventBus.register("openConfigGui", (args) -> {
            Minecraft client = Minecraft.getInstance();
            if (client.screen != null) {
                EventBus.post("openConfigGui", args);
                return;
            }
            
            YACLScreen configScreen = (YACLScreen) ConfigScreen.getConfigScreen(null);
            configScreen.init(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
            int tabIndex = -1;
            String title = (String) args.get("title");
            for (int i=0; i<configScreen.tabNavigationBar.getTabs().size(); i++) {
                if (configScreen.tabNavigationBar.getTabs().get(i).getTabTitle().getString().equals(title)) {
                    tabIndex = i;
                    break;
                }
            }
            if (tabIndex == -1) {
                LOGGER.error("Can not find tab with title: " + title);
                return;
            }
            // true: play click sound
            configScreen.tabNavigationBar.selectTab(tabIndex, true);
            client.setScreen(configScreen);
        });
    }
}