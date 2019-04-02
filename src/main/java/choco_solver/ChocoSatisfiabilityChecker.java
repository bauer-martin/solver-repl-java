package choco_solver;

import static choco_solver.ChocoHelper.selectFeatures;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.Variable;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.SatisfiabilityChecker;

final class ChocoSatisfiabilityChecker implements SatisfiabilityChecker {

  @Nonnull
  private final ChocoConstraintSystemContext context;

  ChocoSatisfiabilityChecker(ChocoConstraintSystemContext context) {
    this.context = context;
  }

  private static void selectFeaturesAndDeselectAllOthers(ChocoConstraintSystemContext context,
                                                         Collection<BinaryOption> selectedOptions) {
    for (Entry<BinaryOption, Variable> entry : context.binaryOptions()) {
      BinaryOption option = entry.getKey();
      Variable variable = entry.getValue();
      if (selectedOptions.contains(option)) {
        variable.asBoolVar().eq(1).post();
      } else {
        variable.asBoolVar().eq(0).post();
      }
    }
  }

  @Override
  public boolean isValid(Set<BinaryOption> selectedOptions, boolean isPartialConfiguration) {
    context.markCheckpoint();
    Model model = context.getModel();

    // feature selection
    if (isPartialConfiguration) {
      selectFeatures(context, selectedOptions);
    } else {
      selectFeaturesAndDeselectAllOthers(context, selectedOptions);
    }

    // check if configuration is valid
    Solver solver = model.getSolver();
    boolean isSolvable = solver.solve();

    // cleanup
    context.resetToLastCheckpoint();
    return isSolvable;
  }
}
