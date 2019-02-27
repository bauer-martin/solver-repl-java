package spl_conqueror;

import javax.annotation.Nonnull;

public interface SolverFactory {

  @Nonnull
  SatisfiabilityChecker createSatisfiabilityChecker();
}
