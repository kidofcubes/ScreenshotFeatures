package io.github.kidofcubes.screenshotfeatures.integrations;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.kidofcubes.screenshotfeatures.screens.ConfigsGui;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> {
            ConfigsGui gui = new ConfigsGui();
            gui.setParent(screen);
            return gui;
        };
    }
}
