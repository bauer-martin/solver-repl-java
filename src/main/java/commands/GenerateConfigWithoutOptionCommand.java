package commands;

import java.util.Set;

import javax.annotation.Nonnull;

import option_coding.OptionCoding;
import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
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
    OptionCoding coding = context.getOptionCoding();
    Set<BinaryOption> config = coding.decodeBinaryOptions(tokens[0]);
    if (tokens.length < 2) {
      return error("no option specified");
    }
    BinaryOption optionToRemove = vm.getBinaryOption(tokens[1]);

    VariantGenerator vg = context.getSolverFacade().getVariantGenerator();
    Tuple<Set<BinaryOption>, Set<BinaryOption>> result
        = vg.generateConfigWithoutOption(config, optionToRemove);
    if (result == null) {
      return "none";
    } else {
      Set<BinaryOption> newConfig = result.getFirst();
      Set<BinaryOption> removedOptions = result.getSecond();
      return String.format("%s %s",
                           coding.encodeBinaryOptions(newConfig),
                           coding.encodeBinaryOptions(removedOptions));
    }
  }
}
