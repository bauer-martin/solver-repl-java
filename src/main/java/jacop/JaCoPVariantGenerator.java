package jacop;

import static java.util.Comparator.comparing;

import org.jacop.constraints.And;
import org.jacop.constraints.LinearInt;
import org.jacop.constraints.Not;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.SumBool;
import org.jacop.constraints.XeqC;
import org.jacop.core.BooleanVar;
import org.jacop.core.Domain;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainRandom;
import org.jacop.search.InputOrderSelect;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SimpleSolutionListener;
import org.jacop.search.SolutionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;
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

  private static IntVar constraintSelectedOptions(
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
    IntVar sumVar = new IntVar(store, "selectedOptionsCount", IntDomain.MinInt, IntDomain.MaxInt);
    store.impose(new LinearInt(goals, coefficients, "==", sumVar));
    return sumVar;
  }

  private static boolean performSearch(ConstraintSystemContext context,
                                       SolutionListener<IntVar> solutionListener) {
    Search<IntVar> search = new DepthFirstSearch<>();
    search.setPrintInfo(false);
    search.setAssignSolution(false);
    Store store = context.getStore();
    SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store,
                                                              context.getVariables(),
                                                              new IndomainRandom<>(0));
    solutionListener.recordSolutions(true);
    search.setSolutionListener(solutionListener);
    return search.labeling(store, select);
  }

  private static OptionalInt performOptimizeSearch(
      ConstraintSystemContext context, @Nullable SolutionListener<IntVar> solutionListener,
      IntVar costVar) {
    Search<IntVar> search = new DepthFirstSearch<>();
    search.setPrintInfo(false);
    search.setAssignSolution(false);
    Store store = context.getStore();
    SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store,
                                                              context.getVariables(),
                                                              new IndomainRandom<>(0));
    if (solutionListener != null) {
      solutionListener.recordSolutions(true);
      search.setSolutionListener(solutionListener);
    }
    boolean hasFoundSolution = search.labeling(store, select, costVar);
    return hasFoundSolution ? OptionalInt.of(search.getCostValue()) : OptionalInt.empty();
  }

  @Nullable
  @Override
  public Set<BinaryOption> findOptimalConfig(Set<BinaryOption> config,
                                             Set<BinaryOption> unwantedOptions) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);

    // feature selection
    constraintSelectedOptions(context, config);

    IntVar sumVar = constraintSelectedOptions(
        context, option -> unwantedOptions.contains(option) && !config.contains(option) ? 100 : 1);

    DefaultSolutionListener solutionListener = new DefaultSolutionListener(1);
    OptionalInt optimalCost = performOptimizeSearch(context, solutionListener, sumVar);
    return optimalCost.isPresent() ? toBinaryOptions(solutionListener.getSolutionAsConfig())
                                   : null;
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> findAllOptimalConfigs(Set<BinaryOption> config,
                                                             Set<BinaryOption> unwantedOptions) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);
    Store store = context.getStore();

    // feature selection
    constraintSelectedOptions(context, config);

    IntVar sumVar = constraintSelectedOptions(
        context, option -> unwantedOptions.contains(option) && !config.contains(option) ? 100 : -1);

    OptionalInt optimalCost = performOptimizeSearch(context, null, sumVar);
    assert optimalCost.isPresent();

    store.impose(new XeqC(sumVar, optimalCost.getAsInt()));
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(-1);
    boolean hasFoundSolution = performSearch(context, solutionListener);
    return hasFoundSolution ? toBinaryOptions(solutionListener.getSolutionsAsConfigs())
                            : Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> generateUpToNConfigs(int n) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(n);
    boolean hasFoundSolution = performSearch(context, solutionListener);
    return hasFoundSolution ? toBinaryOptions(solutionListener.getSolutionsAsConfigs())
                            : Collections.emptyList();
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
    IntVar sumVar = constraintSelectedOptions(
        context, option -> config.contains(option) ? -1000 : 1000);

    // find an optimal solution
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(1);
    OptionalInt optimalCost = performOptimizeSearch(context, null, sumVar);
    if (optimalCost.isPresent()) {
      Set<BinaryOption> optimalConfig = toBinaryOptions(solutionListener.getSolutionAsConfig());
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
    DefaultSolutionListener solutionListener = new DefaultSolutionListener(-1);
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

  @Nullable
  @Override
  public Set<BinaryOption> generateConfig(int selectedOptionsCount,
                                          Map<Set<BinaryOption>, Integer> featureWeight,
                                          Collection<Set<BinaryOption>> excludedConfigs) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);
    Store store = context.getStore();
    List<Entry<Set<BinaryOption>, Integer>> featureRanking
        = featureWeight.entrySet()
                       .stream()
                       .sorted(comparing(Entry::getValue))
                       .collect(Collectors.toList());

    // there should be exactly selectedOptionsCount features selected
    BooleanVar[] allVariables = new BooleanVar[context.getVariableCount()];
    int index = 0;
    for (Entry<ConfigurationOption, BooleanVar> entry : context) {
      allVariables[index] = entry.getValue();
      index++;
    }
    IntVar sumVar = new IntVar(store, "sum", IntDomain.MinInt, IntDomain.MaxInt);
    store.impose(new SumBool(allVariables, "==", sumVar));
    store.impose(new XeqC(sumVar, selectedOptionsCount));

    // excluded configurations should not be considered as a solution
    List<BinaryOption> allBinaryOptions = vm.getBinaryOptions();
    for (Set<BinaryOption> excludedConfig : excludedConfigs) {
      PrimitiveConstraint[] ands = new PrimitiveConstraint[allBinaryOptions.size()];
      for (int i = 0; i < allBinaryOptions.size(); i++) {
        BinaryOption option = allBinaryOptions.get(i);
        BooleanVar variable = context.getVariable(option);
        ands[i] = excludedConfig.contains(option) ? new XeqC(variable, 1) : new XeqC(variable, 0);
      }
      store.impose(new Not(new And(ands)));
    }

    // if we have a feature ranking, we can use it to approximate the optimal solution
    Set<BinaryOption> approximateOptimal = getSmallWeightConfig(context, featureRanking);
    if (approximateOptimal == null) {
      DefaultSolutionListener solutionListener = new DefaultSolutionListener(1);
      boolean hasFoundSolution = performSearch(context, solutionListener);
      return hasFoundSolution ? toBinaryOptions(solutionListener.getSolutionAsConfig())
                              : null;
    } else {
      return approximateOptimal;
    }
  }

  @Nullable
  private Set<BinaryOption> getSmallWeightConfig(
      ConstraintSystemContext context,
      Iterable<Entry<Set<BinaryOption>, Integer>> featureRanking) {
    Store store = context.getStore();
    for (Entry<Set<BinaryOption>, Integer> entry : featureRanking) {
      Set<BinaryOption> candidates = entry.getKey();

      // record current state
      int baseLevel = store.level;
      store.setLevel(baseLevel + 1);

      // force features to be selected
      store.impose(new And(candidates.stream()
                                     .map(option -> new XeqC(context.getVariable(option), 1))
                                     .collect(Collectors.toList())));

      // check if satisfiable
      DefaultSolutionListener solutionListener = new DefaultSolutionListener(1);
      boolean hasFoundSolution = performSearch(context, solutionListener);
      Set<BinaryOption> solution = null;
      if (hasFoundSolution) {
        solution = toBinaryOptions(solutionListener.getSolutionAsConfig());
      }

      // reset constraint system
      if (store.level != baseLevel + 1) {
        throw new IllegalStateException("investigation needed");
      }
      store.removeLevel(baseLevel);

      // stop if solution has been found
      if (solution != null) {
        return solution;
      }
    }
    return null;
  }

  @Nonnull
  @SuppressWarnings("TypeMayBeWeakened")
  private Set<BinaryOption> toBinaryOptions(Set<String> selectedOptions) {
    return selectedOptions.stream().map(vm::getBinaryOption).collect(Collectors.toSet());
  }

  @Nonnull
  private Collection<Set<BinaryOption>> toBinaryOptions(Collection<Set<String>> solutions) {
    return solutions.stream().map(this::toBinaryOptions).collect(Collectors.toList());
  }

  private static final class DefaultSolutionListener extends SimpleSolutionListener<IntVar> {

    DefaultSolutionListener(int solutionLimit) {
      recordSolutions(true);
      if (solutionLimit > 0) {
        setSolutionLimit(solutionLimit);
      } else {
        searchAll(true);
      }
    }

    @Nonnull
    private Set<String> getSolutionAsConfig() {
      return getSolutionAsConfig(solutionsNo());
    }

    @Nonnull
    private Set<String> getSolutionAsConfig(int solutionNumber) {
      Domain[] solution = getSolution(solutionNumber);
      return IntStream.range(0, solution.length)
                      .filter(i -> ((IntDomain) solution[i]).value() == 1)
                      .mapToObj(i -> vars[i].id)
                      .collect(Collectors.toSet());
    }

    @Nonnull
    private Collection<Set<String>> getSolutionsAsConfigs() {
      int solutionCount = solutionsNo();
      return IntStream.rangeClosed(1, solutionCount)
                      .mapToObj(this::getSolutionAsConfig)
                      .collect(Collectors.toCollection(() -> new ArrayList<>(solutionCount)));
    }
  }
}
