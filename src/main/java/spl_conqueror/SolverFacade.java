package spl_conqueror;

import javax.annotation.Nonnull;

public interface SolverFacade {

  @Nonnull
  SatisfiabilityChecker getSatisfiabilityChecker();

  @Nonnull
  VariantGenerator getVariantGenerator();
}
