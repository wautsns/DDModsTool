/*
 *  Copyright (C) 2023 the original author or authors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ddmodstool.core.game.base.data.localization;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loc exception.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class LocX extends RuntimeException {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static LocX of(Throwable cause) {
    return new LocX(cause);
  }

  public static LocX of(String messageEntryId) {
    return new LocX(Loc.G.text(messageEntryId));
  }

  public static LocX ofRaw(String message) {
    return new LocX(message);
  }

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private final List<Map.Entry<String, String>> context = new LinkedList<>();

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public LocX with(String nameEntryId, Object value) {
    return withRaw(Loc.G.text(nameEntryId), value);
  }

  public LocX withRaw(String name, Object value) {
    String valueStr = (value == null) ? null : value.toString();
    context.add(new AbstractMap.SimpleImmutableEntry<>(name, valueStr));
    return this;
  }

  // ---------------------------------------------------------------------------------------------
  // @Override PublicMethods, RuntimeException
  // ---------------------------------------------------------------------------------------------

  @Override
  public String getLocalizedMessage() {
    String ln = System.lineSeparator();
    StringBuilder bu = new StringBuilder();
    Throwable thrown = this;
    while (true) {
      bu.append(thrown.getClass().getSimpleName());
      if (thrown instanceof LocX x) {
        bu.append(": ").append(x.getMessage());
        for (Map.Entry<String, String> entry : x.context) {
          bu.append(ln).append('\t').append(entry.getKey()).append(": ").append(entry.getValue());
        }
      } else {
        String message = thrown.getLocalizedMessage();
        if (message != null) {
          bu.append(": ").append(message);
        }
      }
      thrown = thrown.getCause();
      if (thrown != null) {
        bu.append(ln);
      } else {
        return bu.toString();
      }
    }
  }

  @Override
  public void printStackTrace(PrintStream stream) {
    try {
      internalPrintStackTrace(stream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void printStackTrace(PrintWriter writer) {
    try {
      internalPrintStackTrace(writer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // PublicConstructors
  // ---------------------------------------------------------------------------------------------

  private LocX(String message) {
    super(message, null);
  }

  private LocX(Throwable cause) {
    super(Loc.G.text("txt[PleaseSeeBelow]"), cause);
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateMethods
  // ---------------------------------------------------------------------------------------------

  private void internalPrintStackTrace(Appendable appender) throws IOException {
    String ln = System.lineSeparator();
    Set<StackTraceElement> printed = new HashSet<>();
    for (Throwable thrown = this; thrown != null; thrown = thrown.getCause()) {
      StackTraceElement[] stack = thrown.getStackTrace();
      if (!printed.isEmpty()) {
        appender.append(ln).append("Caused by: ");
      }
      appender.append(thrown.getClass().getName()).append(": ");
      StackTraceElement element0 = stack[0];
      appender.append("Failed to invoke `").append(element0.getClassName()).append("#")
          .append(element0.getMethodName()).append("(...)`");
      String message = thrown.getMessage();
      if (message != null) {
        appender.append(" => ").append(message);
      }
      appender.append(ln).append("\tat ").append(element0.toString());
      printed.add(element0);
      if (thrown instanceof LocX x) {
        for (Map.Entry<String, String> entry : x.context) {
          appender.append(ln).append("\t\twith <").append(entry.getKey())
              .append("> = ").append(entry.getValue());
        }
      }
      for (int i = 1, n = stack.length; i < n; i++) {
        StackTraceElement element = stack[i];
        appender.append(ln);
        if (printed.add(element)) {
          appender.append("\tat ").append(element.toString());
        } else {
          appender.append("\t... ").append(Integer.toString(n - i)).append(" more");
          break;
        }
      }
    }
    appender.append(ln);
  }

}
