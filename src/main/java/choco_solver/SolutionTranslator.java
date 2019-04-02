package choco_solver;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;

public final class SolutionTranslator {

  private SolutionTranslator() {
  }

  @Nonnull
  static Set<BinaryOption> toBinaryOptions(Solution solution,
                                           ChocoConstraintSystemContext context) {
    Set<BinaryOption> config = new HashSet<>(context.getVariableCount());
    for (Entry<BinaryOption, Variable> entry : context.binaryOptions()) {
      int value = solution.getIntVal(entry.getValue().asBoolVar());
      if (value == 1) {
        config.add(entry.getKey());
      }
    }
    return config;
  }

  @Nonnull
  static Collection<Set<BinaryOption>> toBinaryOptions(Collection<Solution> solutions,
                                                       ChocoConstraintSystemContext context) {
    return solutions.stream()
                    .map(solution -> toBinaryOptions(solution, context))
                    .collect(Collectors.toCollection(() -> new ArrayList<>(solutions.size())));
  }
}
