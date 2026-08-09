package io.github.kidofcubes.screenshotfeatures.mixin;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.google.common.collect.Streams;
import io.github.kidofcubes.screenshotfeatures.ScreenshotFeatures;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.include.IncludeProcessor;
import net.irisshaders.iris.shaderpack.preprocessor.GlslCollectingListener;
import org.anarres.cpp.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Mixin(value = ShaderPack.class, remap = false)
public class ShaderPackMixin {
    @ModifyArgs(method="lambda$new$8", at=@At(value="INVOKE", target="Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"))
    private static void injectUniforms(Args args){
//        source still has #defines here
        // just need to try to fix anything that was using the defined variable
        // then add the uniform to the top

        List<String> overridedUniforms = new ArrayList<>();
        for(var entry: Configs.CustomUniforms.CUSTOM_UNIFORMS.getEntries()){
            if(entry.override){
                overridedUniforms.add(entry.getName());
            }
        }


        var iterable = (Iterable<StringPair>)args.get(1);
        Iterable<StringPair> filtered = Streams.stream(iterable).filter(pair -> {
            return !overridedUniforms.contains(pair.key());
        })::iterator;
        args.set(1,filtered); //remove the macro from the next actuall preprocessing stage


        String source = args.get(0);

        //remove all things that define the thing
        for(String overridedUniform: overridedUniforms){
            String regex = "(?m)^(\\s*#define\\s+)" + Pattern.quote(overridedUniform) + "(\\s+)[+-]?\\d*(?:\\.\\d+)?(\\s*//.*)?$";
            source = source.replaceAll(regex, "");
        }



        StringBuilder builder = new StringBuilder();
//        for(int i=0;i<tokenList.size();i++){
//            builder.append(tokenList.get(i).getText());
//        }
//        builder.append((String)args.get(0));
        builder.append((String)source);
        StringBuilder uniformsBuilder = new StringBuilder();
        for(String overridedUniform: overridedUniforms){
            uniformsBuilder.append("uniform float " + overridedUniform + ";\n");
        }
        String origBuilder = builder.toString();
        builder.insert( builder.indexOf("\n", builder.indexOf("#version"))+1,uniformsBuilder);
        args.set(0,builder.toString());

//        source.set(builder.toString());
//

//        builder.append(tok.getText());
    }

