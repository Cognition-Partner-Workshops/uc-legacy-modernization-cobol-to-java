package carddemo;

import java.io.PrintStream;
import java.util.Map;

/** Exposes {@link Main#run} to tests in other packages. */
public final class MainTestAccess {
    private MainTestAccess() {
    }

    public static int run(String[] args, Map<String, String> env, PrintStream stdout, PrintStream stderr) {
        return Main.run(args, env, stdout, stderr);
    }
}
