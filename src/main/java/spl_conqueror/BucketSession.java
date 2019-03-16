package spl_conqueror;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

public interface BucketSession {

  /**
   * This method returns a configuration with the given number of selected features.
   *
   * @param selectedOptionsCount The number of features that should be selected.
   * @param featureRanking       The ranking of certain feature combinations.
   *
   * @return A list of binary options.
   */
  @Nullable
  Set<BinaryOption> generateConfig(int selectedOptionsCount,
                                   List<Set<BinaryOption>> featureRanking);
}
