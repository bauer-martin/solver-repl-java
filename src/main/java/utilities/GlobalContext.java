package utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import option_coding.OptionCoding;
import spl_conqueror.BucketSession;
import spl_conqueror.SolverFacade;
import spl_conqueror.VariabilityModel;

public final class GlobalContext {

  @Nullable
  private OptionCoding optionCoding;

  @Nullable
  private VariabilityModel variabilityModel;

  @Nullable
  private SolverFacade solverFacade;

  @Nullable
  private BucketSession bucketSession;

  @Nonnull
  public OptionCoding getOptionCoding() {
    if (optionCoding == null) {
      throw new IllegalStateException("no coding strategy has been set");
    }
    return optionCoding;
  }

  public void setOptionCoding(OptionCoding optionCoding) {
    this.optionCoding = optionCoding;
  }

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
  public SolverFacade getSolverFacade() {
    if (solverFacade == null) {
      throw new IllegalStateException("no solver facade has been set");
    }
    return solverFacade;
  }

  public void setSolverFacade(SolverFacade solverFacade) {
    if (this.solverFacade == null
        || !this.solverFacade.getClass().equals(solverFacade.getClass())) {
      this.solverFacade = solverFacade;
    }
  }

  @Nullable
  public BucketSession getBucketSession() {
    return bucketSession;
  }

  public void setBucketSession(@Nullable BucketSession bucketSession) {
    this.bucketSession = bucketSession;
  }
}