    @Inject(method="lambda$new$8", at=@At(value="RETURN", ordinal = 2), cancellable = true)
    private static void unConst(List disabledPrograms,IncludeProcessor includeProcessor,Iterable finalEnvironmentDefines1,AbsolutePackPath path,CallbackInfoReturnable<String> cir){
        String origSource = cir.getReturnValue();
        boolean toPrint = false;
        String name="NULLNAME";

        if(ScreenshotFeatures.DEBUG && origSource.contains("CLOUDS_CIRRUS_ALTITUDE")){
            toPrint=true;
            name=path.getPathString().replace("/","-");

//            try{
//                byte[] hash = MessageDigest.getInstance("SHA-256").digest(origSource.getBytes());
//                // Convert hash bytes to hex string
//                StringBuilder hex = new StringBuilder();
//                for(byte b : hash) hex.append(String.format("%02x",b));
//                name = hex.toString().substring(60);
//            }catch(NoSuchAlgorithmException e){
//                throw new RuntimeException(e);
//            }
            System.out.println("====================================================");
            System.out.println("STARTING UNCONST FOR "+name);
        }

        List<String> overridedWithUniforms = new ArrayList<>();
        for(var entry: Configs.CustomUniforms.CUSTOM_UNIFORMS.getEntries()){
            if(entry.override){
                overridedWithUniforms.add(entry.getName());
            }
        }
        String source = cir.getReturnValue();

        source = source.replace("#version", "#warning IRIS_JCPP_GLSL_VERSION");
        source = source.replace("#extension", "#warning IRIS_JCPP_GLSL_EXTENSION");
        source = source.replace("\u0000", "");
        Preprocessor pp = new Preprocessor();

//        try {
//            for(StringPair envDefine : environmentDefines) {
//                pp.addMacro(envDefine.key(), envDefine.value());
//            }
//        } catch (LexerException e) {
//            throw new RuntimeException("Unexpected LexerException processing macros", e);
//        }

        GlslCollectingListener listener = new GlslCollectingListener();
        pp.setListener(listener);
        pp.addInput(new StringLexerSource(source, true));
        pp.addFeature(Feature.KEEPCOMMENTS);

        List<Token> tokenList = new ArrayList<>();

        try {
            while(true) {
                Token tok = pp.token();
                if (tok == null || tok.getType() == Token.EOF) {
                    break;
                }
                tokenList.add(tok);
            }
        } catch (Exception e) {
            throw new RuntimeException("GLSL source pre-processing failed", e);
        }

        List<Predicate<Token>> constMatch = List.of(
            (x -> x.getText().equals("const")), // const
            (x -> x.getText().equals("float")),
            (x -> x.getType()==Token.IDENTIFIER),
            (x -> x.getText().equals("=")), //61
            (x -> x.getText().equals(";"))
        );

        int searchingIndex = 0;
        Map<String, List<Token>> toBeUnConsted = new HashMap<>();
        AtomicInteger blocksDeep = new AtomicInteger(0);
        while(true){
            List<Integer> matchedConst = matchIndex(tokenList, searchingIndex, (i, x) -> {
                if(i==0){
                    if(x.getText().equals("{")){
                        blocksDeep.getAndIncrement();
                    }else if(x.getText().equals("}")){
                        blocksDeep.getAndDecrement();
                    }
                }
                return !x.getText().equals(";");
            }, constMatch);
            if(matchedConst.getFirst()==-1){
                break;
            }

            boolean unconst = false;
            for(int i=matchedConst.get(3)+1;i<matchedConst.get(4);i++){
                if(overridedWithUniforms.contains(tokenList.get(i).getText())){ //find consts effected by our #define removal
                    if(blocksDeep.get()!=0){ //if inline const
                        //just remove the const part, inlined ones will just work
                        if(ScreenshotFeatures.DEBUG){
                            System.out.println("BLOCKSDEEP FIRST was "+blocksDeep+", signifying an inline const");
                            for(int j=matchedConst.get(0);j<=matchedConst.get(4);j++){
                                System.out.print(tokenList.get(j).getText());
                            }
                            System.out.println();
                        }
                        tokenList.set(matchedConst.get(0), new Token(Token.WHITESPACE, -1, -1, "", null));
                    }else{
                        //this is a root level const define, we need to substitute this in everywhere
                        unconst = true;
                    }
                    break;
                }
            }

            if(unconst){
                //old to do (mostly works) remove the const definition, put it in parentheses, and replace usages of it everywhere else
                List<Token> replacement = new ArrayList<>(tokenList.subList(matchedConst.get(3)+1, matchedConst.get(4)));
                replacement.addFirst(new Token('(', -1, -1, "(", null));
                replacement.add(new Token(')', -1, -1, ")", null));
                toBeUnConsted.put(tokenList.get(matchedConst.get(2)).getText(), replacement);

                if(ScreenshotFeatures.DEBUG){
                    System.out.print("in FIRST\n\"");
                    for(int j=matchedConst.get(0);j<=matchedConst.get(4);j++){
                        System.out.print(tokenList.get(j).getText());
                    }
                    System.out.println("\"");
                    System.out.println("replaced all of it with empty");
                }
                for(int i=matchedConst.get(0);i<=matchedConst.get(4);i++){
                    tokenList.set(i, new Token(Token.WHITESPACE, -1, -1, "", null));
                }
//                tokenList.set(matchedConst.get(0), new Token(Token.WHITESPACE, -1, -1, "uniform", null));

            }

            searchingIndex = matchedConst.getFirst() + 1;
        }
        searchingIndex=0;
        //technically, this should definitely be zero, but setting it here again just to be safe
        blocksDeep.set(0);

        //now we search for all root level consts that depend on things tobeunconsted, replace them, and delete them
        while(true){
            List<Integer> matchedConst = matchIndex(tokenList, searchingIndex, (i, x) -> {
                if(i == 0){
                    if(x.getText().equals("{")){
                        blocksDeep.getAndIncrement();
                    }else if(x.getText().equals("}")){
                        blocksDeep.getAndDecrement();
                    }
                }

                return !x.getText().equals(";");
            }, constMatch);
            if(matchedConst.getFirst()==-1){
                break;
            }

            boolean unconst = false;
            for(int i=matchedConst.get(3)+1;i<matchedConst.get(4);i++){
                List<Token> toReplace = toBeUnConsted.get(tokenList.get(i).getText());
                if(toReplace!=null){ //found usage of a tobeunconsted in this const
                    if(ScreenshotFeatures.DEBUG){
                        System.out.println("in ");
                        for(int j=matchedConst.get(0);j<=matchedConst.get(4);j++){
                            System.out.print(tokenList.get(j).getText());
                        }
                        System.out.println();
                        System.out.println("replaced "+tokenList.get(i).getText()+" with ");
                        for(Token token: toReplace){
                            System.out.print(token.getText());
                        }
                        System.out.println();
                    }
                    if(blocksDeep.get()!=0){
                        if(ScreenshotFeatures.DEBUG){
                            System.out.println("blocksdeep was "+blocksDeep+", signifying an inline const");
                        }
                        //this is an inline const, we just unconst it and do replacements, no need for searching for dependents of this
                        tokenList.set(matchedConst.get(0), new Token(Token.WHITESPACE, -1, -1, "", null));
                    }else{
                        //not inline const, we need to calculate dependents of this
                        unconst = true;
                    }
                    tokenList.addAll(i+1, toReplace);
                    tokenList.remove(i);
                    matchedConst.set(4, matchedConst.get(4) + toReplace.size() - 1);
                }
            }

            if(unconst){
                //this is gurenteed to be the full expansion because everything must have been declared beforehand already

                List<Token> replacement = new ArrayList<>(tokenList.subList(matchedConst.get(3)+1, matchedConst.get(4)));
                replacement.addFirst(new Token('(', -1, -1, "(", null));
                replacement.add(new Token(')', -1, -1, ")", null));
                toBeUnConsted.put(tokenList.get(matchedConst.get(2)).getText(), replacement);

                if(ScreenshotFeatures.DEBUG){
                    System.out.print("in second \n\"");
                    for(int j=matchedConst.get(0);j<=matchedConst.get(4);j++){
                        System.out.print(tokenList.get(j).getText());
                    }
                    System.out.println("\"");
                    System.out.println("replaced all of it with empty");
                }

                for(int i=matchedConst.get(0);i<=matchedConst.get(4);i++){
                    tokenList.set(i, new Token(Token.WHITESPACE, -1, -1, "", null));
                }
            }

            searchingIndex = matchedConst.getFirst() + 1;
        }
        for(int i=0;i<tokenList.size();i++){
            Token token = tokenList.get(i);
            if(token.getType()!=Token.IDENTIFIER){
                continue;
            }
            List<Token> toReplace = toBeUnConsted.get(tokenList.get(i).getText());
            if(toReplace!=null){
                tokenList.addAll(i+1, toReplace);
                tokenList.remove(i);
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0;i<tokenList.size();i++){
            stringBuilder.append(tokenList.get(i).getText());
        }
        String result = listener.collectLines() + stringBuilder;
        if(ScreenshotFeatures.DEBUG && toPrint){
            List<String> origSourceLines = Arrays.asList(origSource.split("\n"));
            Patch<String> diff = DiffUtils.diff(origSourceLines, Arrays.asList(result.split("\n")));
            // Generate unified diff output format
            List<String> fullDiff = UnifiedDiffUtils.generateUnifiedDiff(
                "original.glsl",
                "processed.glsl",
                origSourceLines,
                diff,
                100000 // Context lines around changes
            );

            List<String> minDiff = UnifiedDiffUtils.generateUnifiedDiff(
                "original.glsl",
                "processed.glsl",
                origSourceLines,
                diff,
                3 // Context lines around changes
            );
//            String printResult =
//                cir.getReturnValue().replaceAll("(?m)^\\h*$\\n?", "") +
//                    "\n\n\n\n========================================================================================================================================================================\n\n\n\n" +
//                result.replaceAll("(?m)^\\h*$\\n?", "");
            String printResult = String.join("\n",fullDiff);
            try{
                String prefixPath = "./run/shaderdebugout/";
                Files.writeString(Path.of(prefixPath+name + ".txt"), printResult);
                Files.writeString(Path.of(prefixPath+name + "_min.txt"), String.join("\n", minDiff));
            }catch(IOException e){
                throw new RuntimeException(e);
            }
        }
        cir.setReturnValue(result);
    }

    @Unique
    private static <T> List<Integer> matchIndex(List<T> list,int startingIndex,BiPredicate<Integer,T> ignore,List<Predicate<T>> match){
        List<Integer> indexes = new ArrayList<>(Collections.nCopies(match.size(),0));
        int toMatchIndex=0;
        for(int i=startingIndex;i<list.size();i++){
            var item = list.get(i);
            if(match.get(toMatchIndex).test(item)){
                indexes.set(toMatchIndex,i);
                toMatchIndex++;
                if(toMatchIndex>=match.size()){
                    return indexes;
                }
            }else if(!ignore.test(toMatchIndex,item)){
                if(toMatchIndex>0){
                    //try starting search from shifted forward one index from our original origin
                    i = indexes.get(0);
                }
                toMatchIndex=0;
            }
        }
        Collections.fill(indexes, -1);
        return indexes;
    }

}
