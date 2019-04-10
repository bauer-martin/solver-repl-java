package choco_solver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.SatisfiabilityChecker;
import spl_conqueror.SolverFacade;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;

public final class ChocoSolverFacade implements SolverFacade {

  @Nonnull
  private final ChocoConstraintSystemContext context;

  @Nullable
  private ChocoSatisfiabilityChecker satisfiabilityChecker;

  @Nullable
  private ChocoVariantGenerator variantGenerator;

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
    }
    return variantGenerator;
  }
}
