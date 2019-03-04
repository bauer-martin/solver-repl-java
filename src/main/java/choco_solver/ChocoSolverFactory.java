package choco_solver;

import spl_conqueror.SolverFactory;
import spl_conqueror.VariabilityModel;

public final class ChocoSolverFactory implements SolverFactory {

  private final VariabilityModel vm;

  public ChocoSolverFactory(VariabilityModel vm) {
    this.vm = vm;
  }
}
