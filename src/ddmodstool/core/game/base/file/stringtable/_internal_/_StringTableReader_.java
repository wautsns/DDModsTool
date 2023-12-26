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
package ddmodstool.core.game.base.file.stringtable._internal_;

import ddmodstool.core.game.base.data.localization.LocX;
import ddmodstool.core.game.base.file.stringtable.StringTable;
import ddmodstool.core.game.base.file.stringtable.StringTable.EntryMap;
import ddmodstool.core.game.base.file.stringtable.StringTableFilter;
import ddmodstool.core.lang.util.IO;
import ddmodstool.core.lang.util.Simple;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * [ *.string_table.xml ] reader.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class _StringTableReader_ extends DefaultHandler implements Closeable {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static StringTable read(Path path, StringTableFilter filter) {
    Path absPath = path.toAbsolutePath();
    try (_StringTableReader_ reader = new _StringTableReader_(absPath, filter)) {
      return reader.result;
    } catch (Exception e) {
      throw LocX.of(e).with("StringTable#path", absPath);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private final StringTable result = Simple.init(() -> {
    StringTable stringTable = new StringTable();
    stringTable.setEntryMapMap(new LinkedHashMap<>());
    return stringTable;
  });

  private final InputStream input;
  private final Predicate<String> languageIdFilter;
  private final Predicate<String> entryIdFilter;
  private final Predicate<String> entryCdataFilter;

  private EntryMap entryMap;
  private String entryId;

  // ---------------------------------------------------------------------------------------------
  // @Override PublicMethods, DefaultHandler
  // ---------------------------------------------------------------------------------------------

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    if (qName.equals("entry") && (entryMap != null)) {
      String id = attributes.getValue("id");
      if ((id != null) && entryIdFilter.test(id)) {
        entryId = id;
      }
    } else if (qName.equals("language")) {
      String id = attributes.getValue("id");
      if ((id != null) && languageIdFilter.test(id)) {
        entryMap = result.getEntryMap(id);
        if (entryMap == null) {
          entryMap = new EntryMap();
          entryMap.setLanguageId(id);
          entryMap.setCdataListMap(new LinkedHashMap<>());
          result.reqEntryMapMap().put(entryMap.getLanguageId(), entryMap);
        }
      }
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) {
    if (qName.equals("entry")) {
      entryId = null;
    } else if (qName.equals("language")) {
      entryMap = null;
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    if (entryId == null) {
      return;
    }
    String cdata = new String(ch, start, length);
    if (!entryCdataFilter.test(cdata)) {
      return;
    }
    List<String> cdataList = entryMap.getCdataList(entryId);
    if (cdataList == null) {
      cdataList = List.of(cdata);
      entryMap.reqCdataListMap().put(entryId, cdataList);
    } else {
      if (cdataList.size() == 1) {
        String prev = cdataList.get(0);
        cdataList = new ArrayList<>(2);
        cdataList.add(prev);
        entryMap.reqCdataListMap().put(entryId, cdataList);
      }
      cdataList.add(cdata);
      if (cdataList.size() >= 4) {
        ((ArrayList<?>) cdataList).trimToSize();
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // @Override PublicMethods, Closeable
  // ---------------------------------------------------------------------------------------------

  @Override
  public void close() throws IOException {
    if (input != null) {
      input.close();
    }
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateConstructors
  // ---------------------------------------------------------------------------------------------

  private _StringTableReader_(Path path, StringTableFilter filter) {
    result.setPath(path);
    this.input = IO.input(path);
    Predicate<String> returnTrueDirectly = ignored -> true;
    if (filter == null) {
      this.languageIdFilter = returnTrueDirectly;
      this.entryIdFilter = returnTrueDirectly;
      this.entryCdataFilter = returnTrueDirectly;
    } else {
      if (filter.getForLanguageId() == null) {
        this.languageIdFilter = returnTrueDirectly;
      } else {
        this.languageIdFilter = filter.getForLanguageId();
      }
      if (filter.getForEntryId() == null) {
        this.entryIdFilter = returnTrueDirectly;
      } else {
        this.entryIdFilter = filter.getForEntryId();
      }
      if (filter.getForEntryCdata() == null) {
        this.entryCdataFilter = returnTrueDirectly;
      } else {
        this.entryCdataFilter = filter.getForEntryCdata();
      }
    }
    // --- do read ---
    IO.scanXml(path, this);
  }

}
