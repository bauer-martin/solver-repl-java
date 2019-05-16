package commands;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariantGenerator;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class GenerateUpToNConfigsCommand extends ShellCommand {

  public GenerateUpToNConfigsCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    int count;
    try {
      count = Integer.parseInt(argsString);
    } catch (NumberFormatException e) {
      return error("invalid number '" + argsString + '\'');
    }
    VariantGenerator vg = context.getSolverFacade().getVariantGenerator();
    Collection<Set<BinaryOption>> configs = vg.generateUpToNConfigs(count);
    return context.getOptionCoding().encodeBinaryOptionsIterable(configs);
  }
}
