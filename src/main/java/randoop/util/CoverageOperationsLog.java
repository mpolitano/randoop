package randoop.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import org.checkerframework.checker.formatter.qual.FormatMethod;

import randoop.main.GenInputsAbstract;
import randoop.main.RandoopBug;

/** Static methods that log to GenInputsAbstract.operations_log, if that is non-null. */
public final class CoverageOperationsLog {

  private CoverageOperationsLog() {
    throw new IllegalStateException("no instance");
  }

  public static boolean isLoggingOn() {
    return GenInputsAbstract.operations_log != null;
  }

  /**
   * Log using {@code String.format} to GenInputsAbstract.operations_log, if that is non-null.
   *
   * @param fmt the format string
   * @param args arguments to the format string
   */
  @FormatMethod
  public static void logPrintf(String fmt, Object... args) {
    if (!isLoggingOn()) {
      return;
    }

    String msg;
    try {
      msg = String.format(fmt, args);
    } catch (Throwable t) {
      logPrintf("A user-defined toString() method failed.%n");
      Class<?>[] argTypes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        argTypes[i] = args[i].getClass();
      }
      logPrintf("  fmt = %s%n", fmt);
      logPrintf("  arg types = %s%n", Arrays.toString(argTypes));
      logStackTrace(t);
      return;
    }

    try {
      GenInputsAbstract.operations_log.write(msg);
      GenInputsAbstract.operations_log.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to log", e);
    }
  }

  /**
   * Log to GenInputsAbstract.operations_log, if that is non-null.
   *
   * @param t the Throwable whose stack trace to operations_log
   */
  public static void logStackTrace(Throwable t) {
    if (!isLoggingOn()) {
      return;
    }

    try {
      // Gross, GenInputsAbstract.operations_log should be a writer instead of a FileWriter
      PrintWriter pw = new PrintWriter(GenInputsAbstract.operations_log);
      t.printStackTrace(pw);
      pw.flush();
      GenInputsAbstract.operations_log.flush();
    } catch (IOException e) {
      throw new RandoopBug("Exception while writing to log", e);
    }
  }
}