package commands;

import static utilities.ParsingUtils.decodedBinaryOptions;
import static utilities.ParsingUtils.encodedBinaryOptions;

import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import utilities.GlobalContext;
import utilities.ShellCommand;
import utilities.Tuple;

public final class GenerateConfigWithoutOptionCommand extends ShellCommand {

  public GenerateConfigWithoutOptionCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    String[] tokens = argsString.split(" ");
    if (tokens.length < 1 || tokens[0].length() < 1) {
      return error("no configuration specified");
    }
    VariabilityModel vm = context.getVariabilityModel();
    Set<BinaryOption> config = decodedBinaryOptions(tokens[0], vm);
    if (tokens.length < 2) {
      return error("no option specified");
    }
    BinaryOption optionToRemove = vm.getBinaryOption(tokens[1]);

    Tuple<Set<BinaryOption>, Set<BinaryOption>> result
        = context.getVariantGenerator().generateConfigWithoutOption(config, optionToRemove);
    if (result == null) {
      return "none";
    } else {
      Set<BinaryOption> newConfig = result.getFirst();
      Set<BinaryOption> removedOptions = result.getSecond();
      return String.format("%s %s",
                           encodedBinaryOptions(newConfig),
                           encodedBinaryOptions(removedOptions));
    }
  }
}
