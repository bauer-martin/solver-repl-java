package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import commands.LoadVMCommand;

public final class Main {

  private Main() {
  }

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public static void main(String... args) throws IOException {
    BufferedReader in = new BufferedReader(
        new InputStreamReader(System.in, Charset.forName("UTF-8")));
    Shell shell = new Shell(in, System.out);
    GlobalContext context = new GlobalContext();
    shell.registerCommand(new LoadVMCommand(context), "load-vm");
    shell.execute();
  }
}
