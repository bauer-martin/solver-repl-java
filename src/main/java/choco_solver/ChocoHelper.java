package choco_solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.SolutionCounter;
import org.chocosolver.solver.search.strategy.Search;
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

  @Nonnull
  private static Solver createSolver(ChocoConstraintSystemContext context, int seed) {
    Model model = context.getModel();
    Solver solver = model.getSolver();
    IntVar[] allVariables = new IntVar[context.getVariableCount()];
    int index = 0;
    for (Entry<BinaryOption, Variable> entry : context.binaryOptions()) {
      allVariables[index] = (IntVar) entry.getValue();
      index++;
    }
    solver.setSearch(Search.randomSearch(allVariables, seed));
    return solver;
  }

  @Nullable
  static Set<BinaryOption> findSolution(ChocoConstraintSystemContext context, int seed) {
    Solver solver = createSolver(context, seed);
    Solution solution = solver.findSolution();
    return toBinaryOptions(solution, context);
  }

  @Nonnull
  static Collection<Set<BinaryOption>> findAllSolutions(ChocoConstraintSystemContext context,
                                                        int seed,
                                                        int limit) {
    Solver solver = createSolver(context, seed);
    List<Solution> solutions = limit > 0
                               ? solver.findAllSolutions(new SolutionCounter(context.getModel(),
                                                                             limit))
                               : solver.findAllSolutions();
    return toBinaryOptions(solutions, context);
  }

  @Nullable
  static Set<BinaryOption> findOptimalSolution(ChocoConstraintSystemContext context,
                                               int seed,
                                               IntVar costVar) {
    Solver solver = createSolver(context, seed);
    Solution optimalSolution = solver.findOptimalSolution(costVar, false);
    return toBinaryOptions(optimalSolution, context);
  }

  @Nonnull
  static Collection<Set<BinaryOption>> findAllOptimalSolutions(ChocoConstraintSystemContext context,
                                                               int seed,
                                                               IntVar costVar) {
    Solver solver = createSolver(context, seed);
    List<Solution> optimalSolutions = solver.findAllOptimalSolutions(costVar, false);
    return toBinaryOptions(optimalSolutions, context);
  }

}
