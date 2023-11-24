package kilzm.androidcallgraph;

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
            StringBuilder sb = new StringBuilder()
                    .append(call)
                    .append(" argument: ")
                    .append(call.getArgument(0));
            this.callsToPrintln.add(sb.toString());
        }
    }
}
