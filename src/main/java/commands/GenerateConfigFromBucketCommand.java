package commands;

import static utilities.ParsingUtils.encodedBinaryOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.BucketSession;
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
  private static Map<Set<BinaryOption>, Integer> decodeFeatureWeightMap(String str,
                                                                        VariabilityModel vm) {
    String[] entryTokens = str.split(";");
    Map<Set<BinaryOption>, Integer> map = new HashMap<>(entryTokens.length);
    for (String entryToken : entryTokens) {
      String[] tokens = entryToken.split("=");
      Set<BinaryOption> config = ParsingUtils.decodedBinaryOptions(tokens[0], vm);
      int weight = Integer.parseInt(tokens[1]);
      map.put(config, weight);
    }
    return map;
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
    Map<Set<BinaryOption>, Integer> featureWeight;
    VariabilityModel vm = context.getVariabilityModel();
    featureWeight = tokens.length < 2 ? Collections.emptyMap()
                                      : decodeFeatureWeightMap(tokens[1], vm);
    BucketSession bucketSession = context.getBucketSession();
    if (bucketSession == null) {
      VariantGenerator vg = context.getVariantGenerator();
      bucketSession = vg.createBucketSession();
      context.setBucketSession(bucketSession);
    }
    Set<BinaryOption> config = bucketSession.generateConfig(selectedOptionsCount, featureWeight);
    return config == null ? "none" : encodedBinaryOptions(config);
  }
}
