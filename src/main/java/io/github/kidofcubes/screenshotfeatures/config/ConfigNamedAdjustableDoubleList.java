package io.github.kidofcubes.screenshotfeatures.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages a dynamic list of named ConfigAdjustableDouble entries.
 * Users can add/remove entries from the in-game GUI.
 * Each entry has a user-defined name, a double value, and a keybind.
 */
public class ConfigNamedAdjustableDoubleList {
    private final String name;
    private final List<NamedEntry> entries = new ArrayList<>();
    private final List<IHotkey> hotkeys = new ArrayList<>();

    public ConfigNamedAdjustableDoubleList(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<NamedEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Returns all entries as IConfigBase list for use in config GUIs.
     */
    public List<IConfigBase> getEntriesAsConfigBases() {
        List<IConfigBase> configs = new ArrayList<>();
        for (NamedEntry entry : entries) {
            configs.add(entry.getConfig());
        }
        return configs;
    }

    /**
     * Returns all hotkeys from entries for keybind registration.
     */
    public List<IHotkey> getHotkeys() {
        return Collections.unmodifiableList(hotkeys);
    }

    /**
     * Adds a new entry with the given name and default values.
     * @return the created entry, or null if the name is invalid/duplicate
     */
    public NamedEntry addEntry(String entryName) {
        if (entryName == null || entryName.isBlank()) return null;
        // Check for duplicate names
        for (NamedEntry existing : entries) {
            if (existing.getName().equalsIgnoreCase(entryName)) return null;
        }

        NamedEntry entry = new NamedEntry(entryName.trim());
        entries.add(entry);
        hotkeys.add(entry.getConfig());
        // ConfigAdjustableDouble constructor already adds to adjustableValues
        return entry;
    }

    /**
     * Removes an entry by its index.
     * @return true if the entry was found and removed
     */
    public boolean removeEntry(int index) {
        if (index < 0 || index >= entries.size()) return false;
        NamedEntry entry = entries.remove(index);
        hotkeys.remove(entry.getConfig());
        ScreenshotFeatures.adjustableValues.remove(entry.getConfig());
        return true;
    }

    /**
     * Removes an entry by its name.
     * @return true if the entry was found and removed
     */
    public boolean removeEntry(String entryName) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getName().equalsIgnoreCase(entryName)) {
                return removeEntry(i);
            }
        }
        return false;
    }

    /**
     * Removes all entries and cleans up registrations.
     */
    public void clearEntries() {
        for (NamedEntry entry : new ArrayList<>(entries)) {
            hotkeys.remove(entry.getConfig());
            ScreenshotFeatures.adjustableValues.remove(entry.getConfig());
        }
        entries.clear();
    }

    /**
     * Gets an entry by name (case-insensitive).
     */
    public NamedEntry getEntry(String entryName) {
        for (NamedEntry entry : entries) {
            if (entry.getName().equalsIgnoreCase(entryName)) {
                return entry;
            }
        }
        return null;
    }

    public int getEntryCount() {
        return entries.size();
    }

    // --- Persistence ---

    public JsonElement getAsJsonElement() {
        JsonObject root = new JsonObject();
        JsonArray entriesArray = new JsonArray();

        for (NamedEntry entry : entries) {
            JsonObject entryObj = new JsonObject();
            entryObj.addProperty("name", entry.getName());
            entryObj.add("config", entry.getConfig().getAsJsonElement());
            entriesArray.add(entryObj);
        }

        root.add("entries", entriesArray);
        return root;
    }

    public void setValueFromJsonElement(JsonElement element) {
        // Clear existing entries first
        clearEntries();

        if (element == null || !element.isJsonObject()) return;
        JsonObject root = element.getAsJsonObject();

        if (!root.has("entries") || !root.get("entries").isJsonArray()) return;
        JsonArray entriesArray = root.getAsJsonArray("entries");

        for (JsonElement entryElement : entriesArray) {
            if (!entryElement.isJsonObject()) continue;
            JsonObject entryObj = entryElement.getAsJsonObject();

            if (!entryObj.has("name") || !entryObj.has("config")) continue;

            String entryName = entryObj.get("name").getAsString();
            JsonElement configElement = entryObj.get("config");

            // Create the entry (this also registers it)
            NamedEntry entry = new NamedEntry(entryName);
            entry.getConfig().setValueFromJsonElement(configElement);
            entries.add(entry);
            hotkeys.add(entry.getConfig());
            // ConfigAdjustableDouble constructor already adds to adjustableValues
        }
    }

    /**
     * Represents a single named entry in the list.
     * Each entry wraps a ConfigAdjustableDouble with a user-defined name.
     */
    public static class NamedEntry {
        private final String name;
        private final ConfigAdjustableDouble config;

        public NamedEntry(String name) {
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
