package io.github.kidofcubes.screenshotfeatures.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.kidofcubes.screenshotfeatures.config.Configs;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Mixin(value = ShaderPack.class, remap = false)
public class ShaderPackMixin {
//    @Inject(method="lambda$new$8", at=@At(value="INVOKE", target="Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"))
    @ModifyArgs(method="lambda$new$8", at=@At(value="INVOKE", target="Lnet/irisshaders/iris/shaderpack/preprocessor/JcppProcessor;glslPreprocessSource(Ljava/lang/String;Ljava/lang/Iterable;)Ljava/lang/String;"))
//    private static void injectUniforms(CallbackInfoReturnable<String> cir,@Local(name="source") LocalRef<String> source, @Local(name="finalEnvironmentDefines1") LocalRef<Iterable<StringPair>> finalEnvironmentDefines1){
    private static void injectUniforms(Args args){
//        source doesn't have #defines already now
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
        args.set(1,filtered);


        String source = args.get(0);

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
    private static void unConst(CallbackInfoReturnable<String> cir){

        List<String> overridedUniforms = new ArrayList<>();
        for(var entry: Configs.CustomUniforms.CUSTOM_UNIFORMS.getEntries()){
            if(entry.override){
                overridedUniforms.add(entry.getName());
            }
        }

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
        pp.addInput(new StringLexerSource(cir.getReturnValue(), true));
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

        List<Predicate<Token>> defineMatch = List.of(
            (x -> x.getType()==Token.HASH), // const
            (x -> x.getText().equals("define")), // const
            (x -> x.getType()==Token.IDENTIFIER),
            (x -> x.getType()==Token.NUMBER),
            (x -> x.getType()==Token.CPPCOMMENT)
        );
        int searchingIndex = 0;
        while(true){
            List<Integer> matchedConst = matchIndex(tokenList, searchingIndex, x -> x.getType() == Token.WHITESPACE||x.getText().equals("-"), constMatch);
            if(matchedConst.getFirst()==-1){
                break;
            }

            boolean unconst = false;
            for(int i=matchedConst.get(3)+1;i<matchedConst.get(4);i++){
                if(overridedUniforms.contains(tokenList.get(i).getText())){
                    unconst = true;
                    break;
                }
            }

            if(unconst){
                //todo remove the const definition, put it in parentheses, and replace usages of it everywhere else
//                tokenList.set(matchedConst.get(0), new Token(Token.WHITESPACE, -1, -1, "uniform", null));

            }

            searchingIndex = matchedConst.getFirst() + 1;
        }


//        while(true){
//            List<Integer> matchedDefine = matchIndex(tokenList,searchingIndex,x -> x.getType()==Token.WHITESPACE||x.getText().equals("-"),constMatch);
//            if(matchedDefine.getFirst()==-1){
//                break;
//            }
//        }


//
//        List<Integer> matchIndices = IntStream.rangeClosed(0, lazyIds.size() - targetSize)
//            .filter(i -> lazyIds.subList(i, i + targetSize).equals(constMatch))
//            .boxed()
//            .toList();
//
//        for(int i=0;i<tokenList.size();i++){
//            if(tokenList.get(i).getType()!=Token.NL && tokenList.get(i).getType()!=Token.WHITESPACE){
//                System.out.println("token of type "+tokenList.get(i).getType()+" with string type " + Token.getTokenName(tokenList.get(i).getType()) + " text "+tokenList.get(i).getText());
//            }else{
////                System.out.println("nl");
//            }
//        }

    }



    @Unique
    private static <T> List<Integer> matchIndex(List<T> list,int startingIndex,Predicate<T> ignore,List<Predicate<T>> match){
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
            }else if(!ignore.test(item)){
                if(toMatchIndex>0){
                    i = indexes.get(toMatchIndex-1);
                }
                toMatchIndex=0;
            }
        }
        Collections.fill(indexes, -1);
        return indexes;
    }

}
