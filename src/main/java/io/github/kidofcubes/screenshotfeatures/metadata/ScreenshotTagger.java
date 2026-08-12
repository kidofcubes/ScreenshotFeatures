package io.github.kidofcubes.screenshotfeatures.metadata;

import com.google.gson.*;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import io.github.kidofcubes.screenshotfeatures.integrations.ShaderIntegration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.NodeList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures.client;

public class ScreenshotTagger {
    public final static Gson gson = new GsonBuilder().setFormattingStyle(FormattingStyle.COMPACT).create();

    public static void merge(JsonObject orig, String key, Map<?,?> map){
        merge(orig,key,(JsonObject)gson.toJsonTree(map));
    }
    public static void merge(JsonObject orig, String key, JsonObject values){
        if(!orig.has(key)){
            orig.add(key,new JsonObject());
        }
        for(var entry: values.entrySet()){
            orig.getAsJsonObject(key).add(entry.getKey(),entry.getValue());
        }
    }
    public static JsonObject getTags() {
        JsonObject tags = new JsonObject();
        if(client.player == null) {
            return tags;
        }

        float tickProgress = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        tags.add(ScreenshotFeatures.MOD_ID, gson.toJsonTree(Map.of(
                "version",FabricLoader.getInstance().getModContainer(ScreenshotFeatures.MOD_ID).get().getMetadata().getVersion().getFriendlyString()
        )));

        if(Configs.Metadata.LOCATION.getBooleanValue()) {
            merge(tags, "location", Map.of(
                    "x",client.player.getX(),
                    "y",client.player.getY(),
                    "z",client.player.getZ(),
                    "yaw",client.player.getYRot(tickProgress),
                    "pitch", client.player.getXRot(tickProgress)
            ));
        }
        if(Configs.Metadata.CAMERA_DATA.getBooleanValue()) {
            merge(tags, "cameraData", Map.of(
                    "x", client.gameRenderer.mainCamera().position().x,
                    "y", client.gameRenderer.mainCamera().position().y,
                    "z", client.gameRenderer.mainCamera().position().z,
                    "yaw", client.gameRenderer.mainCamera().yaw(),
                    "pitch", client.gameRenderer.mainCamera().xRot(),
                    "gamma", client.options.gamma().get(),
                    "configuredFov", client.options.fov().get(),
                    "fov", client.gameRenderer.mainCamera().getFov()
            ));
        }
        ServerLevel serverWorld = null;
        if (client.isLocalServer()){
            serverWorld = client.getSingleplayerServer().getLevel(client.level.dimension());
        }
        if(Configs.Metadata.WORLD_DATA.getBooleanValue()) {
            String worldName;
            if (client.isLocalServer()) {
                worldName = ((ServerLevelData)serverWorld.getLevelData()).getLevelName();
            } else {
                worldName = "MULTIPLAYER_WORLD_NAME";
            }
            merge(tags, "worldData", Map.of(
                    "worldName", worldName,
                    "time", client.level.getGameTime()
            ));
        }
        if(Configs.Metadata.WORLD_SEED.getBooleanValue()){
            merge(tags, "worldData", Map.of(
                    "seed", client.isLocalServer() ? Long.toString(serverWorld.getSeed()) : "MULTIPLAYER_WORLD_SEED"
            ));
        }
        if(Configs.Metadata.RESOURCE_PACKS.getBooleanValue()) {
            List<PackResources> resourcePacks = client.getResourceManager().listPacks().toList();
            merge(tags, "resourcePacks", Map.of(
                    "ids", resourcePacks.stream().map(rp -> rp.location().id()).toList()
            ));
        }
        if(Configs.Metadata.MOD_LIST.getBooleanValue()) {
            JsonElement modList = gson.toJsonTree(FabricLoader.getInstance().getAllMods().stream().map(modContainer -> {
                return Map.of("id",modContainer.getMetadata().getId(),"version",modContainer.getMetadata().getVersion().getFriendlyString());
            }).toList());
            merge(tags, "mods", Map.of(
                    "mods", modList
            ));
        }
        if(Configs.Metadata.SHADER_PACK_NAME.getBooleanValue()) {
            merge(tags, "shader" ,Map.of(
                    "name",ShaderIntegration.getShaderName()
            ));
        }
        if(Configs.Metadata.SHADER_PACK_HASH.getBooleanValue()) {
            merge(tags, "shader", Map.of(
                    "combinedHash", ShaderIntegration.getShaderHash(),
                    "zipHash", ShaderIntegration.getShaderZipHash()
            ));
        }
        if(Configs.Metadata.SHADER_PACK_COMMIT.getBooleanValue()) {
            merge(tags, "shader", Map.of(
                    "commit", ShaderIntegration.getShaderCommit()
            ));
        }
        if(Configs.Metadata.SHADER_PACK_DIFF.getBooleanValue()) {
            merge(tags, "shader", Map.of(
                    "gitDiff", ShaderIntegration.getShaderDiff()
            ));
        }
        if(Configs.Metadata.SHADER_PACK_SETTINGS.getBooleanValue()) {
            merge(tags, "shader", Map.of(
                    "allSettings", ShaderIntegration.getShaderAllSettings(),
                    "configuredSettings", ShaderIntegration.getShaderConfiguredSettings()
            ));
        }
        if(Configs.Metadata.MC_VERSION.getBooleanValue()) {
            merge(tags, "mcData", Map.of(
                    "version", client.getLaunchedVersion()
            ));
        }

        return tags;
    }


    @NotNull
    public static JsonObject getScreenshotTags(final File f) throws IOException {
        ImageInputStream input = ImageIO.createImageInputStream(f);
        ImageReader reader = ImageIO.getImageReaders(input).next();
        reader.setInput(input);
        IIOImage img = reader.readAll(0, null);
        input.close();

        final IIOMetadataNode root = (IIOMetadataNode) img.getMetadata().getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
        final NodeList entries = root.getElementsByTagName("TextEntry");

        for (int i = 0; i < entries.getLength(); i++) {
            final IIOMetadataNode node = (IIOMetadataNode) entries.item(i);
            if (node.getAttribute("keyword").equals(ScreenshotFeatures.MOD_ID)) {
                return ScreenshotUpdater.upgrade(gson.fromJson(node.getAttribute("value"), JsonObject.class));
            }
        }

        //doesn't have metadata or is old format
        return ScreenshotUpdater.upgrade(ScreenshotUpdater.fromPreJsonFormat(f));
    }

    public static void writeScreenshotTags(final File f, JsonObject tags) throws IOException {
        final ImageInputStream input = ImageIO.createImageInputStream(f);
        final Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        final ImageReader reader = readers.next();
        reader.setInput(input);
        final IIOImage img = reader.readAll(0, null);

        IIOMetadataNode textEntry = new IIOMetadataNode("TextEntry");
        textEntry.setAttribute("keyword", ScreenshotFeatures.MOD_ID);
        textEntry.setAttribute("value", gson.toJson(tags));

        IIOMetadataNode text = new IIOMetadataNode("Text");
        text.appendChild(textEntry);

        IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
        root.appendChild(text);

        img.getMetadata().mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);

        input.close();
        final ImageOutputStream out = ImageIO.createImageOutputStream(f);
        final ImageWriter writer = ImageIO.getImageWriter(reader);
        writer.setOutput(out);
        writer.write(img);
        out.close();
    }

}
