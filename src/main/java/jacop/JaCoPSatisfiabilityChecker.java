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

import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.SatisfiabilityChecker;
import spl_conqueror.VariabilityModel;

public final class JaCoPSatisfiabilityChecker implements SatisfiabilityChecker {

  @Nonnull
  private final JaCoPConstraintSystemContext context;

  JaCoPSatisfiabilityChecker(VariabilityModel variabilityModel) {
    context = new JaCoPConstraintSystemContext(variabilityModel);
  }

  @Override
  public boolean isValid(Set<BinaryOption> selectedOptions, boolean isPartialConfiguration) {
    context.markCheckpoint();
    Store store = context.getStore();

    // feature selection and variable gathering
    IntVar[] allVariables = new IntVar[context.getVariableCount()];
    int index = 0;
    for (Entry<BinaryOption, BooleanVar> entry : context.binaryOptions()) {
      BinaryOption option = entry.getKey();
      BooleanVar variable = entry.getValue();
      if (selectedOptions.contains(option)) {
        store.impose(new XeqC(variable, 1));
      } else if (!isPartialConfiguration) {
        store.impose(new XeqC(variable, 0));
      }
      allVariables[index] = entry.getValue();
      index++;
    }

    // check if configuration is valid
    Search<IntVar> search = new DepthFirstSearch<>();
    search.setPrintInfo(false);
    search.setAssignSolution(false);

    SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store,
                                                              allVariables,
                                                              new IndomainRandom<>(0));
    boolean hasFoundSolution = search.labeling(store, select);

    // cleanup
    context.resetToLastCheckpoint();
    return hasFoundSolution;
  }
}
