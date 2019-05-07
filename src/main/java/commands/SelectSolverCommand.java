package commands;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import spl_conqueror.SolverFacade;
import spl_conqueror.VariabilityModel;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class SelectSolverCommand extends ShellCommand {

  private final Map<String, Function<VariabilityModel, SolverFacade>> solverConstructors;

  public SelectSolverCommand(GlobalContext context) {
    super(context);
    solverConstructors = new HashMap<>();
  }

  public void registerSolver(String solverName,
                             Function<VariabilityModel, SolverFacade> solverConstructor) {
    solverConstructors.put(solverName, solverConstructor);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    if (!solverConstructors.containsKey(argsString)) {
      return error("unknown solver: " + argsString);
    }
    Function<VariabilityModel, SolverFacade> solverConstructor = solverConstructors.get(argsString);
    SolverFacade facade = solverConstructor.apply(context.getVariabilityModel());
    context.setSolverFacade(facade);
    return DEFAULT_SUCCESS_RESPONSE;
  }
}
