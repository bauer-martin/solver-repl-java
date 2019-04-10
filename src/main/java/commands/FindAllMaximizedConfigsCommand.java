package commands;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import option_coding.OptionCoding;
import spl_conqueror.BinaryOption;
import spl_conqueror.VariantGenerator;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class FindAllMaximizedConfigsCommand extends ShellCommand {

  public FindAllMaximizedConfigsCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    String[] tokens = argsString.split(" ");
    OptionCoding coding = context.getOptionCoding();
    Set<BinaryOption> config = tokens.length < 1 || tokens[0].isEmpty()
                               ? Collections.emptySet()
                               : coding.decodeBinaryOptions(tokens[0]);
    Set<BinaryOption> unwantedOptions = tokens.length < 2 ? Collections.emptySet()
                                                          : coding.decodeBinaryOptions(tokens[1]);

    VariantGenerator vg = context.getSolverFacade().getVariantGenerator();
    Collection<Set<BinaryOption>> optimalConfigs = vg.findAllMaximizedConfigs(config,
                                                                              unwantedOptions);
    return coding.encodeBinaryOptionsIterable(optimalConfigs);
  }
}
