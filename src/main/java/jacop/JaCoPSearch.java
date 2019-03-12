package jacop;

import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainRandom;
import org.jacop.search.InputOrderSelect;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SolutionListener;

import java.util.OptionalInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class JaCoPSearch {

  private JaCoPSearch() {
  }

  static boolean performSearch(ConstraintSystemContext context,
                               SolutionListener<IntVar> solutionListener) {
    Search<IntVar> search = new DepthFirstSearch<>();
    return performSearch(context, search, solutionListener, null);
  }

  @Nonnull
  static OptionalInt performMinimizingSearch(ConstraintSystemContext context,
                                             @Nullable SolutionListener<IntVar> solutionListener,
                                             IntVar costVar) {
    Search<IntVar> search = new DepthFirstSearch<>();
    boolean hasFoundSolution = performSearch(context, search, solutionListener, costVar);
    return hasFoundSolution ? OptionalInt.of(search.getCostValue()) : OptionalInt.empty();
  }

  private static boolean performSearch(ConstraintSystemContext context,
                                       Search<IntVar> search,
                                       @Nullable SolutionListener<IntVar> solutionListener,
                                       @Nullable IntVar costVar) {
    search.setPrintInfo(false);
    search.setAssignSolution(false);
    Store store = context.getStore();
    SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store,
                                                              context.getVariables(),
                                                              new IndomainRandom<>(0));
    if (solutionListener != null) {
      solutionListener.recordSolutions(true);
      search.setSolutionListener(solutionListener);
    }
    return costVar == null ? search.labeling(store, select)
                           : search.labeling(store, select, costVar);
  }
}