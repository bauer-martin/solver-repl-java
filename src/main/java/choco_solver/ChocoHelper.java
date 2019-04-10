package choco_solver;

import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

  @Nullable
  private static Set<BinaryOption> toBinaryOptions(@Nullable Solution solution,
                                                   ChocoConstraintSystemContext context) {
    if (solution == null) {
      return null;
    }
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
  private static Collection<Set<BinaryOption>> toBinaryOptions(Collection<Solution> solutions,
                                                               ChocoConstraintSystemContext context) {
    return solutions.stream()
                    .map(solution -> toBinaryOptions(solution, context))
                    .collect(Collectors.toCollection(() -> new ArrayList<>(solutions.size())));
  }

  @Nullable
  static Set<BinaryOption> findSolution(ChocoConstraintSystemContext context) {
    Solver solver = context.getModel().getSolver();
    Solution solution = solver.findSolution();
    return toBinaryOptions(solution, context);
  }

  @Nonnull
  static Collection<Set<BinaryOption>> findAllSolutions(ChocoConstraintSystemContext context,
                                                        int limit) {
    Solver solver = context.getModel().getSolver();
    List<Solution> solutions = limit > 0
                               ? solver.findAllSolutions(new SolutionCounter(context.getModel(),
                                                                             limit))
                               : solver.findAllSolutions();
    return toBinaryOptions(solutions, context);
  }

  @Nullable
  static Set<BinaryOption> findOptimalSolution(ChocoConstraintSystemContext context,
                                               IntVar costVar) {
    Solver solver = context.getModel().getSolver();
    Solution optimalSolution = solver.findOptimalSolution(costVar, false);
    return toBinaryOptions(optimalSolution, context);
  }

  @Nonnull
  static Collection<Set<BinaryOption>> findAllOptimalSolutions(ChocoConstraintSystemContext context,
                                                               IntVar costVar) {
    Solver solver = context.getModel().getSolver();
    List<Solution> optimalSolutions = solver.findAllOptimalSolutions(costVar, false);
    return toBinaryOptions(optimalSolutions, context);
  }

}
