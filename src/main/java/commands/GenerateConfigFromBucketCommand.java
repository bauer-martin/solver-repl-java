package commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.GlobalContext;
import utilities.ParsingUtils;
import utilities.ShellCommand;

public final class GenerateConfigFromBucketCommand extends ShellCommand {

  public GenerateConfigFromBucketCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    String[] tokens = argsString.split(" ");
    if (tokens.length < 1 || tokens[0].length() < 1) {
      return error("no number specified specified");
    }
    int selectedOptionsCount;
    try {
      selectedOptionsCount = Integer.parseInt(tokens[0]);
    } catch (NumberFormatException e) {
      return error("invalid number");
    }
    Map<List<BinaryOption>, Integer> featureWeight;
    VariabilityModel vm = context.getVariabilityModel();
    featureWeight = tokens.length < 2 ? Collections.emptyMap()
                                      : ParsingUtils.featureWeightFromString(tokens[1], vm);
    Collection<List<BinaryOption>> excludedConfigs = context.buckets.computeIfAbsent(
        selectedOptionsCount, n -> new ArrayList<>());
    VariantGenerator variantGenerator = context.getVariantGenerator();
    List<BinaryOption> config = variantGenerator.generateConfig(selectedOptionsCount,
                                                                featureWeight,
                                                                excludedConfigs);
    if (config == null) {
      return "none";
    } else {
      excludedConfigs.add(config);
      return ParsingUtils.binaryOptionsToString(config);
    }
  }
}
