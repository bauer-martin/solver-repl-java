package choco_solver;

import javax.annotation.Nonnull;

import spl_conqueror.SatisfiabilityChecker;
import spl_conqueror.SolverFactory;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;

public final class ChocoSolverFactory implements SolverFactory {

  @Nonnull
  private final ConstraintSystemContext context;

  public ChocoSolverFactory(VariabilityModel vm) {
    context = ConstraintSystemContext.from(vm);
  }

  @Nonnull
  @Override
  public SatisfiabilityChecker createSatisfiabilityChecker() {
    return new ChocoSatisfiabilityChecker(context);
  }

  @Nonnull
  @Override
  public VariantGenerator createVariantGenerator() {
    return new ChocoVariantGenerator(context);
  }
}
