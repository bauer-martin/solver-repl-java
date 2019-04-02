package jacop;

import java.util.Collection;
import java.util.Set;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.AbstractSolutionsCountTest;

@SuppressWarnings("JUnitTestCaseWithNoTests")
final class JaCoPSolutionsCountTest extends AbstractSolutionsCountTest {

  @Override
  protected int countSolutions(VariabilityModel vm) {
    VariantGenerator vg = new JaCoPVariantGenerator(vm);
    Collection<Set<BinaryOption>> solutions = vg.generateUpToNConfigs(-1);
    return solutions.size();
  }
}
