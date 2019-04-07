package commands;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import option_coding.OptionCoding;
import spl_conqueror.BinaryOption;
import spl_conqueror.VariantGenerator;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class GenerateAllVariantsCommand extends ShellCommand {

  public GenerateAllVariantsCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    String[] tokens = argsString.split(" ");
    if (tokens.length < 1 || tokens[0].length() < 1) {
      return error("no options specified");
    }
    OptionCoding coding = context.getOptionCoding();
    Set<BinaryOption> optionsToConsider = coding.decodeBinaryOptions(tokens[0]);

    VariantGenerator vg = context.getVariantGenerator();
    Collection<Set<BinaryOption>> allVariants = vg.generateAllVariants(optionsToConsider);
    return coding.encodeBinaryOptionsIterable(allVariants);
  }
}
