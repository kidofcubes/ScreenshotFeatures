package io.github.kidofcubes.screenshotfeatures.metadata;

import com.google.gson.*;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.NodeList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static io.github.kidofcubes.screenshotfeatures.metadata.ScreenshotTagger.merge;

public class ScreenshotUpdater {
    private final static Gson gson = new GsonBuilder().setFormattingStyle(FormattingStyle.COMPACT).create();

    //nothing to do currently if its already jsonobject as of 1.7
    public static JsonObject upgrade(JsonObject jsonObject){
        return jsonObject;
    }
    @NotNull
    /// returns empty jsonobject if no metadata
    public static JsonObject fromPreJsonFormat(File file){
        //pre 1.7
        Map<String,String> entries = new HashMap<>();
        try {
            final ImageInputStream input = ImageIO.createImageInputStream(file);
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            final ImageReader reader = readers.next();
            reader.setInput(input);
            final IIOImage img = reader.readAll(0, null);
            input.close();
            final IIOMetadataNode root = (IIOMetadataNode) img.getMetadata().getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
            final NodeList entriesList = root.getElementsByTagName("TextEntry");

            for (int i = 0; i < entriesList.getLength(); i++) {
                final IIOMetadataNode node = (IIOMetadataNode) entriesList.item(i);
                if (node.hasAttribute("keyword")) {
                    entries.put(node.getAttribute("keyword"), node.getAttribute("value"));
                }
            }

        } catch (final IOException e) {
            ScreenshotFeatures.LOGGER.warn("Error while converting pre 1.7 screenshot: ",e);
            return new JsonObject();
//            throw new RuntimeException(e);
        }
        JsonObject tags = new JsonObject();

        // Add version info
        merge(tags, ScreenshotFeatures.MOD_ID, Map.of("version", "1.7"));

        // Process each old entry and convert to new format
        for(Map.Entry<String,String> entry : entries.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            try{

                if(key.equals("Coordinates")){
                    // Parse coordinate data and add to location section
                    try{
                        // Expected format: "X:[%f] Y:[%f] Z:[%f] Yaw:[%f] Pitch:[%f]"
                        String[] parts = value.replace("[","").replace("]","").split(" ");
                        if(parts.length>=5){
                            merge(tags,"location",Map.of(
                                    "x",Double.parseDouble(parts[0].split(":")[1]),
                                    "y",Double.parseDouble(parts[1].split(":")[1]),
                                    "z",Double.parseDouble(parts[2].split(":")[1]),
                                    "yaw",Float.parseFloat(parts[3].split(":")[1]),
                                    "pitch",Float.parseFloat(parts[4].split(":")[1])
                            ));
                        }
                    }catch(Exception e){
                        // If parsing fails, skip this entry
                    }
                }else if(key.equals("Camera_Coordinates")){
                    // Already in JSON format, parse and add as camera_data
                    try{
                        JsonObject cameraData = gson.fromJson(value,JsonObject.class);
                        tags.add("camera_data",cameraData);
                    }catch(JsonSyntaxException e){
                        // If parsing fails, skip this entry
                    }
                }else if(key.equals("Time")){
                    // Expected format: "GameTimeOfDay:%d GameLunarTime:%d RealTime:%d"
                    String[] parts = value.split(" ");
                    merge(tags,"worldData",Map.of(
                            "time",Long.parseLong(parts[0].split(":")[1]),
                            "lunarTime",Long.parseLong(parts[1].split(":")[1]),
                            "realTime",Long.parseLong(parts[2].split(":")[1])
                    ));
                }else if(key.equals("World/Server Name")){
                    merge(tags,"world_data",Map.of("worldName",value));
                }else if(key.equals("World Seed")){
                    // add later
                }else if(key.equals("Resource Packs")){
                    // Parse resource packs array
                    try{
                        // Expected format: "[%s, %s, ...]"
                        String cleanValue = value.replaceAll("[\\[\\]]",""); // Remove brackets
                        String[] packs = cleanValue.isEmpty() ? new String[0] : cleanValue.split(", ");

                        merge(tags,"resource_packs",Map.of("ids",packs));
                    }catch(Exception e){
                        // If parsing fails, skip this entry
                    }

                }else if(key.equals("Shader Pack")){
                    merge(tags,"shader",Map.of("name",value));

                }else if(key.equals("Shader Pack Hash")){
                    // {"XXH32":"964020f5","MD5":"ee29c4d178ff9513971321714c966f21","SHA256":"985fc76e0ed6b6512507869014104606aedb11d4b9722f1d37bf9d02b1dc3823"}
                    JsonObject tag = new JsonObject();
                    try {
                        tag.add("combinedHash",gson.fromJson(value,JsonObject.class));
                    } catch (JsonSyntaxException e) {
                        // probably xxh32 hash
                        if(value.length()==8){
                            tag.add("combinedHash",gson.fromJson(value,JsonObject.class));
                        }
                    }

                    merge(tags,"shader",tag);
                }else if(key.equals("Shader Pack Zip Hash")){
                    // same as pack hash
                    JsonObject tag = new JsonObject();
                    try {
                        tag.add("zipHash",gson.fromJson(value,JsonObject.class));
                    } catch (JsonSyntaxException e) {
                        // probably xxh32 hash
                        if(!value.equals("No zip hash found, shader may be a folder") && value.length()==8){
                            tag.add("zipHash",gson.fromJson(value,JsonObject.class));
                        }
                    }
                    merge(tags,"shader",tag);
                }else if(key.equals("Shader Pack Git Commit")){
                    merge(tags,"shader",Map.of("commit",value));
                }else if(key.equals("Shader Pack Git Diff")){
                    merge(tags,"shader",Map.of("diff",value));
                }else if(key.equals("Shader Pack Settings")){
                    merge(tags,"shader",Map.of("allSettings",value));
                }else if(key.equals("Shader Pack Configured Settings")){
                    merge(tags,"shader",Map.of("configuredSettings",value));
                }else if(key.equals("Minecraft Version")){
                    merge(tags,"mc_version",Map.of("version",value));
                }else if(key.equals("Misc Minecraft Data")){
                    String[] parts = value.split(" ");
                    merge(tags,"camera_data",Map.of(
                            "gamma",Float.parseFloat(parts[1]),
                            "configuredFov",Integer.parseInt(parts[4]),
                            "fov",Float.parseFloat(parts[6])
                    ));

                }
            }catch(Exception e){
                ScreenshotFeatures.LOGGER.warn("warning while upgrading from pre 1.7 format: \""+key+"\": \""+value+"\"", e);
//                throw new RuntimeException(e);
            }
        }

//                        ScreenshotTagger.writeScreenshotTags(f,tags);
//
//                        // Remove the old metadata entries
//                        final ImageInputStream input = ImageIO.createImageInputStream(file);
//                        final Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
//                        final ImageReader reader = readers.next();
//                        reader.setInput(input);
//                        final IIOImage img = reader.readAll(0,null);
//                        input.close();
//
//                        // Define the old keys to remove
//                        String[] oldKeys = {
//                                "Coordinates","Camera_Coordinates","Time","World/Server Name","World Seed",
//                                "Resource Packs","Shader Pack","Shader Pack Hash","Shader Pack Zip Hash",
//                                "Shader Pack Git Commit","Shader Pack Git Diff","Shader Pack Settings",
//                                "Shader Pack Configured Settings","Minecraft Version","Misc Minecraft Data"
//                        };
//
//                        // Remove each old key
//                        for(String keyToRemove : oldKeys){
//                            removeTextEntry(img.getMetadata(),keyToRemove);
//                        }
//
//                        // Write the image back without the old metadata
//                        final ImageOutputStream out = ImageIO.createImageOutputStream(file);
//                        final ImageWriter writer = ImageIO.getImageWriter(reader);
//                        writer.setOutput(out);
//                        writer.write(img);
//                        out.close();
        return tags;

    }
}