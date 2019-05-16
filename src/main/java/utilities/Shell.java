package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public final class Shell {

  @Nonnull
  private static final Pattern WHITESPACE_SEPARATOR = Pattern.compile("\\s+");

  @Nonnull
  private final PrintStream out;

  @Nonnull
  private final PrintStream err;

  @Nonnull
  private final BufferedReader in;

  @Nonnull
  private final Map<String, ShellCommand> commands = new HashMap<>();

  public Shell(BufferedReader in, PrintStream out, PrintStream err) {
    this.in = in;
    this.out = out;
    this.err = err;
  }

  public void registerCommand(ShellCommand command, String commandString) {
    commands.put(commandString, command);
  }

  public void execute() throws IOException {
    boolean shouldProcessInput = true;
    do {
      String line = in.readLine();
      if (line == null) { // user closed input via EOF
        break;
      }
      String[] tokens = WHITESPACE_SEPARATOR.split(line.trim(), 2);
      String commandString = tokens[0];
      String argsString = tokens.length > 1 ? tokens[1].trim() : "";
      if (commands.containsKey(commandString)) {
        ShellCommand command = commands.get(commandString);
        String response = command.execute(argsString);
        out.println(response);
      } else if (commandString.equals("exit")) {
        shouldProcessInput = false;
      } else {
        err.printf("terminating due to unknown command '%s'%n", commandString);
        shouldProcessInput = false;
      }
    } while (shouldProcessInput);
  }
}
