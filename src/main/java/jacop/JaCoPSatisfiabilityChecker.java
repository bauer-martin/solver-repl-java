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

import java.util.Collection;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.ConfigurationOption;
import spl_conqueror.SatisfiabilityChecker;
import spl_conqueror.VariabilityModel;

public final class JaCoPSatisfiabilityChecker implements SatisfiabilityChecker {

  @Nonnull
  private final ConstraintSystemContext context;

  JaCoPSatisfiabilityChecker(VariabilityModel variabilityModel) {
    context = new ConstraintSystemContext(variabilityModel);
  }

  @Override
  public boolean isValid(Collection<BinaryOption> selectedOptions, boolean isPartialConfiguration) {
    Store store = context.getStore();
    int baseLevel = store.level;
    store.setLevel(baseLevel + 1);

    // feature selection
    for (Entry<ConfigurationOption, BooleanVar> entry : context) {
      BinaryOption option = (BinaryOption) entry.getKey();
      BooleanVar variable = entry.getValue();

      if (selectedOptions.contains(option)) {
        store.impose(new XeqC(variable, 1));
      } else if (!isPartialConfiguration) {
        store.impose(new XeqC(variable, 0));
      }
    }

    // check if configuration is valid
    Search<IntVar> search = new DepthFirstSearch<>();
    search.setPrintInfo(false);
    search.setAssignSolution(false);
    SelectChoicePoint<IntVar> select = new InputOrderSelect<>(store,
                                                              context.getVariables(),
                                                              new IndomainRandom<>(0));
    boolean hasFoundSolution = search.labeling(store, select);

    if (store.level == baseLevel + 1) {
      store.removeLevel(store.level);
      return hasFoundSolution;
    } else {
      throw new IllegalStateException("investigation needed");
    }
  }
}
