package kilzm.androidcallgraph;

import java.io.IOException;
import java.util.*;

import kilzm.androidcallgraph.callvisitor.LogCallsCollector;
import kilzm.androidcallgraph.callvisitor.PrintlnCallsCollector;
import kilzm.androidcallgraph.helper.ClassPools;
import proguard.analysis.CallResolver;
import proguard.analysis.CallVisitor;
import proguard.analysis.datastructure.callgraph.CallGraph;
import proguard.classfile.MethodSignature;

public class CallGraphAnalysis {

    static boolean startActivityCalls = false;
    static boolean printlnCalls = false;
    static boolean logCalls = false;

    static String jdkDirPath;
    static String sdkFilePath;
    static String apkFilePath;

    private static void parseArgs(String[] args) {
        jdkDirPath = args[0];
        sdkFilePath = args[1];
        apkFilePath = args[2];
        // very bad argument parsing
        for (String a : Arrays.asList(args).subList(3, args.length)) {
            switch (a) {
                case "-a":
                    startActivityCalls = true;
                    break;
                case "-p":
                    printlnCalls = true;
                    break;
                case "-l":
                    logCalls = true;
                    break;
            }
        }
    }

    public static void main(String[] args) {
        parseArgs(args);

        ClassPools classPools = new ClassPools();

        try {
            classPools.addFromJdk(jdkDirPath);
            classPools.addFromSdk(sdkFilePath);
            classPools.addFromApk(apkFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        classPools.initialize();

        List<CallVisitor> callVisitors = new ArrayList<>();
        if (printlnCalls) {
            callVisitors.add(new PrintlnCallsCollector());
        }
        if (logCalls) {
            callVisitors.add(new LogCallsCollector());
        }

        CallGraph callGraph = new CallGraph();
        CallResolver resolver = new CallResolver.Builder(classPools.programClassPool, classPools.libraryClassPool, callGraph, callVisitors.toArray(new CallVisitor[0]))
                .setClearCallValuesAfterVisit(true)
                .setUseDominatorAnalysis(false)
                .setEvaluateAllCode(false)
                .setIncludeSubClasses(false)
                .setMaxPartialEvaluations(50)
                .setSkipIncompleteCalls(false)
                .setIgnoreExceptions(true)
                .build();

        // build the call graph
        classPools.programClassPool.classesAccept(resolver);

        if (startActivityCalls) {
            System.out.println("\n\n\n===================== All calls to startActivity in the apk =====================\n");
            MethodSignature tm = new MethodSignature("android/app/Activity", "startActivity", "(Landroid/content/Intent;)V");
            Optional.ofNullable(callGraph.incoming.get(tm)).orElse(Collections.emptySet())
                    .forEach(System.out::println);
        }

        callVisitors.forEach(v -> {
            if (v instanceof PrintlnCallsCollector) {
                System.out.println("\n\n\n======================== All calls to println in the apk ========================\n");
                ((PrintlnCallsCollector) v).callsToPrintln.forEach(System.out::println);
            }

            if (v instanceof LogCallsCollector) {
                System.out.println("\n\n\n============== All calls to methods of android.util.Log in the apk ===============\n");
                ((LogCallsCollector) v).callsToLogMethods.forEach(System.out::println);
            }
        });
    }
}