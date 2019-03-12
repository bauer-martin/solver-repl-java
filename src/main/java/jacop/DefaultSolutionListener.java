package jacop;

import org.jacop.core.Domain;
import org.jacop.core.IntDomain;
import org.jacop.core.IntVar;
import org.jacop.search.SimpleSolutionListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;

final class DefaultSolutionListener extends SimpleSolutionListener<IntVar> {

  @Nonnull
  private final VariabilityModel vm;

  DefaultSolutionListener(VariabilityModel vm, int solutionLimit) {
    this.vm = vm;
    recordSolutions(true);
    if (solutionLimit > 0) {
      setSolutionLimit(solutionLimit);
    } else {
      searchAll(true);
    }
  }

  @Nonnull
  Set<BinaryOption> getSolutionAsConfig() {
    return getSolutionAsConfig(solutionsNo());
  }

  @Nonnull
  private Set<BinaryOption> getSolutionAsConfig(int solutionNumber) {
    Domain[] solution = getSolution(solutionNumber);
    return IntStream.range(0, solution.length)
                    .filter(i -> ((IntDomain) solution[i]).value() == 1)
                    .mapToObj(i -> vm.getBinaryOption(vars[i].id))
                    .collect(Collectors.toSet());
  }

  @Nonnull
  Collection<Set<BinaryOption>> getSolutionsAsConfigs() {
    int solutionCount = solutionsNo();
    return IntStream.rangeClosed(1, solutionCount)
                    .mapToObj(this::getSolutionAsConfig)
                    .collect(Collectors.toCollection(() -> new ArrayList<>(solutionCount)));
  }
}
