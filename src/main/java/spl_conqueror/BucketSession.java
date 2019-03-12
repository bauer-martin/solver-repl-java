package spl_conqueror;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

public interface BucketSession {

  /**
   * This method returns a configuration with the given number of selected features.
   *
   * @param selectedOptionsCount The number of features that should be selected.
   * @param featureWeight        The weight of certain feature combinations.
   *
   * @return A list of binary options.
   */
  @Nullable
  Set<BinaryOption> generateConfig(int selectedOptionsCount,
                                   Map<Set<BinaryOption>, Integer> featureWeight);
}
