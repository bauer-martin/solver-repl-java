package jacop;

import static jacop.JaCoPSearch.performSearch;

import org.jacop.constraints.And;
import org.jacop.constraints.Not;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.SumBool;
import org.jacop.constraints.XeqC;
import org.jacop.core.BooleanVar;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;
import spl_conqueror.BucketSession;
import spl_conqueror.ConfigurationOption;
import spl_conqueror.VariabilityModel;

public final class JaCoPBucketSession implements BucketSession {

  @Nonnull
  private final VariabilityModel vm;

  @Nonnull
  private final Map<Integer, Collection<Set<BinaryOption>>> buckets = new HashMap<>();

  @Nonnull
  private final JaCoPConstraintSystemContext context;

  @Nullable
  private IntVar sumVar;

  public JaCoPBucketSession(VariabilityModel vm) {
    this.vm = vm;
    context = new JaCoPConstraintSystemContext(this.vm);
  }

  @Nullable
  @Override
  public Set<BinaryOption> generateConfig(int selectedOptionsCount,
                                          List<Set<BinaryOption>> featureRanking) {
    if (sumVar == null) {
      setup();
    }
    Collection<Set<BinaryOption>> excludedConfigs =
        buckets.computeIfAbsent(selectedOptionsCount, n -> new HashSet<>());
    Set<BinaryOption> config = generateConfig(selectedOptionsCount,
                                              featureRanking,
                                              excludedConfigs);
    excludedConfigs.add(config);
    return config;
  }

  private void setup() {
    // there should be exactly selectedOptionsCount features selected
    BooleanVar[] allVariables = new BooleanVar[context.getVariableCount()];
    int index = 0;
    for (Entry<ConfigurationOption, BooleanVar> entry : context) {
      allVariables[index] = entry.getValue();
      index++;
    }
    Store store = context.getStore();
    sumVar = new IntVar(store, "sum", IntDomain.MinInt, IntDomain.MaxInt);
    store.impose(new SumBool(allVariables, "==", sumVar));
  }

  @Nullable
  private Set<BinaryOption> generateConfig(int selectedOptionsCount,
                                           Iterable<Set<BinaryOption>> featureRanking,
                                           Iterable<Set<BinaryOption>> excludedConfigs) {
    Store store = context.getStore();
    // record current state
    int baseLevel = store.level;
    store.setLevel(baseLevel + 1);

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
    Set<BinaryOption> approximateOptimal = getSmallWeightConfig(featureRanking);
    Set<BinaryOption> result;
    if (approximateOptimal == null) {
      DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, 1);
      boolean hasFoundSolution = performSearch(context, solutionListener);
      result = hasFoundSolution ? solutionListener.getSolutionAsConfig()
                                : null;
    } else {
      result = approximateOptimal;
    }

    // reset constraint system
    if (store.level != baseLevel + 1) {
      throw new IllegalStateException("investigation needed");
    }
    store.removeLevel(store.level);
    store.setLevel(store.level - 1);

    return result;
  }

  @Nullable
  private Set<BinaryOption> getSmallWeightConfig(Iterable<Set<BinaryOption>> featureRanking) {
    Store store = context.getStore();
    for (Set<BinaryOption> candidates : featureRanking) {

      // record current state
      int baseLevel = store.level;
      store.setLevel(baseLevel + 1);

      // force features to be selected
      store.impose(new And(candidates.stream()
                                     .map(option -> new XeqC(context.getVariable(option), 1))
                                     .collect(Collectors.toList())));

      // check if satisfiable
      DefaultSolutionListener solutionListener = new DefaultSolutionListener(vm, 1);
      boolean hasFoundSolution = performSearch(context, solutionListener);
      Set<BinaryOption> solution = null;
      if (hasFoundSolution) {
        solution = solutionListener.getSolutionAsConfig();
      }

      // reset constraint system
      if (store.level != baseLevel + 1) {
        throw new IllegalStateException("investigation needed");
      }
      store.removeLevel(store.level);
      store.setLevel(store.level - 1);

      // stop if solution has been found
      if (solution != null) {
        return solution;
      }
    }
    return null;
  }
}
