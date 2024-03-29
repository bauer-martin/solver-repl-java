package choco_solver;

import static choco_solver.ChocoHelper.findSolution;
import static choco_solver.ChocoHelper.selectFeatures;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;
import spl_conqueror.BucketSession;

public final class ChocoBucketSession implements BucketSession {

  @Nonnull
  private final Map<Integer, Collection<Set<BinaryOption>>> buckets = new HashMap<>();

  @Nonnull
  private final ChocoConstraintSystemContext context;

  private final int seed;

  ChocoBucketSession(ChocoConstraintSystemContext context, int seed) {
    this.context = context;
    this.seed = seed;
  }

  @Nullable
  @Override
  public Set<BinaryOption> generateConfig(int selectedOptionsCount,
                                          List<Set<BinaryOption>> featureRanking) {
    Collection<Set<BinaryOption>> excludedConfigs =
        buckets.computeIfAbsent(selectedOptionsCount, n -> new HashSet<>());
    Set<BinaryOption> config = generateConfig(selectedOptionsCount,
                                              featureRanking,
                                              excludedConfigs);
    excludedConfigs.add(config);
    return config;
  }

  @Nullable
  private Set<BinaryOption> generateConfig(int selectedOptionsCount,
                                           Iterable<Set<BinaryOption>> featureRanking,
                                           Iterable<Set<BinaryOption>> excludedConfigs) {
    context.markCheckpoint();
    Model model = context.getModel();

    // there should be exactly selectedOptionsCount features selected
    BoolVar[] allVariables = new BoolVar[context.getVariableCount()];
    int index = 0;
    for (Entry<BinaryOption, Variable> entry : context.binaryOptions()) {
      allVariables[index] = entry.getValue().asBoolVar();
      index++;
    }
    model.sum(allVariables, "=", selectedOptionsCount).post();

    // excluded configurations should not be considered as a solution
    List<BinaryOption> allBinaryOptions = context.getVariabilityModel().getBinaryOptions();
    for (Set<BinaryOption> excludedConfig : excludedConfigs) {
      BoolVar[] ands = new BoolVar[allBinaryOptions.size()];
      for (int i = 0; i < allBinaryOptions.size(); i++) {
        BinaryOption option = allBinaryOptions.get(i);
        BoolVar variable = context.getVariable(option).asBoolVar();
        ands[i] = excludedConfig.contains(option) ? variable : variable.not();
      }
      model.not(model.and(ands)).post();
    }

    // if we have a feature ranking, we can use it to approximate the optimal solution
    Set<BinaryOption> approximateOptimal = getSmallWeightConfig(featureRanking);
    Set<BinaryOption> result;
    result = approximateOptimal == null ? findSolution(context, seed) : approximateOptimal;

    // cleanup
    context.resetToLastCheckpoint();
    return result;
  }

  @Nullable
  private Set<BinaryOption> getSmallWeightConfig(Iterable<Set<BinaryOption>> featureRanking) {
    for (Set<BinaryOption> candidates : featureRanking) {
      context.markCheckpoint();

      // force features to be selected
      selectFeatures(context, candidates);

      // check if satisfiable
      Set<BinaryOption> config = findSolution(context, seed);

      // cleanup
      context.resetToLastCheckpoint();

      // stop if solution has been found
      if (config != null) {
        return config;
      }
    }
    return null;
  }
}
