package utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.VariabilityModel;

public final class GlobalContext {

  @Nullable
  private VariabilityModel variabilityModel;

  @Nonnull
  public VariabilityModel getVariabilityModel() {
    if (variabilityModel != null) {
      return variabilityModel;
    } else {
      throw new IllegalStateException("no variability model has been set");
    }
  }

  public void setVariabilityModel(VariabilityModel variabilityModel) {
    if (this.variabilityModel == null) {
      this.variabilityModel = variabilityModel;
    } else {
      throw new IllegalStateException("changing the variability model is not supported");
    }
  }
}
