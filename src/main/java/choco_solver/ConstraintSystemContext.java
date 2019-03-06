package choco_solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;
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

final class ConstraintSystemContext implements Iterable<Entry<ConfigurationOption, Variable>> {

  @Nonnull
  private final VariabilityModel vm;

  @Nonnull
  private final Model model;

  @Nonnull
  private final Map<ConfigurationOption, Variable> optionToVar;

  private boolean constraintSystemIsInUse;

  private int nbVars;

  private int nbCstrs;

  private ConstraintSystemContext(VariabilityModel vm) {
    this.vm = vm;
    model = new Model();
    optionToVar = new HashMap<>();
  }

  @Nonnull
  static ConstraintSystemContext from(VariabilityModel vm) {
    ConstraintSystemContext context = new ConstraintSystemContext(vm);
    context.createVariables();
    context.processBinaryOptions();
    context.processBinaryConstraints();
    return context;
  }

  private void createVariables() {
    for (BinaryOption option : vm.getBinaryOptions()) {
      BoolVar variable = model.boolVar(option.getName());
      optionToVar.put(option, variable);
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
    BoolVar variable = optionToVar.get(option).asBoolVar();
    if (option.isRoot()) {
      model.boolVar(true).imp(variable).post();
    } else if (option.getParent() != null) {
      BoolVar parentVar = optionToVar.get(option.getParent()).asBoolVar();
      variable.imp(parentVar).post();
      if (option.isMandatory() && !option.hasExcludedOptions()) {
        parentVar.imp(variable).post();
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
    BoolVar parentVar = optionToVar.get(option.getParent()).asBoolVar();
    BoolVar[] alternativeGroupVars = new BoolVar[options.size() + 1];
    alternativeGroupVars[0] = optionToVar.get(option).asBoolVar();
    for (int i = 1; i < alternativeGroupVars.length; i++) {
      ConfigurationOption o = options.get(i - 1);
      alternativeGroupVars[i] = optionToVar.get(o).asBoolVar();
    }
    parentVar.imp(model.sum(alternativeGroupVars, "=", 1).reify()).post();
    processedAlternatives.addAll(options);
  }

  private void processExcludedOptionsAsCrossTreeConstraints(BinaryOption option) {
    List<List<ConfigurationOption>> options = option.getNonAlternativeExcludedOptions();
    for (List<ConfigurationOption> nonAlternativeOption : options) {
      BoolVar[] orVars = new BoolVar[nonAlternativeOption.size()];
      for (int i = 0; i < orVars.length; i++) {
        ConfigurationOption o = nonAlternativeOption.get(i);
        orVars[i] = optionToVar.get(o).asBoolVar();
      }
      optionToVar.get(option).asBoolVar().imp(model.not(model.or(orVars)).reify()).post();
    }
  }

  private void processImpliedOptions(BinaryOption option) {
    for (List<ConfigurationOption> impliedOptions : option.getImpliedOptions()) {
      BoolVar[] orVars = new BoolVar[impliedOptions.size()];
      for (int i = 0; i < orVars.length; i++) {
        ConfigurationOption o = impliedOptions.get(i);
        orVars[i] = optionToVar.get(o).asBoolVar();
      }
      optionToVar.get(option).asBoolVar().imp(model.or(orVars).reify()).post();
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

      BoolVar[] termVars = new BoolVar[terms.length];
      for (int i = 0; i < termVars.length; i++) {
        String term = terms[i];
        String optionName = term.trim();
        if (optionName.startsWith("!")) {
          optionName = optionName.substring(1);
          BinaryOption option = vm.getBinaryOption(optionName);
          termVars[i] = optionToVar.get(option).asBoolVar().not();
        } else {
          BinaryOption option = vm.getBinaryOption(optionName);
          termVars[i] = optionToVar.get(option).asBoolVar();
        }
      }
      if (and) {
        model.and(termVars).post();
      } else {
        model.or(termVars).post();
      }
    }
  }

  @Nonnull
  @Override
  public Iterator<Entry<ConfigurationOption, Variable>> iterator() {
    return optionToVar.entrySet().iterator();
  }

  @Nonnull
  Model getConstraintSystem() {
    if (constraintSystemIsInUse) {
      throw new UnsupportedOperationException("Constraint system can not be used more than once! "
                                              + "Call resetConstraintSystem() first!");
    }
    nbVars = model.getNbVars();
    nbCstrs = model.getNbCstrs();
    constraintSystemIsInUse = true;
    return model;
  }

  void resetConstraintSystem() {
    Arrays.stream(model.getCstrs(), nbCstrs, model.getNbCstrs()).forEach(model::unpost);
    Arrays.stream(model.getVars(), nbVars, model.getNbVars()).forEach(model::unassociates);
    model.getCachedConstants().clear();
    model.getSolver().hardReset();
    constraintSystemIsInUse = false;
  }

  int getVariableCount() {
    return optionToVar.size();
  }

  @Nonnull
  Variable getVariable(ConfigurationOption option) {
    if (!optionToVar.containsKey(option)) {
      throw new IllegalArgumentException(option.getName() + " is not used as variable");
    }
    return optionToVar.get(option);
  }

  @Nonnull
  VariabilityModel getVariabilityModel() {
    return vm;
  }
}
