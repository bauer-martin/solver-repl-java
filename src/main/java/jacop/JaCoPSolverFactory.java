package jacop;

import javax.annotation.Nonnull;

import spl_conqueror.SatisfiabilityChecker;
import spl_conqueror.SolverFactory;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;

public final class JaCoPSolverFactory implements SolverFactory {

  @Nonnull
  private final VariabilityModel variabilityModel;

  public JaCoPSolverFactory(VariabilityModel variabilityModel) {
    this.variabilityModel = variabilityModel;
  }

  @Nonnull
  @Override
  public SatisfiabilityChecker createSatisfiabilityChecker() {
    return new JaCoPSatisfiabilityChecker(variabilityModel);
  }

  @Nonnull
  @Override
  public VariantGenerator createVariantGenerator() {
    return new JaCoPVariantGenerator(variabilityModel);
  }
}
