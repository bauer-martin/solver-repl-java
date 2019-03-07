package commands;

import static utilities.ParsingUtils.decodedBinaryOptions;
import static utilities.ParsingUtils.encodedBinaryOptionsCollection;

import java.util.Collections;
import java.util.List;

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
    List<BinaryOption> config = tokens.length < 2 ? Collections.emptyList()
                                                  : decodedBinaryOptions(tokens[1], vm);
    List<BinaryOption> unwantedOptions = tokens.length < 3 ? Collections.emptyList()
                                                           : decodedBinaryOptions(tokens[2], vm);

    List<List<BinaryOption>> optimalConfigs =
        context.getVariantGenerator().findAllOptimalConfigs(minimize, config, unwantedOptions);
    return encodedBinaryOptionsCollection(optimalConfigs);
  }
}
