package utilities;

import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.stream.Stream;

import choco_solver.ChocoSolverFacade;
import commands.CheckSatisfiabilityCommand;
import commands.ClearBucketCacheCommand;
import commands.FindAllMaximizedConfigsCommand;
import commands.FindMinimizedConfigCommand;
import commands.GenerateAllVariantsCommand;
import commands.GenerateConfigFromBucketCommand;
import commands.GenerateConfigWithoutOptionCommand;
import commands.GenerateUpToNConfigsCommand;
import commands.LoadVMCommand;
import commands.SelectOptionCodingCommand;
import commands.SelectSolverCommand;
import commands.SetSolverParametersCommand;
import jacop.JaCoPSolverFacade;

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

  @SuppressWarnings({ "UseOfSystemOutOrSystemErr", "OverlyCoupledMethod" })
  private static void startShell(BufferedReader input) throws IOException {
    Shell shell = new Shell(input, System.out);
    GlobalContext context = new GlobalContext();
    shell.registerCommand(new LoadVMCommand(context), "load-vm");
    SelectSolverCommand selectSolverCommand = new SelectSolverCommand(context);
    selectSolverCommand.registerSolver("choco", ChocoSolverFacade::new);
    selectSolverCommand.registerSolver("jacop", JaCoPSolverFacade::new);
    shell.registerCommand(selectSolverCommand, "select-solver");
    shell.registerCommand(new SetSolverParametersCommand(context), "set-solver-parameters");
    shell.registerCommand(new SelectOptionCodingCommand(context), "select-option-coding");
    shell.registerCommand(new CheckSatisfiabilityCommand(context), "check-sat");
    shell.registerCommand(new FindMinimizedConfigCommand(context), "find-minimized-config");
    shell.registerCommand(new FindAllMaximizedConfigsCommand(context),
                          "find-all-maximized-configs");
    shell.registerCommand(new GenerateUpToNConfigsCommand(context), "generate-up-to");
    shell.registerCommand(new GenerateConfigWithoutOptionCommand(context),
                          "generate-config-without-option");
    shell.registerCommand(new GenerateAllVariantsCommand(context), "generate-all-variants");
    shell.registerCommand(new GenerateConfigFromBucketCommand(context),
                          "generate-config-from-bucket");
    shell.registerCommand(new ClearBucketCacheCommand(context), "clear-bucket-cache");
    shell.execute();
  }
}
