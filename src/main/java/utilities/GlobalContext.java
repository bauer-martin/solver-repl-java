package utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import spl_conqueror.BinaryOption;
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

  @Nonnull
  private final Map<Integer, Collection<List<BinaryOption>>> buckets = new HashMap<>();

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

  @Nonnull
  private SolverFactory getSolverFactory() {
    if (solverFactory == null) {
      throw new IllegalStateException("no solver factory has been set");
    } else {
      return solverFactory;
    }
  }

  public void setSolverFactory(SolverFactory solverFactory) {
    this.solverFactory = solverFactory;
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

  @Nonnull
  public Collection<List<BinaryOption>> getBucket(int selectedOptionCount) {
    return buckets.computeIfAbsent(selectedOptionCount, n -> new ArrayList<>());
  }

  public void clearBucketCache() {
    buckets.clear();
  }
}
