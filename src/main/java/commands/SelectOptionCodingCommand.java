package commands;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import option_coding.OptionCoding;
import option_coding.OptionNameOptionCoding;
import spl_conqueror.VariabilityModel;
import utilities.GlobalContext;
import utilities.ShellCommand;

public final class SelectOptionCodingCommand extends ShellCommand {

  private static final Map<String, CodingStrategyType> CODINGS_BY_NAME;

  static {
    CODINGS_BY_NAME = new HashMap<>();
    for (CodingStrategyType type : CodingStrategyType.values()) {
      CODINGS_BY_NAME.put(type.getName(), type);
    }
  }

  public SelectOptionCodingCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    if (!CODINGS_BY_NAME.containsKey(argsString)) {
      return error("unknown coding strategy: " + argsString);
    }
    CodingStrategyType codingType = CODINGS_BY_NAME.get(argsString);
    OptionCoding optionCoding = codingType.createOptionCoding(context.getVariabilityModel());
    context.setOptionCoding(optionCoding);
    return DEFAULT_SUCCESS_RESPONSE;
  }

  private enum CodingStrategyType {
    OPTION_NAME;

    @Nonnull
    String getName() {
      switch (this) {
        case OPTION_NAME:
          return "option-name";
        default:
          throw new IllegalStateException("missing enum case");
      }
    }

    @Nonnull
    OptionCoding createOptionCoding(VariabilityModel vm) {
      switch (this) {
        case OPTION_NAME:
          return new OptionNameOptionCoding(vm);
        default:
          throw new IllegalStateException("missing enum case");
      }
    }
  }
}
