package jacop;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.SatisfiabilityChecker;
import spl_conqueror.SolverFacade;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.SolverParameterKeys;

public final class JaCoPSolverFacade implements SolverFacade {

  @Nonnull
  private final VariabilityModel variabilityModel;

  @Nullable
  private JaCoPSatisfiabilityChecker satisfiabilityChecker;

  @Nullable
  private JaCoPVariantGenerator variantGenerator;

  private int seed = 1;

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
      applyParameters();
    }
    return variantGenerator;
  }

  @Override
  public void setParameters(Map<String, String> parameters) {
    if (parameters.containsKey(SolverParameterKeys.RANDOM_SEED)) {
      seed = Integer.parseInt(parameters.get(SolverParameterKeys.RANDOM_SEED));
    }
    applyParameters();
  }

  private void applyParameters() {
    if (variantGenerator == null) {
      return;
    }
    variantGenerator.setSeed(seed);
  }
}
