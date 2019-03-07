package commands;

import static utilities.ParsingUtils.decodedBinaryOptions;
import static utilities.ParsingUtils.encodedBinaryOptionsCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
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
    if (tokens.length < 1 || tokens[0].length() < 1) {
      return error("optimization goal not specified");
    }
    boolean minimize = tokens[0].equals("minimize");
    VariabilityModel vm = context.getVariabilityModel();
    Set<BinaryOption> config = tokens.length < 2 ? Collections.emptySet()
                                                 : decodedBinaryOptions(tokens[1], vm);
    Set<BinaryOption> unwantedOptions = tokens.length < 3 ? Collections.emptySet()
                                                          : decodedBinaryOptions(tokens[2], vm);

    Collection<Set<BinaryOption>> optimalConfigs =
        context.getVariantGenerator().findAllOptimalConfigs(minimize, config, unwantedOptions);
    return encodedBinaryOptionsCollection(optimalConfigs);
  }
}
