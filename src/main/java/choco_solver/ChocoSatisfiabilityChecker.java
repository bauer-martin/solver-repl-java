package choco_solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.ConfigurationOption;
import spl_conqueror.SatisfiabilityChecker;

final class ChocoSatisfiabilityChecker implements SatisfiabilityChecker {

  @Nonnull
  private final ConstraintSystemContext context;

  ChocoSatisfiabilityChecker(ConstraintSystemContext context) {
    this.context = context;
  }

  @Override
  public boolean isValid(Collection<BinaryOption> selectedOptions, boolean isPartialConfiguration) {
    Model cs = context.getConstraintSystem();
    Solver solver = cs.getSolver();

    // feature selection
    Collection<Constraint> addedConstraints = new ArrayList<>();
    for (Entry<ConfigurationOption, Variable> entry : context) {
      BinaryOption option = (BinaryOption) entry.getKey();
      Variable variable = entry.getValue();

      if (selectedOptions.contains(option)) {
        Constraint constraint = cs.boolVar(true).imp(variable.asBoolVar()).decompose();
        addedConstraints.add(constraint);
        constraint.post();
      } else if (!isPartialConfiguration) {
        Constraint constraint = cs.boolVar(true).imp(variable.asBoolVar().not()).decompose();
        addedConstraints.add(constraint);
        constraint.post();
      }
    }

    // check if configuration is valid
    boolean isSolvable = solver.solve();

    // reset constraint system
    addedConstraints.forEach(cs::unpost);
    solver.reset();

    return isSolvable;
  }
}
