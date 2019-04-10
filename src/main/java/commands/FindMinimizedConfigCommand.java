package commands;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import option_coding.OptionCoding;
import spl_conqueror.BinaryOption;
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
    OptionCoding coding = context.getOptionCoding();
    String optionsString = tokens[0];
    Set<BinaryOption> config = coding.decodeBinaryOptions(optionsString);
    Set<BinaryOption> unwantedOptions = tokens.length < 2 ? Collections.emptySet()
                                                          : coding.decodeBinaryOptions(tokens[1]);

    VariantGenerator vg = context.getSolverFacade().getVariantGenerator();
    Set<BinaryOption> optimalConfig = vg.findMinimizedConfig(config, unwantedOptions);
    return optimalConfig == null ? "none" : coding.encodeBinaryOptions(optimalConfig);
  }
}
