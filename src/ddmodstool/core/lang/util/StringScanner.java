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
package ddmodstool.core.lang.util;

import ddmodstool.core.lang.function.CharPredicate;
import lombok.Getter;

/**
 * String scanner.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class StringScanner {

  private final String string;
  private @Getter int cursor;

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public boolean hasNext() {
    return (cursor < string.length());
  }

  public boolean hasNext(char c) {
    return hasNext() && (look() == c);
  }

  public boolean hasNextCrlf() {
    return hasNext() && isCrlf(look());
  }

  // ---------------------------------------------------------------------------------------------

  public char look() {
    return string.charAt(cursor);
  }

  public String near(int prev, int next) {
    return string.substring(Math.max(0, cursor - prev), Math.max(string.length(), cursor + next));
  }

  // ---------------------------------------------------------------------------------------------

  public char take() {
    return string.charAt(cursor++);
  }

  public String takeWhile(CharPredicate predicate) {
    StringBuilder bu = new StringBuilder();
    while (hasNext()) {
      char c = look();
      if (predicate.test(c)) {
        bu.append(take());
      } else {
        break;
      }
    }
    return bu.toString();
  }

  public String takeWhileNotWhitespace() {
    return takeWhile(c -> !Character.isWhitespace(c));
  }

  // ---------------------------------------------------------------------------------------------

  public StringScanner drop() {
    take();
    return this;
  }

  public StringScanner dropWhile(CharPredicate predicate) {
    while (hasNext() && predicate.test(look())) {
      cursor++;
    }
    return this;
  }

  public StringScanner dropWhileWhitespace() {
    return dropWhile(Character::isWhitespace);
  }

  public StringScanner dropWhileNotCrlf() {
    return dropWhile(c -> !isCrlf(c));
  }

  public StringScanner dropWhileWhitespaceNotCrlf() {
    return dropWhile(c -> Character.isWhitespace(c) && !isCrlf(c));
  }

  // ---------------------------------------------------------------------------------------------
  // PublicConstructors
  // ---------------------------------------------------------------------------------------------

  public StringScanner(String string) {
    this.string = string;
    this.cursor = 0;
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateStaticMethods
  // ---------------------------------------------------------------------------------------------

  private static boolean isCrlf(char c) {
    return (c == '\n') || (c == '\r');
  }

}
