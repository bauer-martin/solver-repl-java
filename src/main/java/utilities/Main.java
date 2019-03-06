package utilities;

import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import commands.CheckSatisfiabilityCommand;
import commands.FindAllOptimalConfigsCommand;
import commands.FindOptimalConfigCommand;
import commands.GenerateAllVariantsCommand;
import commands.GenerateConfigWithoutOptionCommand;
import commands.GenerateUpToNConfigsCommand;
import commands.LoadVMCommand;
import commands.SelectSolverCommand;

public final class Main {

  private Main() {
  }

  public static void main(String... args) throws IOException {
    runShellUsingStandardInput();
  }

  @SuppressWarnings("unused")
  private static void runShellUsingStandardInput() throws IOException {
    BufferedReader input = new BufferedReader(
        new InputStreamReader(System.in, Charset.forName("UTF-8")));
    startShell(input);
  }

  private static void runShell(String... args) throws IOException {
    String inputCommands = Stream.of(args).collect(joining(System.lineSeparator()));
    BufferedReader input = new BufferedReader(new StringReader(inputCommands));
    startShell(input);
  }

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  private static void startShell(BufferedReader input) throws IOException {
    Shell shell = new Shell(input, System.out);
    GlobalContext context = new GlobalContext();
    shell.registerCommand(new LoadVMCommand(context), "load-vm");
    shell.registerCommand(new SelectSolverCommand(context), "select-solver");
    shell.registerCommand(new CheckSatisfiabilityCommand(context), "check-sat");
    shell.registerCommand(new FindOptimalConfigCommand(context), "find-optimal-config");
    shell.registerCommand(new FindAllOptimalConfigsCommand(context), "find-all-optimal-configs");
    shell.registerCommand(new GenerateUpToNConfigsCommand(context), "generate-up-to");
    shell.registerCommand(new GenerateConfigWithoutOptionCommand(context),
                          "generate-config-without-option");
    shell.registerCommand(new GenerateAllVariantsCommand(context), "generate-all-variants");
    shell.execute();
  }
}
