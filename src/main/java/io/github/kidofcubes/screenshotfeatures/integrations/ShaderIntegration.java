package io.github.kidofcubes.screenshotfeatures.integrations;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shaderpack.option.ShaderPackOptions;
import net.irisshaders.iris.shaderpack.option.values.MutableOptionValues;
import net.irisshaders.iris.shaderpack.option.values.OptionValues;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.XXHash32;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.treewalk.FileTreeIterator;

import java.io.*;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures.LOGGER;

public class ShaderIntegration {
    public static boolean irisPresent() {
        return FabricLoader.getInstance().isModLoaded("iris");
    }

    private static final Gson gson = new Gson();
    public static String getShaderName() {
        if (irisPresent()) {
            return Iris.getCurrentPackName();
        }
        return "Iris Shaders not found -- could not resolve shader pack";
    }
    public static Map<HashType,String> getShaderHash() {
        return currentShaderPackHash.orElse(Collections.emptyMap());
    }
    public static String getShaderCommit() {
        return currentShaderPackCommit.orElse("No commit found");
    }
    public static String getShaderAllSettings() {
        return currentShaderPackAllSettings.orElse("No settings found");
    }
    public static String getShaderConfiguredSettings() {
        return currentShaderPackConfiguredSettings.orElse("No configured settings found");
    }
    public static String getShaderDiff() {
        return currentShaderPackDiff.orElse("No diff found");
    }
    public static Map<HashType,String> getShaderZipHash() {
        return currentShaderPackZipHash.orElse(Collections.emptyMap());
    }


    public static void onShaderPackLoad(Path shaderPackRoot, Path shaderPackPath, boolean isZip){
        try{
            saveShaderHashes(shaderPackRoot,shaderPackPath,isZip);
        }catch(IOException e){
            LOGGER.warn("Failed to save shader hashes, defaulting to none", e);
            currentShaderPackHash = Optional.empty();
            currentShaderPackZipHash = Optional.empty();
        }
        try{
            saveGitStatus(shaderPackPath,isZip);
        }catch(IOException e){
            LOGGER.warn("Failed to save shader git status, defaulting to none", e);
            currentShaderPackCommit = Optional.empty();
            currentShaderPackDiff = Optional.empty();
        }
        try{
            saveShaderSettings();
        }catch(IOException e){
            LOGGER.warn("Failed to save shader settings, defaulting to none", e);
            currentShaderPackAllSettings = Optional.empty();
            currentShaderPackConfiguredSettings = Optional.empty();
        }
    }
    public static void onShaderPackUnload(){
        currentShaderPackCommit = Optional.empty();
        currentShaderPackDiff = Optional.empty();
        currentShaderPackZipHash = Optional.empty();
        currentShaderPackHash = Optional.empty();
        currentShaderPackAllSettings = Optional.empty();
        currentShaderPackConfiguredSettings = Optional.empty();
    }
    public enum HashType {
        XXH32,
        MD5,
        SHA256;
        public String hash(byte[] bytes){
            if(this==XXH32){
                XXHash32 hasher = new XXHash32();
                hasher.update(bytes);
                return String.format("%08x", hasher.getValue());
            }else if(this==MD5){
                return DigestUtils.md5Hex(bytes);
            }else if(this==SHA256){
                return DigestUtils.sha256Hex(bytes);
            }
            throw new RuntimeException();
        }
        public static Map<HashType,String> allHashes(byte[] bytes){
            Map<HashType,String> map = new HashMap<>();
            for(HashType type: values()) {
                map.put(type,type.hash(bytes));
            }
            return map;
        }
    }


    private static Optional<String> currentShaderPackCommit = Optional.empty();
    private static Optional<String> currentShaderPackDiff = Optional.empty();
    //for convenience, store the hash of the entire shader if its a zip
    private static Optional<Map<HashType,String>> currentShaderPackZipHash = Optional.empty();
    private static Optional<Map<HashType,String>> currentShaderPackHash = Optional.empty();
    private static Optional<String> currentShaderPackAllSettings = Optional.empty();
    private static Optional<String> currentShaderPackConfiguredSettings = Optional.empty();

    private static void saveShaderHashes(Path shaderPackRoot, Path shaderPackPath, boolean isZip) throws IOException{
        currentShaderPackHash = Optional.of(calcHashForPath(shaderPackPath));
        if(isZip){
            try(InputStream stream = Files.newInputStream(shaderPackRoot)){
                currentShaderPackZipHash = Optional.of(HashType.allHashes(stream.readAllBytes()));
            }
        }else{
            currentShaderPackZipHash = Optional.empty();
        }
    }

    private static void saveGitStatus(Path shaderPackPath, boolean isZip) throws IOException{
        if(isZip){ //github doesn't package the .git into zips
            ShaderIntegration.currentShaderPackCommit = Optional.empty();
            ShaderIntegration.currentShaderPackDiff = Optional.empty();
            return;
        }
        RepositoryBuilder builder = new RepositoryBuilder().findGitDir(shaderPackPath.toFile());
        if(builder.getGitDir()==null){
            ShaderIntegration.currentShaderPackCommit = Optional.empty();
            ShaderIntegration.currentShaderPackDiff = Optional.empty();
            return;
        }

        try(Repository repo = builder.build()){
            ShaderIntegration.currentShaderPackCommit = Optional.of(repo.resolve("HEAD").getName());
            DirCacheIterator dci = new DirCacheIterator(repo.readDirCache());
            FileTreeIterator fti = new FileTreeIterator(repo);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            df.setRepository(repo);
            df.format(dci,fti);
            df.flush();
            ShaderIntegration.currentShaderPackDiff = Optional.of(out.toString());
        }
    }
    private static void saveShaderSettings() throws IOException{
        ShaderPackOptions options = Iris.getCurrentPack().get().getShaderPackOptions();
        OptionValues values = options.getOptionValues();
        Properties explicitSettings = new Properties();
        values.getOptionSet().getBooleanOptions().keySet().forEach(key -> explicitSettings.setProperty(key, Boolean.toString(values.getBooleanValueOrDefault(key))));
        values.getOptionSet().getStringOptions().keySet().forEach(key -> explicitSettings.setProperty(key, values.getStringValueOrDefault(key)));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        explicitSettings.store(stream,null);
        currentShaderPackAllSettings = Optional.of(stream.toString());

        //taken from Iris
        MutableOptionValues changedConfigsValues = values.mutableCopy();

        // Store changed values from those currently in use by the shader pack
        Properties configsToSave = new Properties();
        changedConfigsValues.getBooleanValues().forEach((k, v) -> configsToSave.setProperty(k, Boolean.toString(v)));
        changedConfigsValues.getStringValues().forEach(configsToSave::setProperty);
        stream = new ByteArrayOutputStream();
        configsToSave.store(stream,null);
        currentShaderPackConfiguredSettings = Optional.of(stream.toString());
    }

    private static Map<HashType,String> calcHashForPath(Path dirToHash) throws IOException{
        try(Stream<Path> paths = Files.walk(dirToHash,FileVisitOption.FOLLOW_LINKS)){
            List<Path> sortedPaths = paths.sorted(Comparator.comparing(path -> {
                String thing = path.toString();
                return thing;
            })).toList();

            List<InputStream> streams = new ArrayList<>(sortedPaths.size());
            for (Path path : sortedPaths) {
                if(!Files.isDirectory(path)){
                    streams.add(Files.newInputStream(path));
                }
            }

            try(SequenceInputStream seqStream = new SequenceInputStream(Collections.enumeration(streams))){
                return HashType.allHashes(seqStream.readAllBytes());
            }
        }
    }
}
