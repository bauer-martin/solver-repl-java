package choco_solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;
import spl_conqueror.ConfigurationOption;
import spl_conqueror.VariantGenerator;

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
    List<BinaryOption> result;
    if (solver.solve()) {
      List<BinaryOption> optimalConfig = new ArrayList<>();
      for (Entry<ConfigurationOption, Variable> entry : context) {
        ESat value = entry.getValue().asBoolVar().getBooleanValue();
        if (value == ESat.TRUE) {
          optimalConfig.add((BinaryOption) entry.getKey());
        }
      }
      result = optimalConfig;
    } else {
      result = null;
    }

    // reset constraint system
    addedConstraints.forEach(cs::unpost);
    solver.reset();

    return result;
  }
}
