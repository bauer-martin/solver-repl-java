package choco_solver;

import java.util.Collection;
import java.util.Set;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.AbstractSolutionsCountTest;

@SuppressWarnings("JUnitTestCaseWithNoTests")
final class ChocoSolutionsCountTest extends AbstractSolutionsCountTest {

  @Override
  protected int countSolutions(VariabilityModel vm) {
    ChocoConstraintSystemContext context = ChocoConstraintSystemContext.from(vm);
    VariantGenerator vg = new ChocoVariantGenerator(context);
    Collection<Set<BinaryOption>> solutions = vg.generateUpToNConfigs(-1);
    return solutions.size();
  }
}
