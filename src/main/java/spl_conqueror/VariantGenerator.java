package spl_conqueror;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
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

  /**
   * Based on a given (partial) configuration and a variability model, we aim at finding all
   * optimally maximal or minimal (in terms of selected binary options) configurations.
   *
   * @param minimize        If true, we search for the smallest (in terms of selected options)
   *                        valid configurations. If false, we search for the largest ones.
   * @param config          The (partial) configuration which needs to be expanded to be valid.
   * @param unwantedOptions Binary options that we do not want to become part of the
   *                        configuration. Might be part if there is no other valid configuration
   *                        without them.
   *
   * @return A list of valid configurations that satisfy the VM and the goal (or null if there is
   * none)
   */
  @Nonnull
  List<List<BinaryOption>> findAllOptimalConfigs(boolean minimize,
                                                 Collection<BinaryOption> config,
                                                 Collection<BinaryOption> unwantedOptions);
}

