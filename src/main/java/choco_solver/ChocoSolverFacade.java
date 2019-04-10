package choco_solver;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.SatisfiabilityChecker;
import spl_conqueror.SolverFacade;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;
import utilities.SolverParameterKeys;

public final class ChocoSolverFacade implements SolverFacade {

  @Nonnull
  private final ChocoConstraintSystemContext context;

  @Nullable
  private ChocoSatisfiabilityChecker satisfiabilityChecker;

  @Nullable
  private ChocoVariantGenerator variantGenerator;

  private int seed = 1;

  public ChocoSolverFacade(VariabilityModel vm) {
    context = ChocoConstraintSystemContext.from(vm);
  }

  @Nonnull
  @Override
  public SatisfiabilityChecker getSatisfiabilityChecker() {
    if (satisfiabilityChecker == null) {
      satisfiabilityChecker = new ChocoSatisfiabilityChecker(context);
    }
    return satisfiabilityChecker;
  }

  @Nonnull
  @Override
  public VariantGenerator getVariantGenerator() {
    if (variantGenerator == null) {
      variantGenerator = new ChocoVariantGenerator(context);
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
