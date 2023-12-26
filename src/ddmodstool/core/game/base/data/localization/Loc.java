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

import static java.lang.String.format;

import ddmodstool.core.conf.Conf;
import ddmodstool.core.game.base.file.stringtable.StringTable;
import ddmodstool.core.game.base.file.stringtable.StringTable.EntryMap;
import ddmodstool.core.game.base.file.stringtable.StringTableFilter;
import ddmodstool.core.lang.util.IO;
import ddmodstool.core.lang.util.ObV;
import ddmodstool.core.lang.util.Simple;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 * Localization.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class Loc {

  public static final Loc G = Simple.init(() -> {
    Loc loc = new Loc(null, Conf.view.language);
    loc.walkAndRead(Conf.home.resolve("res/localization"));
    return loc;
  });

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  @SuppressWarnings({"FieldCanBeLocal", "unused"})
  private final Object languageObserver; // strongly referenced by this instance

  private final Loc parent;
  // absolutePathString -> pathThatWasRead
  private final Map<String, Path> pathMap = new LinkedHashMap<>();
  private final @Getter EntryMap entryMap = Simple.init(() -> {
    EntryMap entryMap = new EntryMap();
    entryMap.setCdataListMap(new LinkedHashMap<>());
    return entryMap;
  });

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public int count(String entryId) {
    List<String> cdataList = entryMap.getCdataList(entryId);
    return (cdataList == null) ? 0 : cdataList.size();
  }

  // ---------------------------------------------------------------------------------------------

  public String text(String entryId) {
    return text(entryId, (_entryId, cdata) -> {
      if (cdata == null) {
        return format("[<%s>]", _entryId);
      }
      return replaceAllColours(cdata, matcher -> matcher.group(2));
    });
  }

  // ---------------------------------------------------------------------------------------------

  public void read(Path path) {
    Simple.lock(lock.writeLock(), () -> {
      Path absPath = path.toAbsolutePath();
      String absPathStr = absPath.toString();
      Map<String, List<String>> cdataListMap = entryMap.reqCdataListMap();
      Map<String, Path> pathToReadMap;
      Path readPath = pathMap.remove(absPathStr);
      if (readPath == null) {
        pathToReadMap = Map.of(absPathStr, absPath);
      } else {
        pathToReadMap = new LinkedHashMap<>(pathMap.size() + 1);
        pathToReadMap.putAll(pathMap);
        pathToReadMap.put(absPathStr, absPath);
        pathMap.clear();
        cdataListMap.clear();
      }
      String languageId = entryMap.reqLanguageId();
      StringTableFilter filter = new StringTableFilter();
      filter.setForLanguageId(languageId::equals);
      pathToReadMap.forEach((pathStrToRead, pathToRead) -> {
        StringTable table = StringTable.read(pathToRead, filter);
        EntryMap entryMap = table.getEntryMap(languageId);
        if (entryMap != null) {
          cdataListMap.putAll(entryMap.reqCdataListMap());
        }
        pathMap.put(pathStrToRead, pathToRead);
      });
    });
  }

  public void walkAndRead(Path start) {
    Simple.lock(lock.writeLock(), () -> IO.walkExt(start, ".string_table.xml", this::read));
  }

  // ---------------------------------------------------------------------------------------------
  // PublicConstructors
  // ---------------------------------------------------------------------------------------------

  public Loc(Loc parent, ObV<Language> language) {
    this.parent = parent;
    this.languageObserver = language.observe((prev, curr) -> {
      Simple.lock(lock.writeLock(), () -> {
        entryMap.setLanguageId(curr.name());
        entryMap.reqCdataListMap().clear();
        List<Path> pathToReadList = new ArrayList<>(pathMap.values());
        pathMap.clear();
        pathToReadList.forEach(this::read);
      });
    }, true);
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateMethods
  // ---------------------------------------------------------------------------------------------

  private String text(String entryId, BinaryOperator<String> cdataAction) {
    return Simple.lock(lock.readLock(), () -> {
      List<String> cdataList = entryMap.getCdataList(entryId);
      if (cdataList == null) {
        if (parent == null) {
          return cdataAction.apply(entryId, null);
        } else {
          return parent.text(entryId, cdataAction);
        }
      } else if (cdataList.size() == 1) {
        return cdataAction.apply(entryId, cdataList.get(0));
      } else {
        int n = cdataList.size();
        int i = ThreadLocalRandom.current().nextInt(n);
        String text = cdataAction.apply(entryId, cdataList.get(i));
        return format("%s ( %d/%d )", text, i + 1, n);
      }
    });
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateStaticMethods ( & Fields )
  // ---------------------------------------------------------------------------------------------

  private static final Pattern COLOUR_PATTERN = Pattern.compile(
      "\\{colour_start\\|([^}]+)}(.*?)\\{colour_end}");

  // ---------------------------------------------------------------------------------------------

  private static String replaceAllColours(String cdata, Function<MatchResult, String> replacer) {
    if (cdata.contains("{colour_")) {
      return COLOUR_PATTERN.matcher(cdata).replaceAll(replacer);
    }
    return cdata;
  }

}
