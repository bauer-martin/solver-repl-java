package spl_conqueror;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

public interface VariantGenerator {

  /**
   * Based on a given (partial) configuration, we aim at finding the smallest valid configuration
   * that has all options.
   *
   * @param minimize        If true, we search for the smallest (in terms of selected options)
   *                        valid configuration. If false, we search for the largest one.
   * @param config          The (partial) configuration which needs to be expanded to be valid.
   * @param unwantedOptions Binary options that we do not want to become part of the
   *                        configuration. Might be part if there is no other valid configuration
   *                        without them.
   *
   * @return The valid configuration (or null if there is none) that satisfies the VM and the goal.
   */
  @Nullable
  List<BinaryOption> findOptimalConfig(boolean minimize,
                                       Collection<BinaryOption> config,
                                       Collection<BinaryOption> unwantedOptions);
}

