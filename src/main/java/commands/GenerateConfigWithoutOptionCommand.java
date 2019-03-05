package commands;

import static utilities.ParsingUtils.binaryOptionsFromString;

import java.util.List;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import utilities.GlobalContext;
import utilities.ParsingUtils;
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
    List<BinaryOption> config = binaryOptionsFromString(tokens[0], vm);
    if (tokens.length < 2) {
      return error("no option specified");
    }
    BinaryOption optionToRemove = vm.getBinaryOption(tokens[1]);

    Tuple<List<BinaryOption>, List<BinaryOption>> result
        = context.getVariantGenerator().generateConfigWithoutOption(config, optionToRemove);
    if (result == null) {
      return "none";
    } else {
      List<BinaryOption> newConfig = result.getFirst();
      List<BinaryOption> removedOptions = result.getSecond();
      return String.format("%s %s",
                           ParsingUtils.binaryOptionsToString(newConfig),
                           ParsingUtils.binaryOptionsToString(removedOptions));
    }
  }
}
