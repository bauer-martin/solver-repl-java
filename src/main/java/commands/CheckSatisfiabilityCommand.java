package commands;

import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.SatisfiabilityChecker;
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
    Set<BinaryOption> config = context.getOptionCoding().decodeBinaryOptions(tokens[1]);
    SatisfiabilityChecker satChecker = context.getSolverFacade().getSatisfiabilityChecker();
    boolean valid = satChecker.isValid(config, isPartialConfiguration);
    return String.valueOf(valid);
  }
}
