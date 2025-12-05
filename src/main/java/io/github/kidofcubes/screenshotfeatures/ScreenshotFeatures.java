package io.github.kidofcubes.screenshotfeatures;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.util.GuiUtils;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import io.github.kidofcubes.screenshotfeatures.screens.ConfigsGui;
import io.github.kidofcubes.screenshotfeatures.integrations.FabrishotIntegration;
import io.github.kidofcubes.screenshotfeatures.integrations.OrthoCameraIntegration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@Environment(EnvType.CLIENT)
public class ScreenshotFeatures implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ScreenshotFeatures");
    public static MinecraftClient client;
    private static final InputHandler inputHandler = new InputHandler();
    public static final String MOD_ID = "screenshotfeatures";

    @Override
    public void onInitializeClient() {
        client = MinecraftClient.getInstance();
        registerScreenshotCommands();
        InitializationHandler.getInstance().registerInitializationHandler(() -> {
            ConfigManager.getInstance().registerConfigHandler(ScreenshotFeatures.MOD_ID, new Configs());

            InputEventHandler.getKeybindManager().registerKeybindProvider(inputHandler);
            InputEventHandler.getInputManager().registerMouseInputHandler(inputHandler);
            IHotkeyCallback valueChange = (keyAction, iKeybind) -> {
                double modifier = iKeybind == Configs.IngameTools.INCREASE_VALUE.getKeybind() ? 1.0 : -1.0;
                return changeValue(modifier);
            };
            Configs.IngameTools.INCREASE_VALUE.getKeybind().setCallback(valueChange);
            Configs.IngameTools.DECREASE_VALUE.getKeybind().setCallback(valueChange);
            Configs.IngameTools.CYCLE_WEATHER_OVERRIDE.getKeybind().setCallback((keyAction, iKeybind) -> {
                Configs.IngameTools.WEATHER_OVERRIDE_VALUE.setOptionListValue(Configs.IngameTools.WEATHER_OVERRIDE_VALUE.getOptionListValue().cycle(true));
                return true;
            });
            Configs.IngameTools.OPEN_CONFIG.getKeybind().setCallback(((keyAction, iKeybind) -> {
                GuiBase.openGui(new ConfigsGui());
                return true;
            }));
        });
        FabrishotIntegration.register();
        OrthoCameraIntegration.register();

        LOGGER.info("ScreenshotFeatures loaded.");
    }
    public static boolean changeValue(double modifier){
        if(Configs.IngameTools.LARGE_VALUE_MODIFIER.getKeybind().isKeybindHeld()){
            modifier *= Configs.IngameTools.LARGE_VALUE_MULTIPLIER.getDoubleValue();
        }
        if(Configs.IngameTools.SMALL_VALUE_MODIFIER.getKeybind().isKeybindHeld()){
            modifier *= Configs.IngameTools.SMALL_VALUE_MULTIPLIER.getDoubleValue();
        }
        boolean used = false;

        if(Configs.IngameTools.DOF_MODIFIER.getKeybind().isKeybindHeld()){
            Configs.IngameTools.DOF_OVERRIDE_VALUE.setDoubleValue(Configs.IngameTools.DOF_OVERRIDE_VALUE.getDoubleValue() + (modifier*Configs.IngameTools.DOF_STEP.getDoubleValue()));
            client.player.sendMessage(Text.translatable("screenshotfeatures.messages.dofOverrideValueChange",Configs.IngameTools.DOF_OVERRIDE_VALUE.getDoubleValue()), true);
            used=true;
        }
        if(Configs.IngameTools.TIME_MODIFIER.getKeybind().isKeybindHeld()){
            Configs.IngameTools.TIME_OVERRIDE_VALUE.setIntegerValue(Configs.IngameTools.TIME_OVERRIDE_VALUE.getIntegerValue() + (int)Math.round(modifier*Configs.IngameTools.TIME_STEP.getIntegerValue()));
            client.player.sendMessage(Text.translatable("screenshotfeatures.messages.timeOverrideValueChange",Configs.IngameTools.TIME_OVERRIDE_VALUE.getIntegerValue()), true);
            used=true;
        }

        if(Configs.OrthoCameraIntegration.X_SCALE_MODIFIER.getKeybind().isKeybindHeld()){
            Configs.OrthoCameraIntegration.X_SCALE.setFloatValue(Configs.OrthoCameraIntegration.X_SCALE.getFloatValue()+(float)(modifier*Configs.OrthoCameraIntegration.X_SCALE_STEP.getFloatValue()));
            client.player.sendMessage(Text.translatable("screenshotfeatures.messages.orthoScaleXValueChange",Configs.OrthoCameraIntegration.X_SCALE.getFloatValue()),true);
            used = true;
        }
        if(Configs.OrthoCameraIntegration.Y_SCALE_MODIFIER.getKeybind().isKeybindHeld()){
            Configs.OrthoCameraIntegration.Y_SCALE.setFloatValue(Configs.OrthoCameraIntegration.Y_SCALE.getFloatValue() + (float)(modifier*Configs.OrthoCameraIntegration.Y_SCALE_STEP.getFloatValue()));
            client.player.sendMessage(Text.translatable("screenshotfeatures.messages.orthoScaleYValueChange",Configs.OrthoCameraIntegration.Y_SCALE.getFloatValue()), true);
            used=true;
        }
        if(Configs.OrthoCameraIntegration.MIN_DISTANCE_MODIFIER.getKeybind().isKeybindHeld()){
            Configs.OrthoCameraIntegration.MIN_DISTANCE.setFloatValue(Configs.OrthoCameraIntegration.MIN_DISTANCE.getFloatValue() + (float)(modifier*Configs.OrthoCameraIntegration.MIN_DISTANCE_STEP.getFloatValue()));
            client.player.sendMessage(Text.translatable("screenshotfeatures.messages.orthoMinDistanceValueChange",Configs.OrthoCameraIntegration.MIN_DISTANCE.getFloatValue()), true);
            used=true;
        }
        if(Configs.OrthoCameraIntegration.MAX_DISTANCE_MODIFIER.getKeybind().isKeybindHeld()){
            Configs.OrthoCameraIntegration.MAX_DISTANCE.setFloatValue(Configs.OrthoCameraIntegration.MAX_DISTANCE.getFloatValue() + (float)(modifier*Configs.OrthoCameraIntegration.MAX_DISTANCE_STEP.getFloatValue()));
            client.player.sendMessage(Text.translatable("screenshotfeatures.messages.orthoMaxDistanceValueChange",Configs.OrthoCameraIntegration.MAX_DISTANCE.getFloatValue()), true);
            used=true;
        }
        return used;
    }

    static class InputHandler implements IKeybindProvider, IMouseInputHandler {
        @Override
        public void addKeysToMap(IKeybindManager manager) {
            for (IHotkey hotkey : Configs.IngameTools.HOTKEYS) {
                manager.addKeybindToMap(hotkey.getKeybind());
            }
            for (IHotkey hotkey : Configs.OrthoCameraIntegration.HOTKEYS) {
                manager.addKeybindToMap(hotkey.getKeybind());
            }
        }

        @Override
        public void addHotkeys(IKeybindManager manager) {
            manager.addHotkeysForCategory(MOD_ID, MOD_ID+".hotkeys.category.ingame_hotkeys", Configs.IngameTools.HOTKEYS);
            manager.addHotkeysForCategory(MOD_ID, MOD_ID+".hotkeys.category.orthocameraintegration_hotkeys", Configs.OrthoCameraIntegration.HOTKEYS);
        }

        @Override
        public boolean onMouseScroll(double mouseX,double mouseY,double amount){
            // Not in a GUI
            if (GuiUtils.getCurrentScreen() == null && amount != 0.0 && Configs.IngameTools.ALLOW_MWHEEL_CHANGE_VALUE.getBooleanValue()) {
                double modifier = amount * Configs.IngameTools.MWHEEL_MULTIPLIER.getDoubleValue();
                return changeValue(modifier);
            }

            return false;
        }
    }

    private void registerScreenshotCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Believe me, I dislike this code duplication, but every single way of
        // "aliasing" that I attempted through brigadier
        // didn't work out so well.
        dispatcher.register(literal("screenshot")
                .executes(this::takeScreenshot)
                .then(argument("filename", StringArgumentType.string())
                        .executes(context ->
                                takeScreenshot(context, StringArgumentType.getString(context, "filename"))
                        )
                )
        );
        dispatcher.register(literal("ss")
                .executes(this::takeScreenshot)
                .then(argument("filename", StringArgumentType.string())
                        .executes(context ->
                                takeScreenshot(context, StringArgumentType.getString(context, "filename"))
                        )
                )
        );
    }

    private int takeScreenshot(CommandContext<ServerCommandSource> context) {
        if (context.getSource().isExecutedByPlayer()) {
            ScreenshotRecorder.saveScreenshot(
                    new File("."),
                    client.getFramebuffer(),
                    context.getSource()::sendMessage
            );
        } else {
            context.getSource().sendMessage(Text.literal("This command can only be executed by players!"));
        }
        return 1;
    }

    private int takeScreenshot(CommandContext<ServerCommandSource> context, String filename) {
        if (context.getSource().isExecutedByPlayer()) {
            ScreenshotRecorder.saveScreenshot(
                    new File("."),
                    filename,
                    client.getFramebuffer(),
                    1,
                    context.getSource()::sendMessage
            );
        } else {
            context.getSource().sendMessage(Text.literal("This command can only be executed by players!"));
        }
        return 1;
    }
}
