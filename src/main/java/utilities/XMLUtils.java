package utilities;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.net.URL;

import javax.annotation.Nonnull;

public final class XMLUtils {

  private XMLUtils() {
  }

  @Nonnull
  public static Element loadXML(URL url) throws DocumentException {
    SAXReader reader = new SAXReader();
    Document document = reader.read(url);
    return document.getRootElement();
  }
}
