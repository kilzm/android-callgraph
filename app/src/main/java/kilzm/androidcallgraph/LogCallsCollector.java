package kilzm.androidcallgraph;

import proguard.analysis.CallVisitor;
import proguard.analysis.datastructure.callgraph.Call;
import proguard.classfile.MethodSignature;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class LogCallsCollector implements CallVisitor {
    public Set<String> callsToLogMethods;

    public LogCallsCollector() {
        this.callsToLogMethods = new HashSet<>();
    };

    @Override
    public void visitCall(Call call) {
        MethodSignature tm = call.getTarget();
        if (tm.getClassName().equals("android/util/Log")) {
            StringBuilder sb = new StringBuilder()
                    .append(call)
                    .append(" arguments ");
            IntStream.range(0, call.getArgumentCount()).forEach(i -> sb.append(call.getArgument(i)).append(" "));
            this.callsToLogMethods.add(sb.toString());
        }
    }
}
