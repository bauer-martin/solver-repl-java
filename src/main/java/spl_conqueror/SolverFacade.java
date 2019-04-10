package spl_conqueror;

import java.util.Map;

import javax.annotation.Nonnull;

public interface SolverFacade {

  @Nonnull
  SatisfiabilityChecker getSatisfiabilityChecker();

  @Nonnull
  VariantGenerator getVariantGenerator();

  void setParameters(Map<String, String> parameters);
}
