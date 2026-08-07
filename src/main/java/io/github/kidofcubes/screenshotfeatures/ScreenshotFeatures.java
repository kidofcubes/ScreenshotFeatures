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
import io.github.kidofcubes.screenshotfeatures.config.ConfigAdjustableDouble;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import io.github.kidofcubes.screenshotfeatures.screens.ConfigsGui;
import io.github.kidofcubes.screenshotfeatures.integrations.FabrishotIntegration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Environment(EnvType.CLIENT)
public class ScreenshotFeatures implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("ScreenshotFeatures");
    public static Minecraft client;
    public static final InputHandler inputHandler = new InputHandler();
    public static final String MOD_ID = "screenshotfeatures";
    public static List<ConfigAdjustableDouble> adjustableValues = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        client = Minecraft.getInstance();
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
        CameraMatrixManager.register();

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

        assert client.player!=null;
        for(ConfigAdjustableDouble value : adjustableValues){
            if(value.getKeybind().isKeybindHeld()){
                value.setDoubleValue(value.getDoubleValue() + modifier);
                String formattedFloat = String.format("%.5f",value.getDoubleValue());
                client.player.sendOverlayMessage(Component.translatable("screenshotfeatures.messages.valueChange",value.getTranslatedName(),formattedFloat));
                used=true;
            }
        }

        if(Configs.IngameTools.DOF_MODIFIER.getKeybind().isKeybindHeld()){
            Configs.IngameTools.DOF_OVERRIDE_VALUE.setDoubleValue(Configs.IngameTools.DOF_OVERRIDE_VALUE.getDoubleValue() + (modifier*Configs.IngameTools.DOF_STEP.getDoubleValue()));
            String formattedFloat = String.format("%.5f",Configs.IngameTools.DOF_OVERRIDE_VALUE.getDoubleValue());
            client.player.sendOverlayMessage(Component.translatable("screenshotfeatures.messages.dofOverrideValueChange",formattedFloat));
            used=true;
        }
        if(Configs.IngameTools.TIME_MODIFIER.getKeybind().isKeybindHeld()){
            Configs.IngameTools.TIME_OVERRIDE_VALUE.setIntegerValue(Configs.IngameTools.TIME_OVERRIDE_VALUE.getIntegerValue() + (int)Math.round(modifier*Configs.IngameTools.TIME_STEP.getIntegerValue()));
            client.player.sendOverlayMessage(Component.translatable("screenshotfeatures.messages.timeOverrideValueChange",Configs.IngameTools.TIME_OVERRIDE_VALUE.getIntegerValue()));
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
            for (IHotkey hotkey : Configs.CameraMatrix.HOTKEYS) {
                manager.addKeybindToMap(hotkey.getKeybind());
            }
            // Register keybinds from dynamic custom uniform entries
            for (IHotkey hotkey : Configs.CustomUniforms.getAllHotkeys()) {
                manager.addKeybindToMap(hotkey.getKeybind());
            }
        }

        @Override
        public void addHotkeys(IKeybindManager manager) {
            manager.addHotkeysForCategory(MOD_ID, MOD_ID+".hotkeys.category.ingame_hotkeys", Configs.IngameTools.HOTKEYS);
            manager.addHotkeysForCategory(MOD_ID, MOD_ID+".hotkeys.category.camera_matrix_hotkeys", Configs.CameraMatrix.HOTKEYS);
            manager.addHotkeysForCategory(MOD_ID, MOD_ID+".hotkeys.category.custom_uniforms_hotkeys", Configs.CustomUniforms.getAllHotkeys());
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

    private void register(CommandDispatcher<CommandSourceStack> dispatcher) {
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

    private int takeScreenshot(CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            Screenshot.grab(
                    new File("."),
                    client.gameRenderer.mainRenderTarget(),
                    context.getSource()::sendSystemMessage
            );
        } else {
            context.getSource().sendSystemMessage(Component.literal("This command can only be executed by players!"));
        }
        return 1;
    }

    private int takeScreenshot(CommandContext<CommandSourceStack> context,String filename) {
        if (context.getSource().isPlayer()) {
            Screenshot.grab(
                    new File("."),
                    filename,
                    client.gameRenderer.mainRenderTarget(),
                    1,
                    context.getSource()::sendSystemMessage
            );
        } else {
            context.getSource().sendSystemMessage(Component.literal("This command can only be executed by players!"));
        }
        return 1;
    }
}
