package choco_solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

import spl_conqueror.BinaryOption;
import spl_conqueror.VariabilityModel;

final class ChocoConstraintSystemContext {

  @Nonnull
  private final VariabilityModel vm;

  @Nonnull
  private final Model model;

  @Nonnull
  private final Map<BinaryOption, Variable> optionToVar;

  private boolean modelIsInUse;

  private int nbVars;

  private int nbCstrs;

  private ChocoConstraintSystemContext(VariabilityModel vm) {
    this.vm = vm;
    model = new Model();
    optionToVar = new HashMap<>();
  }

  @Nonnull
  static ChocoConstraintSystemContext from(VariabilityModel vm) {
    ChocoConstraintSystemContext context = new ChocoConstraintSystemContext(vm);
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
    Collection<BinaryOption> processedAlternatives = new HashSet<>();
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
      variable.eq(1).post();
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

  private void processAlternativeOptions(Collection<BinaryOption> processedAlternatives,
                                         BinaryOption option) {
    List<BinaryOption> options = option.collectAlternativeOptions();
    if (options.isEmpty() || processedAlternatives.contains(option)) {
      return;
    }
    BoolVar parentVar = optionToVar.get(option.getParent()).asBoolVar();
    BoolVar[] alternativeGroupVars = new BoolVar[options.size() + 1];
    alternativeGroupVars[0] = optionToVar.get(option).asBoolVar();
    for (int i = 1; i < alternativeGroupVars.length; i++) {
      BinaryOption o = options.get(i - 1);
      alternativeGroupVars[i] = optionToVar.get(o).asBoolVar();
    }
    parentVar.imp(model.sum(alternativeGroupVars, "=", 1).reify()).post();
    processedAlternatives.addAll(options);
  }

  private void processExcludedOptionsAsCrossTreeConstraints(BinaryOption option) {
    Collection<List<BinaryOption>> options = option.getNonAlternativeExcludedOptions();
    for (List<BinaryOption> nonAlternativeOption : options) {
      BoolVar[] orVars = new BoolVar[nonAlternativeOption.size()];
      for (int i = 0; i < orVars.length; i++) {
        BinaryOption o = nonAlternativeOption.get(i);
        orVars[i] = optionToVar.get(o).asBoolVar();
      }
      optionToVar.get(option).asBoolVar().imp(model.not(model.or(orVars)).reify()).post();
    }
  }

  private void processImpliedOptions(BinaryOption option) {
    for (List<BinaryOption> impliedOptions : option.getImpliedOptions()) {
      BoolVar[] orVars = new BoolVar[impliedOptions.size()];
      for (int i = 0; i < orVars.length; i++) {
        BinaryOption o = impliedOptions.get(i);
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
  public Iterable<Entry<BinaryOption, Variable>> binaryOptions() {
    return optionToVar.entrySet();
  }

  @Nonnull
  Model getModel() {
    if (modelIsInUse) {
      throw new UnsupportedOperationException("Constraint system can not be used more than once! "
                                              + "Call resetModel() first!");
    }
    nbVars = model.getNbVars();
    nbCstrs = model.getNbCstrs();
    modelIsInUse = true;
    return model;
  }

  void resetModel() {
    Arrays.stream(model.getCstrs(), nbCstrs, model.getNbCstrs()).forEach(model::unpost);
    Arrays.stream(model.getVars(), nbVars, model.getNbVars()).forEach(model::unassociates);
    model.getCachedConstants().clear();
    model.getSolver().hardReset();
    modelIsInUse = false;
  }

  int getVariableCount() {
    return optionToVar.size();
  }

  @Nonnull
  Variable getVariable(BinaryOption option) {
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
