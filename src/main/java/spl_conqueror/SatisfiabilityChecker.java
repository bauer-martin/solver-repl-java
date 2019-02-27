package spl_conqueror;

import java.util.Collection;

public interface SatisfiabilityChecker {

  /**
   * Checks whether the boolean selection is valid w.r.t. a variability model.
   *
   * @param selectedOptions        The list of binary options that are SELECTED (only selected
   *                               options must occur in the list).
   * @param isPartialConfiguration Whether the given list of options represents only a partial
   *                               configuration. This means that options not in config might be
   *                               additionally select to obtain a valid configuration.
   *
   * @return True if it is a valid selection w.r.t. the variability model, false otherwise.
   */
  boolean isValid(Collection<BinaryOption> selectedOptions, boolean isPartialConfiguration);
}
