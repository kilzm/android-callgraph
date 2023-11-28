package kilzm.androidcallgraph.helper;

import java.util.stream.IntStream;

import proguard.analysis.datastructure.callgraph.Call;

public class CallStringUtil {
    public static String printCallWithArguments(Call call) {
        StringBuilder s = new StringBuilder()
                .append(call.toString())
                .append(" arguments: ");
        IntStream.range(0, call.getArgumentCount()).forEach(i -> s.append(call.getArgument(i)).append(" "));
        return s.toString();
    }
}
