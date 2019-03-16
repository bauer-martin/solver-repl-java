package choco_solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;

import java.util.List;

import spl_conqueror.VariabilityModel;
import utilities.AbstractSolutionsCountTest;

@SuppressWarnings("JUnitTestCaseWithNoTests")
final class ChocoSolutionsCountTest extends AbstractSolutionsCountTest {

  @Override
  protected int countSolutions(VariabilityModel vm) {
    Model constraintSystem = ConstraintSystemContext.from(vm).getModel();
    Solver solver = constraintSystem.getSolver();
    List<Solution> allSolutions = solver.findAllSolutions();
    return allSolutions.size();
  }
}
