package choco_solver;

import static java.util.Comparator.comparing;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;
import spl_conqueror.ConfigurationOption;
import spl_conqueror.VariantGenerator;
import utilities.Tuple;

class ChocoVariantGenerator implements VariantGenerator {

  @Nonnull
  private final ConstraintSystemContext context;

  ChocoVariantGenerator(ConstraintSystemContext context) {
    this.context = context;
  }

  @Nullable
  @Override
  public Set<BinaryOption> findOptimalConfig(boolean minimize,
                                             Set<BinaryOption> config,
                                             Set<BinaryOption> unwantedOptions) {
    Model cs = context.getConstraintSystem();

    // feature selection
    for (BinaryOption option : config) {
      Variable variable = context.getVariable(option);
      cs.boolVar(true).imp(variable.asBoolVar()).post();
    }

    // defining goals
    BoolVar[] goals = new BoolVar[context.getVariableCount()];
    int[] coefficients = new int[context.getVariableCount()];
    int index = 0;
    for (Entry<ConfigurationOption, Variable> entry : context) {
      BinaryOption option = (BinaryOption) entry.getKey();
      goals[index] = entry.getValue().asBoolVar();
      coefficients[index] = unwantedOptions.contains(option) && !config.contains(option) ? 100 : 1;
      index++;
    }
    IntVar selectedOptionsCountVar = cs.intVar("selectedOptionsCount", IntVar.MIN_INT_BOUND,
                                               IntVar.MAX_INT_BOUND, true);
    cs.scalar(goals, coefficients, "=", selectedOptionsCountVar).post();

    cs.setObjective(!minimize, selectedOptionsCountVar);

    Solver solver = cs.getSolver();
    Solution solution = solver.findSolution();
    Set<BinaryOption> result = solution == null ? null : toBinaryOptions(solution);

    context.resetConstraintSystem();
    return result;
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> findAllOptimalConfigs(boolean minimize,
                                                              Set<BinaryOption> config,
                                                              Set<BinaryOption> unwantedOptions) {
    Model cs = context.getConstraintSystem();

    // feature selection
    for (BinaryOption option : config) {
      Variable variable = context.getVariable(option);
      cs.boolVar(true).imp(variable.asBoolVar()).post();
    }

    // defining goals
    BoolVar[] goals = new BoolVar[context.getVariableCount()];
    int[] coefficients = new int[context.getVariableCount()];
    int index = 0;
    for (Entry<ConfigurationOption, Variable> entry : context) {
      BinaryOption option = (BinaryOption) entry.getKey();
      goals[index] = entry.getValue().asBoolVar();
      coefficients[index] = unwantedOptions.contains(option) && !config.contains(option) ? 100 : 1;
      index++;
    }
    IntVar selectedOptionsCountVar = cs.intVar("selectedOptionsCount", IntVar.MIN_INT_BOUND,
                                               IntVar.MAX_INT_BOUND, true);
    cs.scalar(goals, coefficients, "=", selectedOptionsCountVar).post();

    cs.setObjective(!minimize, selectedOptionsCountVar);

    Solver solver = cs.getSolver();
    List<Solution> solutions = solver.findAllOptimalSolutions(selectedOptionsCountVar, !minimize);
    Collection<Set<BinaryOption>> result = toBinaryOptions(solutions);

    context.resetConstraintSystem();
    return result;
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> generateUpToNConfigs(int n) {
    Model cs = context.getConstraintSystem();
    Solver solver = cs.getSolver();
    if (n > 0) {
      solver.limitSolution(n);
    }
    List<Solution> solutions = solver.findAllSolutions();
    return toBinaryOptions(solutions);
  }

  @Nullable
  @Override
  public Tuple<Set<BinaryOption>, Set<BinaryOption>> generateConfigWithoutOption(
      Set<BinaryOption> config, BinaryOption optionToRemove) {
    Model cs = context.getConstraintSystem();

    // forbid the selection of this configuration option
    cs.boolVar(true).imp(context.getVariable(optionToRemove).asBoolVar().not()).post();

    // defining goals
    BoolVar[] goals = new BoolVar[context.getVariableCount()];
    int[] coefficients = new int[context.getVariableCount()];
    int index = 0;
    for (Entry<ConfigurationOption, Variable> entry : context) {
      BinaryOption option = (BinaryOption) entry.getKey();
      goals[index] = entry.getValue().asBoolVar();
      // we use a large negative value for options contained in the original configuration to
      // increase chances that the option gets selected again
      // a positive value will lead to a small chance that this option gets selected when it is
      // not part of the original configuration
      coefficients[index] = config.contains(option) ? -1000 : 1000;
      index++;
    }
    IntVar selectedOptionsCountVar = cs.intVar("selectedOptionsCount", IntVar.MIN_INT_BOUND,
                                               IntVar.MAX_INT_BOUND, true);
    cs.scalar(goals, coefficients, "=", selectedOptionsCountVar).post();

    cs.setObjective(false, selectedOptionsCountVar);

    Solver solver = cs.getSolver();
    Solution solution = solver.findOptimalSolution(selectedOptionsCountVar, false);
    Tuple<Set<BinaryOption>, Set<BinaryOption>> result;
    if (solution == null) {
      result = null;
    } else {
      Set<BinaryOption> optimalConfig = toBinaryOptions(solution);
      // adding the options that have been removed from the original configuration
      Set<BinaryOption> removedElements
          = config.stream()
                  .filter(option -> !optimalConfig.contains(option))
                  .collect(Collectors.toSet());
      result = new Tuple<>(optimalConfig, removedElements);
    }

    context.resetConstraintSystem();
    return result;
  }

  @Nonnull
  @Override
  public Collection<Set<BinaryOption>> generateAllVariants(Set<BinaryOption> optionsToConsider) {
    Model cs = context.getConstraintSystem();
    Solver solver = cs.getSolver();
    List<Solution> solutions = solver.findAllSolutions();
    Collection<Set<BinaryOption>> allVariants = new HashSet<>();
    for (Solution solution : solutions) {
      Set<BinaryOption> config = new HashSet<>();
      for (Entry<ConfigurationOption, Variable> entry : context) {
        BinaryOption option = (BinaryOption) entry.getKey();
        // ignore all options that should not be considered.
        if (optionsToConsider.contains(option)) {
          int value = solution.getIntVal(entry.getValue().asBoolVar());
          if (value == 1) {
            config.add(option);
          }
        }
      }
      if (!config.isEmpty()) {
        allVariants.add(config);
      }
    }
    return allVariants;
  }

  @Nullable
  @Override
  public Set<BinaryOption> generateConfig(int numberSelectedFeatures,
                                           Map<Set<BinaryOption>, Integer> featureWeight,
                                           Collection<Set<BinaryOption>> excludedConfigs) {
    Model cs = context.getConstraintSystem();
    List<Entry<Set<BinaryOption>, Integer>> featureRanking
        = featureWeight.entrySet()
                       .stream()
                       .sorted(comparing(Entry::getValue))
                       .collect(Collectors.toList());

    // the first goal of this method is, to have an exact number of features selected
    BoolVar[] allVariables = new BoolVar[context.getVariableCount()];
    int index = 0;
    for (Entry<ConfigurationOption, Variable> entry : context) {
      allVariables[index] = entry.getValue().asBoolVar();
      index++;
    }
    cs.sum(allVariables, "=", numberSelectedFeatures).post();

    // add the excluded configurations as constraints
    List<BinaryOption> allBinaryOptions = context.getVariabilityModel().getBinaryOptions();
    for (Set<BinaryOption> excludedConfig : excludedConfigs) {
      BoolVar[] ands = new BoolVar[allBinaryOptions.size()];
      for (int i = 0; i < allBinaryOptions.size(); i++) {
        BinaryOption option = allBinaryOptions.get(i);
        BoolVar variable = context.getVariable(option).asBoolVar();
        ands[i] = excludedConfig.contains(option) ? variable : variable.not();
      }
      cs.not(cs.and(ands)).post();
    }

    Set<BinaryOption> approximateOptimal = getSmallWeightConfig(cs, featureRanking);
    Set<BinaryOption> result;
    if (approximateOptimal == null) {
      Solution solution = cs.getSolver().findSolution();
      result = solution == null ? null : toBinaryOptions(solution);
    } else {
      result = approximateOptimal;
    }

    context.resetConstraintSystem();
    return result;
  }

  @Nullable
  private Set<BinaryOption> getSmallWeightConfig(
      Model cs, Iterable<Entry<Set<BinaryOption>, Integer>> featureRanking) {
    for (Entry<Set<BinaryOption>, Integer> entry : featureRanking) {
      Set<BinaryOption> candidates = entry.getKey();

      // record current state
      int nbVars = cs.getNbVars();
      int nbCstrs = cs.getNbCstrs();

      // force features to be selected
      BoolVar[] ands = candidates.stream()
                                 .map(option -> context.getVariable(option).asBoolVar())
                                 .toArray(BoolVar[]::new);
      cs.and(ands).post();

      // check if satisfiable
      Solution solution = cs.getSolver().findSolution();

      // soft reset constraint system
      Arrays.stream(cs.getCstrs(), nbCstrs, cs.getNbCstrs()).forEach(cs::unpost);
      Arrays.stream(cs.getVars(), nbVars, cs.getNbVars()).forEach(cs::unassociates);
      cs.getCachedConstants().clear();
      cs.getSolver().reset();

      // stop if solution has been found
      if (solution != null) {
        return toBinaryOptions(solution);
      }
    }
    return null;
  }

  @Nonnull
  private Set<BinaryOption> toBinaryOptions(Solution solution) {
    Set<BinaryOption> config = new HashSet<>(context.getVariableCount());
    for (Entry<ConfigurationOption, Variable> entry : context) {
      int value = solution.getIntVal(entry.getValue().asBoolVar());
      if (value == 1) {
        config.add((BinaryOption) entry.getKey());
      }
    }
    return config;
  }

  @Nonnull
  private Collection<Set<BinaryOption>> toBinaryOptions(Collection<Solution> solutions) {
    return solutions.stream()
                    .map(this::toBinaryOptions)
                    .collect(Collectors.toCollection(() -> new ArrayList<>(solutions.size())));
  }
}
