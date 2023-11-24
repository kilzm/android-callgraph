package kilzm.androidcallgraph;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import proguard.analysis.CallResolver;
import proguard.analysis.datastructure.callgraph.CallGraph;
import proguard.classfile.ClassPool;
import proguard.classfile.MethodSignature;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.*;
import proguard.util.CallGraphWalker;
import proguard.util.ExtensionMatcher;
import proguard.io.*;

public class App
{
    public static void main(String[] args)
    {
        String apkFilePath = args[0];

        ClassPool classPool = new ClassPool();
        ClassPoolFiller classPoolFiller = new ClassPoolFiller(classPool);
        
        // Create a FileSource for the apk
        DataEntrySource source =
                new FileSource(
                new File(apkFilePath));


        // Read all dex files starting with "classes"
        DataEntryReader jarReader =
                new JarReader(
                new NameFilteredDataEntryReader("classes*.dex",
                new DexClassReader(true, classPoolFiller)));

        DataEntryReader apkReader =
                new FilteredDataEntryReader(
                new DataEntryNameFilter(
                new ExtensionMatcher("apk")), jarReader);

        try {
            source.pumpDataEntries(apkReader);
        } catch (IOException e) {
            e.printStackTrace();
        }


        CallGraph callGraph = new CallGraph();
        CallResolver resolver = new CallResolver.Builder(classPool, new ClassPool(), callGraph,
                call -> {
                    if (call.getTarget().method.equals("startActivity")) {
                        System.out.printf("Call from %s to %s\n", call.caller.signature, call.getTarget());
                    }
                })
                .setClearCallValuesAfterVisit(false)
                .setUseDominatorAnalysis(false)
                .setEvaluateAllCode(true)
                .setIncludeSubClasses(true)
                .setMaxPartialEvaluations(50)
                .setSkipIncompleteCalls(false)
                .setIgnoreExceptions(true)
                .build();

        // build the call graph
        classPool.classesAccept(resolver);


        Set<MethodSignature> targetMethods = new HashSet<>();
        final String targetMethodName = "startActivity";
        final String targetMethodSignature = "(Landroid/content/Intent;)V";
        final String superClassName = "androidx/appcompat/app/AppCompatActivity";

        classPool.classes().forEach(c -> {
            // TODO: understand how extendsOrImplements works
            if (c.getSuperName().equals(superClassName)) {
                targetMethods.add(new MethodSignature(c.getName(), targetMethodName, targetMethodSignature));
            }
        });

        //targetMethods.add(new MethodSignature(ClassUtil.internalClassName("com.kilzm.testapp.MainActivity"), targetMethodName, "(Landroid/content/Intent;)V"));
        //targetMethods.add(new MethodSignature(ClassUtil.internalClassName("com.kilzm.testapp.GitHubLauncherActivity"), targetMethodName, "(Landroid/content/Intent;)V"));

        // find all incoming paths
        targetMethods
                .stream()
                .map(tm -> callGraph.reconstructCallGraph(classPool, tm, CallGraphWalker.MAX_DEPTH_DEFAULT, CallGraphWalker.MAX_WIDTH_DEFAULT))
                .forEach(result -> result.incomingCallLocations
                        .forEach(icl -> {
                            System.out.printf("%s calls %s\n", icl, result.signature);
                        }));
    }
}
