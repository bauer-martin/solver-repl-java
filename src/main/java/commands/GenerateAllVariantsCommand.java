package commands;

import static utilities.ParsingUtils.decodedBinaryOptions;
import static utilities.ParsingUtils.encodedBinaryOptionsCollection;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import utilities.GlobalContext;
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
    Set<BinaryOption> optionsToConsider = decodedBinaryOptions(tokens[0], vm);

    Collection<Set<BinaryOption>> allVariants = context.getVariantGenerator()
                                                       .generateAllVariants(optionsToConsider);
    return encodedBinaryOptionsCollection(allVariants);
  }
}
