package commands;

import static utilities.ParsingUtils.encodedBinaryOptionsCollection;

import java.util.List;

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
    List<List<BinaryOption>> configs = context.getVariantGenerator().generateUpToNConfigs(count);
    return encodedBinaryOptionsCollection(configs);
  }
}
