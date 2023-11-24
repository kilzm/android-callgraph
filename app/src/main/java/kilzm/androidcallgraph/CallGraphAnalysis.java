package kilzm.androidcallgraph;

import java.io.IOException;
import java.util.*;

import proguard.analysis.CallResolver;
import proguard.analysis.CallVisitor;
import proguard.analysis.datastructure.callgraph.CallGraph;
import proguard.classfile.ClassPool;
import proguard.classfile.MethodSignature;

public class CallGraphAnalysis
{

    static boolean startActivityCalls = false;
    static boolean printlnCalls = false;
    static boolean logCalls = false;
    static String apkFilePath;

    private static void parseArgs(String[] args) {
        apkFilePath = args[0];
        // very bad argument parsing
        for (String a : Arrays.asList(args).subList(1, args.length)) {
            switch (a) {
                case "-a": startActivityCalls = true; break;
                case "-p": printlnCalls = true; break;
                case "-l": logCalls = true; break;
            }
        }
    }
    public static void main(String[] args)
    {
        parseArgs(args);

        ClassPool classPool;
        try {
            classPool = ApkUtil.generateClassPool(apkFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<CallVisitor> callVisitors = new ArrayList<>();
        if (printlnCalls) {
            callVisitors.add(new PrintlnCallsCollector());
        }
        if (logCalls) {
            callVisitors.add(new LogCallsCollector());
        }

        CallGraph callGraph = new CallGraph();
        // TODO: dont print all null pointer exceptions from init methods
        CallResolver resolver = new CallResolver.Builder(classPool, new ClassPool(), callGraph, callVisitors.toArray(new CallVisitor[0]))
                    .setClearCallValuesAfterVisit(true)
                    .setUseDominatorAnalysis(false)
                    .setEvaluateAllCode(true)
                    .setIncludeSubClasses(true)
                    .setMaxPartialEvaluations(50)
                    .setSkipIncompleteCalls(false)
                    .setIgnoreExceptions(true)
                    .build();

        // build the call graph
        classPool.classesAccept(resolver);

        if (startActivityCalls) {
            System.out.println("\n\n\n===================== All calls to startActivity in the apk =====================\n");
            printCallsToStartActivity(classPool, callGraph);
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

    private static void printCallsToStartActivity(ClassPool classPool, CallGraph callGraph) {
        Set<MethodSignature> targetMethods = new HashSet<>();
        // TODO: there might be a smarter approach to finding these method candidates
        classPool.classes().forEach(c -> {
            if (c.getSuperName().equals("androidx/appcompat/app/AppCompatActivity")) {
                targetMethods.add(new MethodSignature(c.getName(), "startActivity", "(Landroid/content/Intent;)V"));
            }
            // TODO: understand how extendsOrImplements works
//            if (c.getSuperClass() != null) {
//               not reached
//            }
        });

        targetMethods.stream()
                .map(callGraph.incoming::get)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(System.out::println);
    }

//    private static void printParamsToAllCallsOf(String className, String methodName, CallGraph callGraph) {
//        callGraph.incoming.keySet().stream()
//                .filter(m -> m.method.equals(methodName) && m.getClassName().equals(className))
//                .map(callGraph.incoming::get).filter(Objects::nonNull)
//                .forEach(cs -> cs.stream().filter(Objects::nonNull).forEach(c -> {
//                    StringBuilder sb = new StringBuilder()
//                            .append(c.caller.getName())
//                            .append(" -> ")
//                            .append(c.getTarget())
//                            .append(" params: ( ");
//                    IntStream.range(0, c.getArgumentCount())
//                            .forEach(i -> sb.append(c.getArgument(i)).append(" "));
//                    sb.append(")");
//                    System.out.println(sb);
//                }));
//    }
}

