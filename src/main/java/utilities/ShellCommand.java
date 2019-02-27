package utilities;

import javax.annotation.Nonnull;

public abstract class ShellCommand {

  protected static final String DEFAULT_SUCCESS_RESPONSE = "ok";

  @Nonnull
  protected final GlobalContext context;

  protected ShellCommand(GlobalContext context) {
    this.context = context;
  }

  @Nonnull
  public abstract String execute(String argsString);

  @SuppressWarnings("MethodMayBeStatic")
  protected final String error(String message) {
    return "error: " + message;
  }
}
