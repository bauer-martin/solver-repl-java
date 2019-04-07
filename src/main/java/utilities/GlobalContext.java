package utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BucketSession;
import spl_conqueror.SatisfiabilityChecker;
import spl_conqueror.SolverFactory;
import spl_conqueror.VariabilityModel;
import spl_conqueror.VariantGenerator;

public final class GlobalContext {

  @Nullable
  private VariabilityModel variabilityModel;

  @Nullable
  private SolverFactory solverFactory;

  @Nullable
  private SatisfiabilityChecker satisfiabilityChecker;

  @Nullable
  private VariantGenerator variantGenerator;

  @Nullable
  private BucketSession bucketSession;

  @Nonnull
  public VariabilityModel getVariabilityModel() {
    if (variabilityModel == null) {
      throw new IllegalStateException("no variability model has been set");
    }
    return variabilityModel;
  }

  public void setVariabilityModel(VariabilityModel variabilityModel) {
    if (this.variabilityModel == null) {
      this.variabilityModel = variabilityModel;
    } else {
      throw new IllegalStateException("changing the variability model is not supported");
    }
  }

  @Nonnull
  private SolverFactory getSolverFactory() {
    if (solverFactory == null) {
      throw new IllegalStateException("no solver factory has been set");
    }
    return solverFactory;
  }

  public void setSolverFactory(SolverFactory solverFactory) {
    if (this.solverFactory == null
        || !this.solverFactory.getClass().equals(solverFactory.getClass())) {
      this.solverFactory = solverFactory;
      satisfiabilityChecker = null;
      variantGenerator = null;
    }
  }

  @Nonnull
  public SatisfiabilityChecker getSatisfiabilityChecker() {
    if (satisfiabilityChecker == null) {
      satisfiabilityChecker = getSolverFactory().createSatisfiabilityChecker();
    }
    return satisfiabilityChecker;
  }

  public VariantGenerator getVariantGenerator() {
    if (variantGenerator == null) {
      variantGenerator = getSolverFactory().createVariantGenerator();
    }
    return variantGenerator;
  }

  @Nullable
  public BucketSession getBucketSession() {
    return bucketSession;
  }

  public void setBucketSession(@Nullable BucketSession bucketSession) {
    this.bucketSession = bucketSession;
  }
}
