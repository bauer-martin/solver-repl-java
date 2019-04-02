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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.ConfigurationOption;
import spl_conqueror.VariabilityModel;

final class JaCoPConstraintSystemContext implements Iterable<Entry<ConfigurationOption, BooleanVar>> {

  @Nonnull
  private final VariabilityModel vm;

  @Nonnull
  private final Store store;

  @Nonnull
  private final Map<ConfigurationOption, BooleanVar> optionToVar;

  @Nonnull
  private final IntVar[] variables;

  JaCoPConstraintSystemContext(VariabilityModel vm) {
    this.vm = vm;
    store = new Store();
    List<BinaryOption> binaryOptions = vm.getBinaryOptions();
    optionToVar = new HashMap<>(binaryOptions.size());
    variables = new IntVar[binaryOptions.size()];
    createVariables();
    processBinaryOptions();
    processBinaryConstraints();
  }

  private void createVariables() {
    List<BinaryOption> binaryOptions = vm.getBinaryOptions();
    for (int i = 0; i < binaryOptions.size(); i++) {
      BinaryOption option = binaryOptions.get(i);
      BooleanVar variable = new BooleanVar(store, option.getName());
      optionToVar.put(option, variable);
      variables[i] = variable;
    }
  }

  private void processBinaryOptions() {
    Collection<ConfigurationOption> processedAlternatives = new HashSet<>();
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

  private void processAlternativeOptions(Collection<ConfigurationOption> processedAlternatives,
                                         BinaryOption option) {
    List<ConfigurationOption> options = option.collectAlternativeOptions();
    if (options.isEmpty() || processedAlternatives.contains(option)) {
      return;
    }
    BooleanVar parentVar = optionToVar.get(option.getParent());
    BooleanVar[] alternativeGroupVars = new BooleanVar[options.size() + 1];
    alternativeGroupVars[0] = optionToVar.get(option);
    for (int i = 1; i < alternativeGroupVars.length; i++) {
      ConfigurationOption o = options.get(i - 1);
      alternativeGroupVars[i] = optionToVar.get(o);
    }
    IntVar sumVar = new IntVar(store, new BoundDomain(0, alternativeGroupVars.length));
    store.impose(new SumBool(alternativeGroupVars, "==", sumVar));
    store.impose(new Or(new XeqC(parentVar, 0), new XeqC(sumVar, 1)));
    processedAlternatives.addAll(options);
  }

  private void processExcludedOptionsAsCrossTreeConstraints(BinaryOption option) {
    Collection<List<ConfigurationOption>> options = option.getNonAlternativeExcludedOptions();
    for (List<ConfigurationOption> nonAlternativeOption : options) {
      PrimitiveConstraint[] orVars = new PrimitiveConstraint[nonAlternativeOption.size()];
      for (int i = 0; i < orVars.length; i++) {
        ConfigurationOption o = nonAlternativeOption.get(i);
        orVars[i] = new XeqC(optionToVar.get(o), 1);
      }
      store.impose(new Or(new XeqC(optionToVar.get(option), 0), new Not(new Or(orVars))));
    }
  }

  private void processImpliedOptions(BinaryOption option) {
    for (List<ConfigurationOption> impliedOptions : option.getImpliedOptions()) {
      PrimitiveConstraint[] orVars = new PrimitiveConstraint[impliedOptions.size()];
      for (int i = 0; i < orVars.length; i++) {
        ConfigurationOption o = impliedOptions.get(i);
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
  @Override
  public Iterator<Entry<ConfigurationOption, BooleanVar>> iterator() {
    return optionToVar.entrySet().iterator();
  }

  @Nonnull
  Store getStore() {
    return store;
  }

  IntVar[] getVariables() {
    //noinspection AssignmentOrReturnOfFieldWithMutableType
    return variables;
  }

  int getVariableCount() {
    assert variables != null;
    return variables.length;
  }

  @Nonnull
  BooleanVar getVariable(ConfigurationOption option) {
    assert optionToVar != null;
    if (!optionToVar.containsKey(option)) {
      throw new IllegalArgumentException(option.getName() + " is not used as variable");
    }
    return optionToVar.get(option);
  }
}

