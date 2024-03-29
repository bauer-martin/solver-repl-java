package jacop;

import org.jacop.constraints.And;
import org.jacop.constraints.Not;
import org.jacop.constraints.Or;
import org.jacop.constraints.PrimitiveConstraint;
import org.jacop.constraints.SumBool;
import org.jacop.constraints.XeqC;
import org.jacop.constraints.XeqY;
import org.jacop.core.BooleanVar;
import org.jacop.core.BoundDomain;
import org.jacop.core.IntVar;
import org.jacop.core.Store;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;

final class JaCoPConstraintSystemContext {

  @Nonnull
  private final VariabilityModel vm;

  @Nonnull
  private final Store store;

  @Nonnull
  private final Map<BinaryOption, BooleanVar> optionToVar;

  @Nonnull
  private final Deque<Integer> checkpoints;

  JaCoPConstraintSystemContext(VariabilityModel vm) {
    this.vm = vm;
    store = new Store();
    List<BinaryOption> binaryOptions = vm.getBinaryOptions();
    optionToVar = new HashMap<>(binaryOptions.size());
    checkpoints = new ArrayDeque<>();
    createVariables();
    processBinaryOptions();
    processBinaryConstraints();
  }

  private void createVariables() {
    List<BinaryOption> binaryOptions = vm.getBinaryOptions();
    for (BinaryOption option : binaryOptions) {
      BooleanVar variable = new BooleanVar(store, option.getName());
      optionToVar.put(option, variable);
    }
  }

  private void processBinaryOptions() {
    Collection<BinaryOption> processedAlternatives = new HashSet<>();
    for (BinaryOption option : vm.getBinaryOptions()) {
      addVariableConstraints(option);
      processAlternativeOptions(processedAlternatives, option);
      processExcludedOptionsAsCrossTreeConstraints(option);
      processImpliedOptions(option);
    }
  }

  private void addVariableConstraints(BinaryOption option) {
    BooleanVar variable = optionToVar.get(option);
    if (option.isRoot()) {
      store.impose(new XeqC(variable, 1));
    } else if (option.getParent() != null) {
      BooleanVar parentVar = optionToVar.get(option.getParent());
      if (option.isMandatory() && !option.hasExcludedOptions()) {
        store.impose(new XeqY(variable, parentVar));
      } else {
        store.impose(new Or(new XeqC(variable, 0), new XeqC(parentVar, 1)));
      }
    } else {
      throw new IllegalArgumentException(option.getName() + " has no parent");
    }
  }

  private void processAlternativeOptions(Collection<BinaryOption> processedAlternatives,
                                         BinaryOption option) {
    List<BinaryOption> options = option.collectAlternativeOptions();
    if (options.isEmpty() || processedAlternatives.contains(option)) {
      return;
    }
    BooleanVar parentVar = optionToVar.get(option.getParent());
    BooleanVar[] alternativeGroupVars = new BooleanVar[options.size() + 1];
    alternativeGroupVars[0] = optionToVar.get(option);
    for (int i = 1; i < alternativeGroupVars.length; i++) {
      BinaryOption o = options.get(i - 1);
      alternativeGroupVars[i] = optionToVar.get(o);
    }
    IntVar sumVar = new IntVar(store, new BoundDomain(0, alternativeGroupVars.length));
    store.impose(new SumBool(alternativeGroupVars, "==", sumVar));
    store.impose(new Or(new XeqC(parentVar, 0), new XeqC(sumVar, 1)));
    processedAlternatives.addAll(options);
  }

  private void processExcludedOptionsAsCrossTreeConstraints(BinaryOption option) {
    Collection<List<BinaryOption>> options = option.getNonAlternativeExcludedOptions();
    for (List<BinaryOption> nonAlternativeOption : options) {
      PrimitiveConstraint[] orVars = new PrimitiveConstraint[nonAlternativeOption.size()];
      for (int i = 0; i < orVars.length; i++) {
        BinaryOption o = nonAlternativeOption.get(i);
        orVars[i] = new XeqC(optionToVar.get(o), 1);
      }
      store.impose(new Or(new XeqC(optionToVar.get(option), 0), new Not(new Or(orVars))));
    }
  }

  private void processImpliedOptions(BinaryOption option) {
    for (List<BinaryOption> impliedOptions : option.getImpliedOptions()) {
      PrimitiveConstraint[] orVars = new PrimitiveConstraint[impliedOptions.size()];
      for (int i = 0; i < orVars.length; i++) {
        BinaryOption o = impliedOptions.get(i);
        orVars[i] = new XeqC(optionToVar.get(o), 1);
      }
      store.impose(new Or(new XeqC(optionToVar.get(option), 0), new Or(orVars)));
    }
  }

  /**
   * Handle global cross-tree constraints involving multiple options.
   * The constraints should be in conjunctive normal form.
   */
  private void processBinaryConstraints() {
    for (String constraint : vm.getBinaryConstraints()) {
      boolean and = false;
      String[] terms;
      if (constraint.contains("&")) {
        and = true;
        terms = constraint.split("&");
      } else {
        terms = constraint.split("\\|");
      }

      PrimitiveConstraint[] termVars = new PrimitiveConstraint[terms.length];
      for (int i = 0; i < termVars.length; i++) {
        String term = terms[i];
        String optionName = term.trim();
        if (optionName.startsWith("!")) {
          optionName = optionName.substring(1);
          BinaryOption option = vm.getBinaryOption(optionName);
          termVars[i] = new XeqC(optionToVar.get(option), 0);
        } else {
          BinaryOption option = vm.getBinaryOption(optionName);
          termVars[i] = new XeqC(optionToVar.get(option), 1);
        }
      }
      if (and) {
        store.impose(new And(termVars));
      } else {
        store.impose(new Or(termVars));
      }
    }
  }

  @Nonnull
  public Iterable<Entry<BinaryOption, BooleanVar>> binaryOptions() {
    return optionToVar.entrySet();
  }

  @Nonnull
  Store getStore() {
    return store;
  }

  int getVariableCount() {
    return optionToVar.size();
  }

  @Nonnull
  BooleanVar getVariable(BinaryOption option) {
    if (!optionToVar.containsKey(option)) {
      throw new IllegalArgumentException(option.getName() + " is not used as variable");
    }
    return optionToVar.get(option);
  }

  void markCheckpoint() {
    int baseLevel = store.level;
    store.setLevel(baseLevel + 1);
    checkpoints.push(baseLevel);
  }

  void resetToLastCheckpoint() {
    int baseLevel = checkpoints.pop();
    if (store.level != baseLevel + 1) {
      throw new IllegalStateException("investigation needed");
    }
    store.removeLevel(store.level);
    store.setLevel(store.level - 1);
  }
}

