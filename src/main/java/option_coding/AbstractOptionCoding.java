package option_coding;

import javax.annotation.Nonnull;

import spl_conqueror.VariabilityModel;

public abstract class AbstractOptionCoding implements OptionCoding {

  @Nonnull
  protected static final String OPTION_SEPARATOR = ",";

  @Nonnull
  protected static final String CONFIG_SEPARATOR = ";";

  @Nonnull
  protected final VariabilityModel vm;

  protected AbstractOptionCoding(VariabilityModel vm) {
    this.vm = vm;
  }
}
