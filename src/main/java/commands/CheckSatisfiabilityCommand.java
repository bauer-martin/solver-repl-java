package commands;

import static utilities.ParsingUtils.decodedBinaryOptions;

import java.util.List;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class CheckSatisfiabilityCommand extends ShellCommand {

  public CheckSatisfiabilityCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    String[] tokens = argsString.split(" ");
    if (tokens.length < 1 || tokens[0].isEmpty()) {
      return error("partial or complete not specified");
    }
    boolean isPartialConfiguration;
    switch (tokens[0]) {
      case "complete":
        isPartialConfiguration = false;
        break;
      case "partial":
        isPartialConfiguration = true;
        break;
      default:
        return error("partial or complete not specified");
    }
    if (tokens.length < 2) {
      return error("no configuration specified");
    }
    List<BinaryOption> config = decodedBinaryOptions(tokens[1], context.getVariabilityModel());
    boolean valid = context.getSatisfiabilityChecker().isValid(config, isPartialConfiguration);
    return String.valueOf(valid);
  }
}
