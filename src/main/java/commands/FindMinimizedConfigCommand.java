package commands;

import static utilities.ParsingUtils.decodedBinaryOptions;
import static utilities.ParsingUtils.encodedBinaryOptions;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class FindMinimizedConfigCommand extends ShellCommand {

  public FindMinimizedConfigCommand(GlobalContext context) {
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
    String optionsString = tokens[0];
    Set<BinaryOption> config = decodedBinaryOptions(optionsString, vm);
    Set<BinaryOption> unwantedOptions = tokens.length < 2 ? Collections.emptySet()
                                                          : decodedBinaryOptions(tokens[1], vm);

    VariantGenerator vg = context.getVariantGenerator();
    Set<BinaryOption> optimalConfig = vg.findMinimizedConfig(config, unwantedOptions);
    return optimalConfig == null ? "none" : encodedBinaryOptions(optimalConfig);
  }
}
