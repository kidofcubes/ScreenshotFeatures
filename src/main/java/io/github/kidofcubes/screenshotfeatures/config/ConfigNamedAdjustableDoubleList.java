package io.github.kidofcubes.screenshotfeatures.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages a dynamic map of named ConfigAdjustableDouble entries.
 * Users can add/remove entries from the in-game GUI.
 * Each entry has a user-defined name, a double value, and a keybind.
 */
public class ConfigNamedAdjustableDoubleList {
    private final String name;
    private final Map<String, NamedEntry> entries = new LinkedHashMap<>();

    public ConfigNamedAdjustableDoubleList(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Collection<NamedEntry> getEntries() {
        return Collections.unmodifiableCollection(entries.values());
    }

    /**
     * Returns all entries as IConfigBase list for use in config GUIs.
     */
    public List<IConfigBase> getEntriesAsConfigBases() {
        List<IConfigBase> configs = new ArrayList<>();
        for (NamedEntry entry : entries.values()) {
            configs.add(entry.getConfig());
        }
        return configs;
    }

    /**
     * Returns all hotkeys from entries for keybind registration.
     */
    public List<IHotkey> getHotkeys() {
        List<IHotkey> hotkeys = new ArrayList<>();
        for (NamedEntry entry : entries.values()) {
            hotkeys.add(entry.getConfig());
        }
        return Collections.unmodifiableList(hotkeys);
    }

    /**
     * Adds a new entry with the given name and default values.
     * @return the created entry, or null if the name is invalid/duplicate
     */
    public NamedEntry addEntry(String entryName, boolean override) {
        if (entryName == null || entryName.isBlank()) return null;
        String key = entryName.trim();
        if (entries.containsKey(key)) return null;

        NamedEntry entry = new NamedEntry(key, override);
        entries.put(key, entry);

        updateKeybinds();
        // ConfigAdjustableDouble constructor already adds to adjustableValues
        return entry;
    }

    private void updateKeybinds(){
//        InputEventHandler.getKeybindManager().updateUsedKeys();

        InputEventHandler.getKeybindManager().unregisterKeybindProvider(ScreenshotFeatures.inputHandler);
        InputEventHandler.getInputManager().unregisterMouseInputHandler(ScreenshotFeatures.inputHandler);

//        InputEventHandler.getKeybindManager().updateUsedKeys();

        InputEventHandler.getKeybindManager().registerKeybindProvider(ScreenshotFeatures.inputHandler);
        InputEventHandler.getInputManager().registerMouseInputHandler(ScreenshotFeatures.inputHandler);

        InputEventHandler.getKeybindManager().updateUsedKeys();
    }

    /**
     * Removes an entry by its name (case-sensitive).
     * @return true if the entry was found and removed
     */
    public boolean removeEntry(String entryName) {
        if (entryName == null || entryName.isBlank()) return false;

        NamedEntry entry = entries.remove(entryName);
        ScreenshotFeatures.adjustableValues.remove(entry.getConfig());
        updateKeybinds();
        return true;
    }

    /**
     * Removes all entries and cleans up registrations.
     */
    public void clearEntries() {
        for (NamedEntry entry : entries.values()) {
            ScreenshotFeatures.adjustableValues.remove(entry.getConfig());
        }
        entries.clear();
        updateKeybinds();
    }

    /**
     * Gets an entry by name (case-sensitive).
     */
    public NamedEntry getEntry(String entryName) {
        if (entryName == null) return null;
        return entries.get(entryName);
    }

    public int getEntryCount() {
        return entries.size();
    }

    // --- Persistence ---

    public JsonElement getAsJsonElement() {
        JsonObject root = new JsonObject();
        JsonArray entriesArray = new JsonArray();

        for (Map.Entry<String, NamedEntry> mapEntry : entries.entrySet()) {
            JsonObject entryObj = new JsonObject();
            entryObj.addProperty("name", mapEntry.getKey());
            entryObj.add("config", mapEntry.getValue().getAsJsonElement());
            entriesArray.add(entryObj);
        }

        root.add("entries", entriesArray);
        return root;
    }

    public void setValueFromJsonElement(JsonElement element) {
        if (element == null || !element.isJsonObject()) {
            clearEntries();
            return;
        }

        JsonObject root = element.getAsJsonObject();

        if (!root.has("entries") || !root.get("entries").isJsonArray()){
            clearEntries();
            return;
        }
        JsonArray entriesArray = root.getAsJsonArray("entries");
        List<String> addedNewEntries = new ArrayList<>();

        for (JsonElement entryElement : entriesArray) {
            if (!entryElement.isJsonObject()) continue;
            JsonObject entryObj = entryElement.getAsJsonObject();

            if (!entryObj.has("name") || !entryObj.has("config")) continue;

            String entryName = entryObj.get("name").getAsString();

            addedNewEntries.add(entryName);

            JsonElement configElement = entryObj.get("config");

            // Create the entry (this also registers it via ConfigAdjustableDouble constructor)
            NamedEntry entry = entries.getOrDefault(entryName, new NamedEntry(entryName, false));
            entry.setValueFromJsonElement(configElement);
            entries.put(entryName, entry);
        }
        for(String entry: entries.keySet()){
            if(!addedNewEntries.contains(entry)){
                entries.remove(entry);
            }
        }
        updateKeybinds();
    }

    /**
     * Represents a single named entry in the list.
     * Each entry wraps a ConfigAdjustableDouble with a user-defined name.
     */
    public static class NamedEntry {
        private final String name;
        public boolean override;
        private final ConfigAdjustableDouble config;

        public NamedEntry(String name, boolean override) {
            this.name = name;
            // Create a ConfigAdjustableDouble with the entry name, default value 0.0, no default hotkey
            this.config = new ConfigAdjustableDouble(
                    name,           // name (used as the config name)
                    0.0,            // default value
                    "",             // default hotkey (none by default)
                    -Double.MAX_VALUE, Double.MAX_VALUE, // min/max
                    1.0,            // multiplier/step
                    KeybindSettings.DEFAULT,
                    "Custom uniform: " + name, // comment
                    name,           // pretty name
                    name            // translated name
            );
            this.override = override;
        }

        public void setValueFromJsonElement(JsonElement jsonElement){
            var object = jsonElement.getAsJsonObject();
            override = object.get("override").getAsBoolean();
            this.config.setValueFromJsonElement(object.get("config"));
        }

        public JsonElement getAsJsonElement() {
            JsonObject object = new JsonObject();
            object.addProperty("override", override);
            object.add("config", this.config.getAsJsonElement());
            return object;
        }

        public String getName() {
            return name;
        }

        public ConfigAdjustableDouble getConfig() {
            return config;
        }

        public double getValue() {
            return config.getDoubleValue();
        }

        public void setValue(double value) {
            config.setDoubleValue(value);
        }
    }
}
