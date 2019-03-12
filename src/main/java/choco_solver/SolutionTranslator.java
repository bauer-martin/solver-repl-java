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
import spl_conqueror.ConfigurationOption;

public final class SolutionTranslator {

  private SolutionTranslator() {
  }

  @Nonnull
  static Set<BinaryOption> toBinaryOptions(Solution solution,
                                           ConstraintSystemContext context) {
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
  static Collection<Set<BinaryOption>> toBinaryOptions(Collection<Solution> solutions,
                                                       ConstraintSystemContext context) {
    return solutions.stream()
                    .map(solution -> toBinaryOptions(solution, context))
                    .collect(Collectors.toCollection(() -> new ArrayList<>(solutions.size())));
  }
}
