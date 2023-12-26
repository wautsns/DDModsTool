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
package ddmodstool.core.game.base.file.darkest._internal_;

import static java.lang.String.format;

import ddmodstool.core.game.base.data.localization.Loc;
import ddmodstool.core.game.base.data.localization.LocX;
import ddmodstool.core.game.base.file.darkest.Darkest;
import ddmodstool.core.game.base.file.darkest.Darkest.Item;
import ddmodstool.core.game.base.file.darkest.Darkest.Line;
import ddmodstool.core.lang.util.IO;
import ddmodstool.core.lang.util.Simple;
import ddmodstool.core.lang.util.StringScanner;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * [ *.darkest ] reader.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class _DarkestReader_ {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static Darkest read(Path path) {
    Path absPath = path.toAbsolutePath();
    try {
      return new _DarkestReader_(absPath).result;
    } catch (Exception e) {
      throw LocX.of(e).with("Darkest#path", absPath);
    }
  }

  // ---------------------------------------------------------------------------------------------

  private static final String FLAG_LINE_TYPE = "LINE_TYPE";
  private static final String FLAG_ITEM_NAME = "ITEM_NAME";
  private static final String FLAG_ITEM_VALUE = "ITEM_VALUE";
  private static final String FLAG_ITEM_VALUE_TEXT = "ITEM_VALUE_TEXT";
  private static final String FLAG_AFTER_ITEM_VALUE = "AFTER_ITEM_VALUE";

  private static final String FLAG_INIT = FLAG_LINE_TYPE;
  private static final String FLAG_OKAY = "OKAY";
  private static final String FLAG_FAIL = "FAIL";

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private final Darkest result = Simple.init(() -> {
    Darkest result = new Darkest();
    result.setLineListMap(new LinkedHashMap<>());
    return result;
  });

  private final StringScanner scanner;

  private String flag;
  private Line line;
  private Item item;

  // ---------------------------------------------------------------------------------------------
  // PrivateMethods
  // ---------------------------------------------------------------------------------------------

  private String readAndSetLineType(boolean returnFlagOnInvalidCharDetected) {
    if (!scanner.dropWhileWhitespace().hasNext()) {
      return FLAG_OKAY;
    }
    if (scanner.hasNext('/')) {
      scanner.dropWhileNotCrlf();
      return flag;
    }
    if (!isIdentifierChar(scanner.look())) {
      if (returnFlagOnInvalidCharDetected) {
        return FLAG_FAIL;
      }
      throw LocX.of("Darkest!InvalidContent")
          .with("txt[PositionOfInvalidChar]", scanner.getCursor())
          .with("txt[Expect]", Loc.G.text("Darkest#lineType"))
          .with("txt[Actual]", scanner.near(0, 20));
    }
    String type = scanner.takeWhile(_DarkestReader_::isIdentifierChar);
    scanner.dropWhile(c -> c != ':').drop();
    line = new Line();
    line.setType(type);
    line.setItemMap(new LinkedHashMap<>());
    result.reqLineListMap().computeIfAbsent(type, ignored -> new ArrayList<>()).add(line);
    return FLAG_ITEM_NAME;
  }

  private String readAndSetItemName(boolean returnFlagOnInvalidCharDetected) {
    if (!scanner.dropWhileWhitespace().hasNext()) {
      return FLAG_OKAY;
    }
    if (scanner.hasNext('/')) {
      scanner.dropWhileNotCrlf();
      return flag;
    }
    if (!scanner.hasNext('.')) {
      if (returnFlagOnInvalidCharDetected) {
        return FLAG_FAIL;
      }
      throw LocX.of("Darkest!InvalidContent")
          .with("txt[PositionOfInvalidChar]", scanner.getCursor())
          .with("txt[Expect]", Loc.G.text("Darkest#itemName"))
          .with("txt[Actual]", scanner.near(0, 20));
    }
    String name = scanner.drop().takeWhile(_DarkestReader_::isIdentifierChar);
    item = new Item();
    item.setName(name);
    item.setValueList(new ArrayList<>());
    if (line.reqItemMap().put(item.reqName(), item) != null) {
      throw LocX.of("Darkest!ConflictingItems")
          .with("Darkest#path", result.getPath())
          .with("Darkest#lineType", line.getType())
          .with("Darkest#itemName", item.getName());
    }
    return FLAG_ITEM_VALUE;
  }

  private String readAndSetItemValue() {
    if (scanner.dropWhileWhitespaceNotCrlf().hasNextCrlf()) {
      return FLAG_AFTER_ITEM_VALUE;
    }
    if (!scanner.dropWhileWhitespace().hasNext()) {
      return FLAG_OKAY;
    }
    if (scanner.hasNext('/')) {
      scanner.dropWhileNotCrlf();
      return flag;
    }
    if (scanner.hasNext('"')) {
      return FLAG_ITEM_VALUE_TEXT;
    } else if (scanner.hasNext('.')) {
      return FLAG_ITEM_NAME;
    } else {
      String value = scanner.takeWhileNotWhitespace();
      item.reqValueList().add(value);
      return FLAG_AFTER_ITEM_VALUE;
    }
  }

  private String readAndSetItemValueText() {
    if (scanner.hasNext('"')) {
      scanner.drop();
      StringBuilder bu = new StringBuilder();
      boolean escaped = false;
      while (scanner.hasNext()) {
        char c = scanner.take();
        if (escaped) {
          bu.append(c);
          escaped = false;
        } else if (c == '\\') {
          escaped = true;
        } else if (c == '"') {
          String value = bu.toString();
          item.reqValueList().add(value);
          return FLAG_AFTER_ITEM_VALUE;
        } else {
          bu.append(c);
        }
      }
    }
    throw LocX.of("Darkest!InvalidContent")
        .with("txt[PositionOfInvalidChar]", scanner.getCursor())
        .with("txt[Expect]", "\"")
        .with("txt[Actual]", scanner.hasNext() ? scanner.near(0, 20) : "<EOF>");
  }

  private String readAndSetAfterItemValue() {
    scanner.dropWhileWhitespaceNotCrlf();
    if (!scanner.hasNext()) {
      return FLAG_OKAY;
    }
    if (scanner.hasNext('/')) {
      scanner.dropWhileNotCrlf();
      return flag;
    }
    boolean hasNextLineSeparator = scanner.hasNextCrlf();
    String flagToReturn = readAndSetItemName(true);
    if (flagToReturn.equals(FLAG_FAIL)) {
      if (hasNextLineSeparator) {
        flagToReturn = readAndSetLineType(true);
        if (flagToReturn.equals(FLAG_FAIL)) {
          throw LocX.of("Darkest!InvalidContent")
              .with("txt[PositionOfInvalidChar]", scanner.getCursor())
              .with("txt[Expect]", format("%s|%s",
                  Loc.G.text("Darkest#lineType"), Loc.G.text("Darkest#itemName")))
              .with("txt[Actual]", scanner.near(0, 20));
        }
      } else {
        flagToReturn = readAndSetItemValue();
        if (flagToReturn.equals(FLAG_FAIL)) {
          throw LocX.of("Darkest!InvalidContent")
              .with("txt[PositionOfInvalidChar]", scanner.getCursor())
              .with("txt[Expect]", format("%s|%s",
                  Loc.G.text("Darkest#itemName"), Loc.G.text("Darkest#itemValue")))
              .with("txt[Actual]", scanner.near(0, 20));
        }
      }
    }
    return flagToReturn;
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateConstructors
  // ---------------------------------------------------------------------------------------------

  private _DarkestReader_(Path path) {
    result.setPath(path);
    this.scanner = new StringScanner(IO.readString(path));
    this.flag = FLAG_INIT;
    // --- do read ---
    while (!FLAG_OKAY.equals(flag)) {
      flag = switch (flag) {
        case FLAG_LINE_TYPE -> readAndSetLineType(false);
        case FLAG_ITEM_NAME -> readAndSetItemName(false);
        case FLAG_ITEM_VALUE -> readAndSetItemValue();
        case FLAG_ITEM_VALUE_TEXT -> readAndSetItemValueText();
        case FLAG_AFTER_ITEM_VALUE -> readAndSetAfterItemValue();
        default -> throw LocX.of("Darkest!InvalidContent")
            .with("txt[PositionOfInvalidChar]", scanner.getCursor())
            .with("txt[Actual]", scanner.near(10, 20));
      };
    }
    for (List<Line> lineList : result.reqLineListMap().values()) {
      ((ArrayList<?>) lineList).trimToSize();
      lineList.stream().flatMap(line -> line.reqItemMap().values().stream()).forEach(item -> {
        ((ArrayList<?>) item.reqValueList()).trimToSize();
      });
    }
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateStaticMethods ( & Fields )
  // ---------------------------------------------------------------------------------------------

  private static final char[] IDENTIFIER_CHAR_ARRAY = Simple.init(() -> {
    String string = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    char[] charArray = string.toCharArray();
    Arrays.sort(charArray);
    return charArray;
  });

  // ---------------------------------------------------------------------------------------------

  private static boolean isIdentifierChar(char c) {
    return (Arrays.binarySearch(IDENTIFIER_CHAR_ARRAY, c) >= 0);
  }

}
