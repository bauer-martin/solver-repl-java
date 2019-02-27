package commands;

import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;

import spl_conqueror.VariabilityModel;
import utilities.GlobalContext;
import utilities.ShellCommand;
import utilities.XMLUtils;

public final class LoadVMCommand extends ShellCommand {

  public LoadVMCommand(GlobalContext context) {
    super(context);
  }

  @Nonnull
  @Override
  public String execute(String argsString) {
    try {
      Path path = Paths.get(argsString);
      Element element = XMLUtils.loadXML(path);
      VariabilityModel variabilityModel = new VariabilityModel(element);
      context.setVariabilityModel(variabilityModel);
      return DEFAULT_SUCCESS_RESPONSE;
    } catch (DocumentException ex) {
      return error("unable to load model: " + ex.getMessage());
    }
  }
}
