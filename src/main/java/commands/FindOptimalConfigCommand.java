package commands;

import static utilities.ParsingUtils.encodedBinaryOptions;
import static utilities.ParsingUtils.decodedBinaryOptions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class FindOptimalConfigCommand extends ShellCommand {

  public FindOptimalConfigCommand(GlobalContext context) {
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
    Set<BinaryOption> config = decodedBinaryOptions(optionsString, vm);
    Set<BinaryOption> unwantedOptions = tokens.length < 3 ? Collections.emptySet()
                                                           : decodedBinaryOptions(tokens[2], vm);

    VariantGenerator vg = context.getVariantGenerator();
    Set<BinaryOption> optimalConfig = vg.findOptimalConfig(minimize, config, unwantedOptions);
    return optimalConfig == null ? "none" : encodedBinaryOptions(optimalConfig);
  }
}
