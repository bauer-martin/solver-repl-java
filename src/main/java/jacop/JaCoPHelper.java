package jacop;

import org.jacop.constraints.XeqC;
import org.jacop.core.BooleanVar;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.search.DepthFirstSearch;
import org.jacop.search.IndomainRandom;
import org.jacop.search.InputOrderSelect;
import org.jacop.search.Search;
import org.jacop.search.SelectChoicePoint;
import org.jacop.search.SolutionListener;

import java.util.Map.Entry;
import java.util.OptionalInt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;

final class JaCoPHelper {

  private JaCoPHelper() {
  }

  static void selectFeatures(JaCoPConstraintSystemContext context,
                             Iterable<BinaryOption> selectedOptions) {
    Store store = context.getStore();
    for (BinaryOption option : selectedOptions) {
      BooleanVar variable = context.getVariable(option);
      store.impose(new XeqC(variable, 1));
    }
  }

  static boolean performSearch(JaCoPConstraintSystemContext context,
                               SolutionListener<IntVar> solutionListener) {
    Search<IntVar> search = new DepthFirstSearch<>();
    return performSearch(context, search, solutionListener, null);
  }

  @Nonnull
  static OptionalInt performMinimizingSearch(JaCoPConstraintSystemContext context,
                                             @Nullable SolutionListener<IntVar> solutionListener,
                                             IntVar costVar) {
    Search<IntVar> search = new DepthFirstSearch<>();
    boolean hasFoundSolution = performSearch(context, search, solutionListener, costVar);
    return hasFoundSolution ? OptionalInt.of(search.getCostValue()) : OptionalInt.empty();
  }

  private static boolean performSearch(JaCoPConstraintSystemContext context,
                                       Search<IntVar> search,
                                       @Nullable SolutionListener<IntVar> solutionListener,
                                       @Nullable IntVar costVar) {
    search.setPrintInfo(false);
    search.setAssignSolution(false);
    Store store = context.getStore();
    IntVar[] allVariables = new IntVar[context.getVariableCount()];
    int index = 0;
    for (Entry<BinaryOption, BooleanVar> entry : context.binaryOptions()) {
      allVariables[index] = entry.getValue();
      index++;
    }
    SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store,
                                                              allVariables,
                                                              new IndomainRandom<>(0));
    if (solutionListener != null) {
      solutionListener.recordSolutions(true);
      search.setSolutionListener(solutionListener);
    }
    return costVar == null ? search.labeling(store, select)
                           : search.labeling(store, select, costVar);
  }
}
