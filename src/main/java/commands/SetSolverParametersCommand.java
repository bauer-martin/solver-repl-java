package commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import utilities.GlobalContext;
import utilities.ShellCommand;

public final class SetSolverParametersCommand extends ShellCommand {

  public SetSolverParametersCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  private static Map<String, String> decodeParameters(String str) {
    String[] entryTokens = str.split(";");
    return Arrays.stream(entryTokens)
                 .map(entryToken -> entryToken.split("="))
                 .collect(Collectors.toMap(tokens -> tokens[0],
                                           tokens -> tokens[1],
                                           (x, y) -> y,
                                           () -> new HashMap<>(entryTokens.length)));
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    Map<String, String> parameters = decodeParameters(argsString);
    context.getSolverFacade().setParameters(parameters);
    return DEFAULT_SUCCESS_RESPONSE;
  }
}
