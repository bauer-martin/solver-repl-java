package jacop;

import static jacop.JaCoPHelper.performMinimizingSearch;
import static jacop.JaCoPHelper.performSearch;
import static jacop.JaCoPHelper.selectFeatures;

import org.jacop.constraints.LinearInt;
import org.jacop.constraints.XeqC;
import org.jacop.core.BooleanVar;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;
import spl_conqueror.BucketSession;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.Tuple;

public final class JaCoPVariantGenerator implements VariantGenerator {

  @Nonnull
  private final VariabilityModel vm;

  private int seed = 1;

  public JaCoPVariantGenerator(VariabilityModel vm) {
    this.vm = vm;
  }

  private static IntVar addOptionWeighting(
      JaCoPConstraintSystemContext context, Function<BinaryOption, Integer> weightingFunction) {
    BooleanVar[] goals = new BooleanVar[context.getVariableCount()];
    int[] coefficients = new int[context.getVariableCount()];
    int index = 0;
    for (Entry<BinaryOption, BooleanVar> entry : context.binaryOptions()) {
      BinaryOption option = entry.getKey();
      goals[index] = entry.getValue();
      coefficients[index] = weightingFunction.apply(option);
      index++;
    }
    Store store = context.getStore();
    IntVar sumVar = new IntVar(store, "sumVar", IntDomain.MinInt, IntDomain.MaxInt);
    store.impose(new LinearInt(goals, coefficients, "==", sumVar));
    return sumVar;
  }

  void setSeed(int seed) {
    this.seed = seed;
  }

  @Nullable
  @Override
  public Set<BinaryOption> findMinimizedConfig(Set<BinaryOption> config,
                                               Set<BinaryOption> unwantedOptions) {
    JaCoPConstraintSystemContext context = new JaCoPConstraintSystemContext(vm);

    selectFeatures(context, config);
    IntVar sumVar = addOptionWeighting(
        context, option -> unwantedOptions.contains(option) && !config.contains(option) ? 100 : 1);

    DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, 1);
    OptionalInt optimalCost = performMinimizingSearch(context, seed, solutionListener, sumVar);
    return optimalCost.isPresent() ? solutionListener.getSolutionAsConfig() : null;
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> findAllMaximizedConfigs(Set<BinaryOption> config,
                                                               Set<BinaryOption> unwantedOptions) {
    JaCoPConstraintSystemContext context = new JaCoPConstraintSystemContext(vm);
    Store store = context.getStore();

    selectFeatures(context, config);
    IntVar sumVar = addOptionWeighting(
        context, option -> unwantedOptions.contains(option) && !config.contains(option) ? 100 : -1);

    OptionalInt optimalCost = performMinimizingSearch(context, seed, null, sumVar);
    assert optimalCost.isPresent();

    store.impose(new XeqC(sumVar, optimalCost.getAsInt()));
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, -1);
    boolean hasFoundSolution = performSearch(context, seed, solutionListener);
    return hasFoundSolution ? solutionListener.getSolutionsAsConfigs() : Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> generateUpToNConfigs(int n) {
    JaCoPConstraintSystemContext context = new JaCoPConstraintSystemContext(vm);
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, n);
    boolean hasFoundSolution = performSearch(context, seed, solutionListener);
    return hasFoundSolution ? solutionListener.getSolutionsAsConfigs() : Collections.emptyList();
  }

  @Nullable
  @Override
  public Tuple<Set<BinaryOption>, Set<BinaryOption>> generateConfigWithoutOption(
      Set<BinaryOption> config, BinaryOption optionToRemove) {
    JaCoPConstraintSystemContext context = new JaCoPConstraintSystemContext(vm);
    Store store = context.getStore();

    // forbid the selection of this configuration option
    store.impose(new XeqC(context.getVariable(optionToRemove), 0));

    // Since we are minimizing, we use a large negative value for options contained in the original
    // configuration to increase chances that the option gets selected again. A positive value
    // will lead to a small chance that this option gets selected when it is not part of the
    // original configuration.
    IntVar sumVar = addOptionWeighting(context, option -> config.contains(option) ? -1000 : 1000);

    // find an optimal solution
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, 1);
    OptionalInt optimalCost = performMinimizingSearch(context, seed, solutionListener, sumVar);
    if (optimalCost.isPresent()) {
      Set<BinaryOption> optimalConfig = solutionListener.getSolutionAsConfig();
      // adding the options that have been removed from the original configuration
      Set<BinaryOption> removedElements
          = config.stream()
                  .filter(option -> !optimalConfig.contains(option))
                  .collect(Collectors.toSet());
      return new Tuple<>(optimalConfig, removedElements);
    } else {
      return null;
    }
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> generateAllVariants(Set<BinaryOption> optionsToConsider) {
    JaCoPConstraintSystemContext context = new JaCoPConstraintSystemContext(vm);
    // find all solutions
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, -1);
    boolean hasFoundSolution = performSearch(context, seed, solutionListener);
    if (hasFoundSolution) {
      Collection<Set<BinaryOption>> allVariants = solutionListener.getSolutionsAsConfigs();
      return allVariants.stream()
                        .peek(solution -> solution.retainAll(optionsToConsider))
                        .filter(binaryOptions -> !binaryOptions.isEmpty())
                        .collect(Collectors.toSet());
    } else {
      return Collections.emptyList();
    }
  }

  @Nonnull
  @Override
  public BucketSession createBucketSession() {
    return new JaCoPBucketSession(vm, seed);
  }
}
