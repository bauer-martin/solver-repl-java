package jacop;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;

public final class SolutionTranslator {

  private SolutionTranslator() {
  }

  @Nonnull
  @SuppressWarnings("TypeMayBeWeakened")
  static Set<BinaryOption> toBinaryOptions(Set<String> selectedOptions,
                                           VariabilityModel vm) {
    return selectedOptions.stream().map(vm::getBinaryOption).collect(Collectors.toSet());
  }

  @Nonnull
  static Collection<Set<BinaryOption>> toBinaryOptions(Collection<Set<String>> solutions,
                                                       VariabilityModel vm) {
    return solutions.stream()
                    .map(solution -> toBinaryOptions(solution, vm))
                    .collect(Collectors.toList());
  }
}
