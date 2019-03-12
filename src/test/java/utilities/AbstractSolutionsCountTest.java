package utilities;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;

import spl_conqueror.VariabilityModel;

@SuppressWarnings("JUnitTestMethodWithNoAssertions")
public abstract class AbstractSolutionsCountTest {

  protected abstract int countSolutions(VariabilityModel vm);

  @Test
  void testApacheEnergy() {
    testCase("Apache_energy.xml", 580);
  }

  private void testCase(String featureModel, int expectedSolutionCount) {
    URL url = ClassLoader.getSystemResource("feature-models/" + featureModel);
    Element rootElement;
    try {
      rootElement = XMLUtils.loadXML(url);
    } catch (DocumentException ex) {
      Assertions.fail(ex);
      return;
    }
    VariabilityModel vm = new VariabilityModel(rootElement);
    int solutionCount = countSolutions(vm);
    Assertions.assertEquals(expectedSolutionCount, solutionCount);
  }

  @Test
  void testBerkeleyDBC() {
    testCase("BerkeleyDBC.xml", 2560);
  }

  @Test
  void testBrotli() {
    testCase("brotli.xml", 180);
  }

  @Test
  void testDuneBin() {
    testCase("Dune_bin.xml", 2304);
  }

  @Test
  void testExastencilsEnergy() {
    testCase("exastencils_energy.xml", 86058);
  }

  @Test
  void testHipaccBin() {
    testCase("Hipacc_bin.xml", 13485);
  }

  @Test
  void testHSQLDBEnergy() {
    testCase("HSQLDB_energy.xml", 864);
  }

  @Test
  void testLLVM() {
    testCase("LLVM.xml", 1024);
  }

  @Test
  void testLLVMEnergy() {
    testCase("LLVM_energy.xml", 65536);
  }
}
