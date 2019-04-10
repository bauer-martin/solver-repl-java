package jacop;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.SatisfiabilityChecker;
import spl_conqueror.SolverFacade;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;

public final class JaCoPSolverFacade implements SolverFacade {

  @Nonnull
  private final VariabilityModel variabilityModel;

  @Nullable
  private JaCoPSatisfiabilityChecker satisfiabilityChecker;

  @Nullable
  private JaCoPVariantGenerator variantGenerator;

  public JaCoPSolverFacade(VariabilityModel variabilityModel) {
    this.variabilityModel = variabilityModel;
  }

  @Nonnull
  @Override
  public SatisfiabilityChecker getSatisfiabilityChecker() {
    if (satisfiabilityChecker == null) {
      satisfiabilityChecker = new JaCoPSatisfiabilityChecker(variabilityModel);
    }
    return satisfiabilityChecker;
  }

  @Nonnull
  @Override
  public VariantGenerator getVariantGenerator() {
    if (variantGenerator == null) {
      variantGenerator = new JaCoPVariantGenerator(variabilityModel);
    }
    return variantGenerator;
  }
}
