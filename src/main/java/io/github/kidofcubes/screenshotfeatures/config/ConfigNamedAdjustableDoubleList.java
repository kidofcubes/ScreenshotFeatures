package io.github.kidofcubes.screenshotfeatures.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
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
    public NamedEntry addEntry(String entryName) {
        if (entryName == null || entryName.isBlank()) return null;
        String key = entryName.trim();
        if (entries.containsKey(key)) return null;

        NamedEntry entry = new NamedEntry(key);
        entries.put(key, entry);
        // ConfigAdjustableDouble constructor already adds to adjustableValues
        return entry;
    }

    /**
     * Removes an entry by its name (case-insensitive).
     * @return true if the entry was found and removed
     */
    public boolean removeEntry(String entryName) {
        if (entryName == null || entryName.isBlank()) return false;
        // Find the actual key (case-insensitive)
        String actualKey = null;
        for (String key : entries.keySet()) {
            if (key.equalsIgnoreCase(entryName.trim())) {
                actualKey = key;
                break;
            }
        }
        if (actualKey == null) return false;

        NamedEntry entry = entries.remove(actualKey);
        ScreenshotFeatures.adjustableValues.remove(entry.getConfig());
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
    }

    /**
     * Gets an entry by name (case-insensitive).
     */
    public NamedEntry getEntry(String entryName) {
        if (entryName == null) return null;
        for (Map.Entry<String, NamedEntry> mapEntry : entries.entrySet()) {
            if (mapEntry.getKey().equalsIgnoreCase(entryName.trim())) {
                return mapEntry.getValue();
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

        for (Map.Entry<String, NamedEntry> mapEntry : entries.entrySet()) {
            JsonObject entryObj = new JsonObject();
            entryObj.addProperty("name", mapEntry.getKey());
            entryObj.add("config", mapEntry.getValue().getConfig().getAsJsonElement());
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

            // Create the entry (this also registers it via ConfigAdjustableDouble constructor)
            NamedEntry entry = new NamedEntry(entryName);
            entry.getConfig().setValueFromJsonElement(configElement);
            entries.put(entryName, entry);
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
