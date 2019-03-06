package commands;

import javax.annotation.Nonnull;

import utilities.GlobalContext;
import utilities.ShellCommand;

public final class ClearBucketCacheCommand extends ShellCommand {

  public ClearBucketCacheCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    context.buckets.clear();
    return DEFAULT_SUCCESS_RESPONSE;
  }
}
