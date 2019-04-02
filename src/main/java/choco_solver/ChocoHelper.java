package choco_solver;

import org.chocosolver.solver.variables.Variable;

import java.util.Collection;
import java.util.Map.Entry;

import spl_conqueror.BinaryOption;

final class ChocoHelper {

  private ChocoHelper() {
  }

  static void selectFeatures(ChocoConstraintSystemContext context,
                             Collection<BinaryOption> selectedOptions) {
    for (Entry<BinaryOption, Variable> entry : context.binaryOptions()) {
      BinaryOption option = entry.getKey();
      Variable variable = entry.getValue();
      if (selectedOptions.contains(option)) {
        variable.asBoolVar().eq(1).post();
      }
    }
  }

}
