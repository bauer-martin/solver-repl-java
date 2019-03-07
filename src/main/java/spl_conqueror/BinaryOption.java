package spl_conqueror;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

public final class BinaryOption extends ConfigurationOption {

  @Nonnull
  public static final String ROOT_NAME = "root";

  private final boolean isOptional;

  public BinaryOption(Element element) {
    super(element);
    isOptional = element.elementTextTrim("optional").equals("True");
  }

  private BinaryOption() {
    super(ROOT_NAME);
    isOptional = false;
  }

  @Nonnull
  public static BinaryOption createRoot() {
    return new BinaryOption();
  }

  public boolean isOptional() {
    return isOptional;
  }

  public boolean isMandatory() {
    return !isOptional;
  }

  public boolean isRoot() {
    return isTopLevel() && !isOptional;
  }

  /**
   * Checks whether the given list of options have the same parent to decide if they all form an
   * alternative group.
   *
   * @param group A list of options that are excluded by this option.
   *
   * @return True if they are alternatives (same parent option), false otherwise
   */
  public boolean isAlternativeGroup(Collection<ConfigurationOption> group) {
    if (isOptional) {
      return false;
    }
    return group.stream().allMatch(opt -> Objects.equals(opt.parent, parent));
  }

  /**
   * Checks whether this binary option has alternative options meaning that there are other
   * binary options with the same parents, but cannot be present in the same configuration as this
   * option.
   *
   * @return True if it has alternative options, false otherwise
   */
  public boolean hasAlternatives() {
    return excludedOptions.stream().anyMatch(this::isAlternativeGroup);
  }

  /**
   * Collects all options that are excluded by this option and that have the same parent
   * (i.e., the alternative group)
   */
  @Nonnull
  public List<ConfigurationOption> collectAlternativeOptions() {
    List<ConfigurationOption> result = new ArrayList<>();
    if (isOptional) {
      return result;
    }
    for (List<ConfigurationOption> group : excludedOptions) {
      if (group.size() == 1) {
        ConfigurationOption option = group.get(0);
        if (Objects.equals(option.parent, parent)
            && ((BinaryOption) option).isMandatory()) {
          result.add(option);
        }
      }
    }
    return result;
  }

  /**
   * Collects all options that are excluded by this option, but do not have the same parent
   * (i.e., cross-tree constraints)
   */
  @Nonnull
  public Collection<List<ConfigurationOption>> getNonAlternativeExcludedOptions() {
    Collection<List<ConfigurationOption>> result = new ArrayList<>();
    for (List<ConfigurationOption> group : excludedOptions) {
      if (group.size() == 1) {
        ConfigurationOption option = group.get(0);
        if (Objects.equals(option.parent, parent)) {
          if (isOptional && ((BinaryOption) option).isOptional) {
            result.add(group);
          }
        } else {
          result.add(group);
        }
      }
    }
    return result;
  }

  /**
   * A binary option can either be selected or selected in a specific configuration of a program.
   */
  public enum BinaryValue {
    /**
     * The binary option is selected
     */
    SELECTED,
    /**
     * The binary option is deselected
     */
    DESELECTED
  }
}
