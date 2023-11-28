package kilzm.androidcallgraph.callvisitor;

import kilzm.androidcallgraph.helper.CallStringUtil;
import proguard.analysis.CallVisitor;
import proguard.analysis.datastructure.callgraph.Call;
import proguard.classfile.MethodSignature;

import java.util.HashSet;
import java.util.Set;

public class PrintlnCallsCollector implements CallVisitor {
    public Set<String> callsToPrintln;

    public PrintlnCallsCollector() {
        callsToPrintln = new HashSet<>();
    }

    @Override
    public void visitCall(Call call) {
        MethodSignature tm = call.getTarget();
        if (tm.getClassName().equals("java/io/PrintStream") && tm.method.equals("println")) {
            callsToPrintln.add(CallStringUtil.printCallWithArguments(call));
        }
    }
}
