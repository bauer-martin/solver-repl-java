package commands;

import static utilities.ParsingUtils.decodedBinaryOptions;
import static utilities.ParsingUtils.encodedBinaryOptionsCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class FindAllOptimalConfigsCommand extends ShellCommand {

  public FindAllOptimalConfigsCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    String[] tokens = argsString.split(" ");
    VariabilityModel vm = context.getVariabilityModel();
    Set<BinaryOption> config = tokens.length < 1 ? Collections.emptySet()
                                                 : decodedBinaryOptions(tokens[1], vm);
    Set<BinaryOption> unwantedOptions = tokens.length < 2 ? Collections.emptySet()
                                                          : decodedBinaryOptions(tokens[2], vm);

    VariantGenerator vg = context.getVariantGenerator();
    Collection<Set<BinaryOption>> optimalConfigs = vg.findAllOptimalConfigs(config,
                                                                            unwantedOptions);
    return encodedBinaryOptionsCollection(optimalConfigs);
  }
}
