package commands;

import static utilities.ParsingUtils.encodedBinaryOptionsCollection;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
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
      return error("invalid number");
    }
    Collection<Set<BinaryOption>> configs = context.getVariantGenerator()
                                                   .generateUpToNConfigs(count);
    return encodedBinaryOptionsCollection(configs);
  }
}
