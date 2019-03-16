package jacop;

import static jacop.JaCoPSearch.performMinimizingSearch;
import static jacop.JaCoPSearch.performSearch;

import org.jacop.constraints.LinearInt;
import org.jacop.constraints.XeqC;
import org.jacop.core.BooleanVar;
import org.jacop.core.Domain;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;
import spl_conqueror.BucketSession;
import spl_conqueror.ConfigurationOption;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.Tuple;

public final class JaCoPVariantGenerator implements VariantGenerator {

  @Nonnull
  private final VariabilityModel vm;

  public JaCoPVariantGenerator(VariabilityModel vm) {
    this.vm = vm;
  }

  @SuppressWarnings("TypeMayBeWeakened")
  private static void constraintSelectedOptions(ConstraintSystemContext context,
                                                Set<BinaryOption> config) {
    Store store = context.getStore();
    for (BinaryOption option : config) {
      BooleanVar variable = context.getVariable(option);
      store.impose(new XeqC(variable, 1));
    }
  }

  private static IntVar addOptionWeighting(
      ConstraintSystemContext context, Function<BinaryOption, Integer> weightingFunction) {
    BooleanVar[] goals = new BooleanVar[context.getVariableCount()];
    int[] coefficients = new int[context.getVariableCount()];
    int index = 0;
    for (Entry<ConfigurationOption, BooleanVar> entry : context) {
      BinaryOption option = (BinaryOption) entry.getKey();
      goals[index] = entry.getValue();
      coefficients[index] = weightingFunction.apply(option);
      index++;
    }
    Store store = context.getStore();
    IntVar sumVar = new IntVar(store, "sumVar", IntDomain.MinInt, IntDomain.MaxInt);
    store.impose(new LinearInt(goals, coefficients, "==", sumVar));
    return sumVar;
  }

  @Nullable
  @Override
  public Set<BinaryOption> findMinimizedConfig(Set<BinaryOption> config,
                                               Set<BinaryOption> unwantedOptions) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);

    // feature selection
    constraintSelectedOptions(context, config);

    IntVar sumVar = addOptionWeighting(
        context, option -> unwantedOptions.contains(option) && !config.contains(option) ? 100 : 1);

    DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, 1);
    OptionalInt optimalCost = performMinimizingSearch(context, solutionListener, sumVar);
    return optimalCost.isPresent() ? solutionListener.getSolutionAsConfig() : null;
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> findAllMaximizedConfigs(Set<BinaryOption> config,
                                                               Set<BinaryOption> unwantedOptions) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);
    Store store = context.getStore();

    // feature selection
    constraintSelectedOptions(context, config);

    IntVar sumVar = addOptionWeighting(
        context, option -> unwantedOptions.contains(option) && !config.contains(option) ? 100 : -1);

    OptionalInt optimalCost = performMinimizingSearch(context, null, sumVar);
    assert optimalCost.isPresent();

    store.impose(new XeqC(sumVar, optimalCost.getAsInt()));
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, -1);
    boolean hasFoundSolution = performSearch(context, solutionListener);
    return hasFoundSolution ? solutionListener.getSolutionsAsConfigs() : Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> generateUpToNConfigs(int n) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, n);
    boolean hasFoundSolution = performSearch(context, solutionListener);
    return hasFoundSolution ? solutionListener.getSolutionsAsConfigs() : Collections.emptyList();
  }

  @Nullable
  @Override
  public Tuple<Set<BinaryOption>, Set<BinaryOption>> generateConfigWithoutOption(
      Set<BinaryOption> config, BinaryOption optionToRemove) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);
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
    OptionalInt optimalCost = performMinimizingSearch(context, null, sumVar);
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
    ConstraintSystemContext context = new ConstraintSystemContext(vm);
    // find all solutions
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, -1);
    boolean hasFoundSolution = performSearch(context, solutionListener);
    if (hasFoundSolution) {
      Collection<Set<BinaryOption>> allVariants = new HashSet<>();
      int solutionCount = solutionListener.solutionsNo();
      for (int solutionNumber = 1; solutionNumber <= solutionCount; solutionNumber++) {
        Domain[] solution = solutionListener.getSolution(solutionNumber);
        Set<BinaryOption> config = new HashSet<>();
        int i = 0;
        for (Entry<ConfigurationOption, BooleanVar> entry : context) {
          BinaryOption option = (BinaryOption) entry.getKey();
          // ignore all options that should not be considered.
          if (optionsToConsider.contains(option)) {
            if (((IntDomain) solution[i]).value() == 1) {
              config.add(vm.getBinaryOption(solutionListener.vars[i].id));
            }
          }
          i++;
        }
        if (!config.isEmpty()) {
          allVariants.add(config);
        }
      }
      return allVariants;
    } else {
      return Collections.emptyList();
    }
  }

  @Nonnull
  @Override
  public BucketSession createBucketSession() {
    return new JaCoPBucketSession(vm);
  }
}
