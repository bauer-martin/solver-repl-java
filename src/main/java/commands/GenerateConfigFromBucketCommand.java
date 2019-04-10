package commands;

import static java.util.Comparator.comparing;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import option_coding.OptionCoding;
import spl_conqueror.BinaryOption;
import spl_conqueror.BucketSession;
import spl_conqueror.VariantGenerator;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class GenerateConfigFromBucketCommand extends ShellCommand {

  public GenerateConfigFromBucketCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  private static Map<Set<BinaryOption>, Integer> decodeFeatureWeightMap(String str,
                                                                        OptionCoding coding) {
    String[] entryTokens = str.split(";");
    Map<Set<BinaryOption>, Integer> map = new HashMap<>(entryTokens.length);
    for (String entryToken : entryTokens) {
      String[] tokens = entryToken.split("=");
      Set<BinaryOption> config = coding.decodeBinaryOptions(tokens[0]);
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
      return error("no number specified");
    }
    int selectedOptionsCount;
    try {
      selectedOptionsCount = Integer.parseInt(tokens[0]);
    } catch (NumberFormatException e) {
      return error("invalid number");
    }
    Map<Set<BinaryOption>, Integer> featureWeight;
    OptionCoding coding = context.getOptionCoding();
    featureWeight = tokens.length < 2 ? Collections.emptyMap()
                                      : decodeFeatureWeightMap(tokens[1], coding);
    BucketSession bucketSession = context.getBucketSession();
    if (bucketSession == null) {
      VariantGenerator vg = context.getSolverFacade().getVariantGenerator();
      bucketSession = vg.createBucketSession();
      context.setBucketSession(bucketSession);
    }
    List<Set<BinaryOption>> featureRanking
        = featureWeight.entrySet()
                       .stream()
                       .sorted(comparing(Entry::getValue))
                       .map(Entry::getKey)
                       .collect(Collectors.toList());
    Set<BinaryOption> config = bucketSession.generateConfig(selectedOptionsCount, featureRanking);
    return config == null ? "none" : coding.encodeBinaryOptions(config);
  }
}
