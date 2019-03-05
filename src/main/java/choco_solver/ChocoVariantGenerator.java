package choco_solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
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
  public List<BinaryOption> findOptimalConfig(boolean minimize,
                                              Collection<BinaryOption> config,
                                              Collection<BinaryOption> unwantedOptions) {
    Model cs = context.getConstraintSystem();
    Collection<Constraint> addedConstraints = new ArrayList<>();

    // feature selection
    for (BinaryOption option : config) {
      Variable variable = context.getVariable(option);
      Constraint constraint = cs.boolVar(true).imp(variable.asBoolVar()).decompose();
      addedConstraints.add(constraint);
      constraint.post();
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
    Constraint constraint = cs.scalar(goals, coefficients, "=", selectedOptionsCountVar);
    addedConstraints.add(constraint);
    constraint.post();

    cs.setObjective(!minimize, selectedOptionsCountVar);

    Solver solver = cs.getSolver();
    Solution solution = solver.findSolution();
    List<BinaryOption> result = solution == null ? null : toBinaryOptions(solution);

    // reset constraint system
    addedConstraints.forEach(cs::unpost);
    solver.reset();

    return result;
  }

  @Nonnull
  @Override
  public List<List<BinaryOption>> findAllOptimalConfigs(boolean minimize,
                                                        Collection<BinaryOption> config,
                                                        Collection<BinaryOption> unwantedOptions) {
    Model cs = context.getConstraintSystem();
    Collection<Constraint> addedConstraints = new ArrayList<>();

    // feature selection
    for (BinaryOption option : config) {
      Variable variable = context.getVariable(option);
      Constraint constraint = cs.boolVar(true).imp(variable.asBoolVar()).decompose();
      addedConstraints.add(constraint);
      constraint.post();
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
    Constraint constraint = cs.scalar(goals, coefficients, "=", selectedOptionsCountVar);
    addedConstraints.add(constraint);
    constraint.post();

    cs.setObjective(!minimize, selectedOptionsCountVar);

    Solver solver = cs.getSolver();
    List<Solution> solutions = solver.findAllOptimalSolutions(selectedOptionsCountVar, !minimize);
    List<List<BinaryOption>> result = toBinaryOptions(solutions);

    // reset constraint system
    addedConstraints.forEach(cs::unpost);
    solver.reset();

    return result;
  }

  @Nonnull
  @Override
  public List<List<BinaryOption>> generateUpToNConfigs(int n) {
    Model cs = context.getConstraintSystem();
    Solver solver = cs.getSolver();
    if (n > 0) {
      solver.limitSolution(n);
    }
    List<Solution> solutions = solver.findAllSolutions();
    return toBinaryOptions(solutions);
  }

  @Nonnull
  private List<BinaryOption> toBinaryOptions(Solution solution) {
    List<BinaryOption> config = new ArrayList<>(context.getVariableCount());
    for (Entry<ConfigurationOption, Variable> entry : context) {
      int value = solution.getIntVal(entry.getValue().asBoolVar());
      if (value == 1) {
        config.add((BinaryOption) entry.getKey());
      }
    }
    return config;
  }

  @Nonnull
  private List<List<BinaryOption>> toBinaryOptions(Collection<Solution> solutions) {
    return solutions.stream()
                    .map(this::toBinaryOptions)
                    .collect(Collectors.toCollection(() -> new ArrayList<>(solutions.size())));
  }

  @Nullable
  @Override
  public Tuple<List<BinaryOption>, List<BinaryOption>> generateConfigWithoutOption(
      Collection<BinaryOption> config, BinaryOption optionToRemove) {
    Model cs = context.getConstraintSystem();
    Collection<Constraint> addedConstraints = new ArrayList<>();

    // forbid the selection of this configuration option
    Constraint constraint = cs.boolVar(true)
                              .imp(context.getVariable(optionToRemove).asBoolVar().not())
                              .decompose();
    addedConstraints.add(constraint);
    constraint.post();

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
    Constraint constraint1 = cs.scalar(goals, coefficients, "=", selectedOptionsCountVar);
    addedConstraints.add(constraint1);
    constraint1.post();

    cs.setObjective(false, selectedOptionsCountVar);

    Solver solver = cs.getSolver();
    Solution solution = solver.findOptimalSolution(selectedOptionsCountVar, false);
    Tuple<List<BinaryOption>, List<BinaryOption>> result;
    if (solution == null) {
      result = null;
    } else {
      List<BinaryOption> optimalConfig = toBinaryOptions(solution);
      // adding the options that have been removed from the original configuration
      List<BinaryOption> removedElements
          = config.stream()
                  .filter(option -> !optimalConfig.contains(option))
                  .collect(Collectors.toList());
      result = new Tuple<>(optimalConfig, removedElements);
    }

    // reset constraint system
    addedConstraints.forEach(cs::unpost);
    solver.reset();

    return result;
  }
}
