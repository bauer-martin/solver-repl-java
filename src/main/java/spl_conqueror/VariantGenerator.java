package spl_conqueror;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import utilities.Tuple;

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
  Set<BinaryOption> findOptimalConfig(boolean minimize,
                                      Set<BinaryOption> config,
                                      Set<BinaryOption> unwantedOptions);

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
  Collection<Set<BinaryOption>> findAllOptimalConfigs(boolean minimize,
                                                      Set<BinaryOption> config,
                                                      Set<BinaryOption> unwantedOptions);

  /**
   * Generates up to n solutions of the variability model.
   * Note that this method may also generate less than n solutions if the variability model does
   * not contain sufficient solutions.
   * Moreover, in the case that <code>n < 0</code>, all solutions are generated.
   *
   * @param n The number of solutions to obtain.
   *
   * @return A list of configurations, in which a configuration is a list of selected binary
   * options.
   */
  @Nonnull
  Collection<Set<BinaryOption>> generateUpToNConfigs(int n);

  /**
   * The method aims at finding a configuration which is similar to the given configuration, but
   * does not contain the optionToBeRemoved.
   *
   * @param config         The configuration for which we want to find a similar one.
   * @param optionToRemove The binary configuration option that must not be part of the
   *                       new configuration.
   *
   * @return A tuple consisting of a configuration that is valid, similar to the original
   * configuration and does not contain the optionToBeRemoved and a list containing options that
   * need to be removed from the given configuration to build a valid configuration. Null is
   * returned if no valid configuration exists.
   */
  @Nullable
  Tuple<Set<BinaryOption>, Set<BinaryOption>> generateConfigWithoutOption(
      Set<BinaryOption> config, BinaryOption optionToRemove);

  /**
   * Generates all valid combinations of all configuration options in the variability model.
   *
   * @param optionsToConsider The options that should be considered. All other options are
   *                          ignored.
   *
   * @return Returns a list of binary options.
   */
  @Nonnull
  Collection<Set<BinaryOption>> generateAllVariants(Set<BinaryOption> optionsToConsider);

  /**
   * This method returns a configuration with the given number of selected features.
   *
   * @param selectedOptionsCount The number of features that should be selected.
   * @param featureWeight        The weight of certain feature combinations.
   * @param excludedConfigs      The configurations that should be ignored.
   *
   * @return A list of binary options.
   */
  @Nullable
  Set<BinaryOption> generateConfig(int selectedOptionsCount,
                                   Map<Set<BinaryOption>, Integer> featureWeight,
                                   Collection<Set<BinaryOption>> excludedConfigs);

}

