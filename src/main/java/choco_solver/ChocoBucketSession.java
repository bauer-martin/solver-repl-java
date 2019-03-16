package choco_solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;
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
import spl_conqueror.ConfigurationOption;

public final class ChocoBucketSession implements BucketSession {

  @Nonnull
  private final Map<Integer, Collection<Set<BinaryOption>>> buckets = new HashMap<>();

  @Nonnull
  private final ConstraintSystemContext context;

  ChocoBucketSession(ConstraintSystemContext context) {
    this.context = context;
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
    // get access to the constraint system
    Model model = context.getModel();

    // there should be exactly selectedOptionsCount features selected
    BoolVar[] allVariables = new BoolVar[context.getVariableCount()];
    int index = 0;
    for (Entry<ConfigurationOption, Variable> entry : context) {
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
    Set<BinaryOption> approximateOptimal = getSmallWeightConfig(model, featureRanking);
    Set<BinaryOption> result;
    if (approximateOptimal == null) {
      Solution solution = model.getSolver().findSolution();
      result = solution == null ? null : SolutionTranslator.toBinaryOptions(solution, context);
    } else {
      result = approximateOptimal;
    }

    // cleanup
    context.resetModel();
    return result;
  }

  @Nullable
  private Set<BinaryOption> getSmallWeightConfig(Model model,
                                                 Iterable<Set<BinaryOption>> featureRanking) {
    for (Set<BinaryOption> candidates : featureRanking) {
      // record current state
      int nbVars = model.getNbVars();
      int nbCstrs = model.getNbCstrs();

      // force features to be selected
      BoolVar[] ands = candidates.stream()
                                 .map(option -> context.getVariable(option).asBoolVar())
                                 .toArray(BoolVar[]::new);
      model.and(ands).post();

      // check if satisfiable
      Solution solution = model.getSolver().findSolution();

      // soft reset constraint system
      Arrays.stream(model.getCstrs(), nbCstrs, model.getNbCstrs()).forEach(model::unpost);
      Arrays.stream(model.getVars(), nbVars, model.getNbVars()).forEach(model::unassociates);
      model.getCachedConstants().clear();
      model.getSolver().reset();

      // stop if solution has been found
      if (solution != null) {
        return SolutionTranslator.toBinaryOptions(solution, context);
      }
    }
    return null;
  }
}
