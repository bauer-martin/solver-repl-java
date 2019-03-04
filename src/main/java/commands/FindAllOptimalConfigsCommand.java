package commands;

import static utilities.ParsingUtils.binaryOptionsFromString;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import utilities.GlobalContext;
import utilities.ParsingUtils;
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
    if (tokens.length < 2) {
      return error("no configuration specified");
    }
    VariabilityModel vm = context.getVariabilityModel();
    String optionsString = tokens[1];
    List<BinaryOption> config = binaryOptionsFromString(optionsString, vm);
    List<BinaryOption> unwantedOptions;
    unwantedOptions = tokens.length < 3 ? Collections.emptyList()
                                        : binaryOptionsFromString(tokens[2], vm);

    List<List<BinaryOption>> optimalConfigs =
        context.getVariantGenerator().findAllOptimalConfigs(minimize, config, unwantedOptions);
    return ParsingUtils.binaryConfigsToString(optimalConfigs);
  }
}
