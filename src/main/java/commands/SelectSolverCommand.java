package commands;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import choco_solver.ChocoSolverFacade;
import jacop.JaCoPSolverFacade;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class SelectSolverCommand extends ShellCommand {

  private static final Map<String, SolverType> SOLVER_TYPES_BY_NAME;

  static {
    SOLVER_TYPES_BY_NAME = new HashMap<>();
    for (SolverType solverType : SolverType.values()) {
      SOLVER_TYPES_BY_NAME.put(solverType.getName(), solverType);
    }
  }

  public SelectSolverCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    if (!SOLVER_TYPES_BY_NAME.containsKey(argsString)) {
      return error("unknown solver: " + argsString);
    }
    switch (SOLVER_TYPES_BY_NAME.get(argsString)) {
      case CHOCO:
        context.setSolverFacade(new ChocoSolverFacade(context.getVariabilityModel()));
        break;
      case JACOP:
        context.setSolverFacade(new JaCoPSolverFacade(context.getVariabilityModel()));
        break;
    }
    return DEFAULT_SUCCESS_RESPONSE;
  }

  private enum SolverType {
    CHOCO,
    JACOP;

    @Nonnull
    String getName() {
      switch (this) {
        case CHOCO:
          return "choco";
        case JACOP:
          return "jacop";
        default:
          throw new IllegalStateException("missing enum case");
      }
    }
  }
}
