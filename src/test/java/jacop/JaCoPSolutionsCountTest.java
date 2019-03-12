package jacop;

import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainRandom;
import org.jacop.search.InputOrderSelect;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;

import spl_conqueror.VariabilityModel;
import utilities.AbstractSolutionsCountTest;

@SuppressWarnings("JUnitTestCaseWithNoTests")
final class JaCoPSolutionsCountTest extends AbstractSolutionsCountTest {

  @Override
  protected int countSolutions(VariabilityModel vm) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);
    Store store = context.getStore();
    Search<IntVar> search = new DepthFirstSearch<>();
    search.setPrintInfo(false);
    search.setAssignSolution(false);
    SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store,
                                                              context.getVariables(),
                                                              new IndomainRandom<>(0));
    search.getSolutionListener().searchAll(true);
    search.labeling(store, select);
    return search.getSolutionListener().solutionsNo();
  }
}
