package choco_solver;

import static choco_solver.ChocoHelper.findAllOptimalSolutions;
import static choco_solver.ChocoHelper.findOptimalSolution;
import static choco_solver.ChocoHelper.findAllSolutions;
import static choco_solver.ChocoHelper.selectFeatures;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;
import spl_conqueror.BucketSession;
import spl_conqueror.VariantGenerator;
import utilities.Tuple;

class ChocoVariantGenerator implements VariantGenerator {

  @Nonnull
  private final ChocoConstraintSystemContext context;

  private int seed;

  ChocoVariantGenerator(ChocoConstraintSystemContext context) {
    this.context = context;
  }

  @Nonnull
  private static IntVar addOptionWeighting(ChocoConstraintSystemContext context,
                                           Function<BinaryOption, Integer> weightingFunction) {
    Model model = context.getModel();
    BoolVar[] goals = new BoolVar[context.getVariableCount()];
    int[] coefficients = new int[context.getVariableCount()];
    int index = 0;
    for (Entry<BinaryOption, Variable> entry : context.binaryOptions()) {
      BinaryOption option = entry.getKey();
      goals[index] = entry.getValue().asBoolVar();
      coefficients[index] = weightingFunction.apply(option);
      index++;
    }
    IntVar sumVar = model.intVar("sumVar", IntVar.MIN_INT_BOUND,
                                 IntVar.MAX_INT_BOUND, true);
    model.scalar(goals, coefficients, "=", sumVar).post();
    return sumVar;
  }

  void setSeed(int seed) {
    this.seed = seed;
  }

  @Nullable
  @Override
  public Set<BinaryOption> findMinimizedConfig(Set<BinaryOption> config,
                                               Set<BinaryOption> unwantedOptions) {
    context.markCheckpoint();

    selectFeatures(context, config);

    // Since we are minimizing, unwanted options which are not part of the original configuration
    // get a large weight. This way, it is unlikely (but not impossible) that they are selected.
    // All other options are assigned 1 as weight, meaning they are not weighted at all.
    IntVar sumVar = addOptionWeighting(
        context,
        option -> unwantedOptions.contains(option) && !config.contains(option) ? 100 : 1);

    // find an optimal solution
    Set<BinaryOption> optimalConfig = findOptimalSolution(context, seed, sumVar);

    // cleanup
    context.resetToLastCheckpoint();
    return optimalConfig;
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> findAllMaximizedConfigs(Set<BinaryOption> config,
                                                               Set<BinaryOption> unwantedOptions) {
    context.markCheckpoint();

    selectFeatures(context, config);

    // Since we are minimizing, unwanted options which are not part of the original configuration
    // get a large weight. This way, it is unlikely (but not impossible) that they are selected.
    // All other options are assigned -1 as weight. Out goal is to maximize the number of
    // selected options, i.e., the more options selected, the better/smaller the cost.
    IntVar sumVar = addOptionWeighting(
        context,
        option -> unwantedOptions.contains(option) && !config.contains(option) ? 100 : -1);

    // find all optimal solutions
    Collection<Set<BinaryOption>> optimalConfigs = findAllOptimalSolutions(context, seed, sumVar);

    // cleanup
    context.resetToLastCheckpoint();
    return optimalConfigs;
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> generateUpToNConfigs(int n) {
    context.markCheckpoint();

    // find solutions
    Collection<Set<BinaryOption>> configs = findAllSolutions(context, seed, n);

    // cleanup
    context.resetToLastCheckpoint();
    return configs;
  }

  @Nullable
  @Override
  public Tuple<Set<BinaryOption>, Set<BinaryOption>> generateConfigWithoutOption(
      Set<BinaryOption> config, BinaryOption optionToRemove) {
    context.markCheckpoint();

    // forbid the selection of this configuration option
    context.getVariable(optionToRemove).asBoolVar().eq(0).post();

    // Since we are minimizing, we use a large negative value for options contained in the original
    // configuration to increase chances that the option gets selected again. A positive value
    // will lead to a small chance that this option gets selected when it is not part of the
    // original configuration.
    IntVar sumVar = addOptionWeighting(context,
                                       option -> config.contains(option) ? -1000 : 1000);

    // find an optimal solution
    Set<BinaryOption> optimalConfig = findOptimalSolution(context, seed, sumVar);
    Tuple<Set<BinaryOption>, Set<BinaryOption>> result;
    if (optimalConfig == null) {
      result = null;
    } else {
      // adding the options that have been removed from the original configuration
      Set<BinaryOption> removedElements
          = config.stream()
                  .filter(option -> !optimalConfig.contains(option))
                  .collect(Collectors.toSet());
      result = new Tuple<>(optimalConfig, removedElements);
    }

    // cleanup
    context.resetToLastCheckpoint();
    return result;
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> generateAllVariants(Set<BinaryOption> optionsToConsider) {
    context.markCheckpoint();

    // find all solutions
    Collection<Set<BinaryOption>> allVariants
        = findAllSolutions(context, seed, -1)
        .stream()
        .peek(config -> config.retainAll(optionsToConsider))
        .filter(binaryOptions -> !binaryOptions.isEmpty())
        .collect(Collectors.toSet());

    // cleanup
    context.resetToLastCheckpoint();
    return allVariants;
  }

  @Nonnull
  @Override
  public BucketSession createBucketSession() {
    return new ChocoBucketSession(context, seed);
  }
}
