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
    // get access to the constraint system
    Model cs = context.getConstraintSystem();

    // feature selection
    for (Entry<ConfigurationOption, Variable> entry : context) {
      BinaryOption option = (BinaryOption) entry.getKey();
      Variable variable = entry.getValue();

      if (selectedOptions.contains(option)) {
        cs.boolVar(true).imp(variable.asBoolVar()).post();
      } else if (!isPartialConfiguration) {
        cs.boolVar(true).imp(variable.asBoolVar().not()).post();
      }
    }

    // check if configuration is valid
    Solver solver = cs.getSolver();
    boolean isSolvable = solver.solve();

    // cleanup
    context.resetConstraintSystem();
    return isSolvable;
  }
}
