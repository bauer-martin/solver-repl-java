package commands;

import static utilities.ParsingUtils.binaryOptionsFromString;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import utilities.GlobalContext;
import utilities.ParsingUtils;
import utilities.ShellCommand;

public final class GenerateAllVariantsCommand extends ShellCommand {

  public GenerateAllVariantsCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    String[] tokens = argsString.split(" ");
    if (tokens.length < 1 || tokens[0].length() < 1) {
      return error("no options specified");
    }
    VariabilityModel vm = context.getVariabilityModel();
    List<BinaryOption> optionsToConsider = binaryOptionsFromString(tokens[0], vm);

    Collection<List<BinaryOption>> allVariants = context.getVariantGenerator()
                                                        .generateAllVariants(optionsToConsider);
    return ParsingUtils.binaryConfigsToString(allVariants);
  }
}
