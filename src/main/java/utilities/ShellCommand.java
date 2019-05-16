package utilities;

import javax.annotation.Nonnull;

public abstract class ShellCommand {

  @Nonnull
  public static final String ERROR_PREFIX = "error: ";

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
    return ERROR_PREFIX + message;
  }
}
