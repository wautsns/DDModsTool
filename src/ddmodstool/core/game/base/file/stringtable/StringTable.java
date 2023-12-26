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
package ddmodstool.core.game.base.file.stringtable;

import ddmodstool.core.game.base.data.localization.LocX;
import ddmodstool.core.game.base.file.stringtable._internal_._StringTableReader_;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * [ *.string_table.xml ] object.
 *
 * @author wautsns
 * @since 1.0.0
 */
@Getter
@Setter
public final class StringTable {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static StringTable read(Path path, StringTableFilter filter) {
    return _StringTableReader_.read(path, filter);
  }

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private Path path;
  // languageId -> entryMap
  private Map<String, EntryMap> entryMapMap;

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public Path reqPath() {
    Path path = this.path;
    if (path == null) {
      throw LocX.of("StringTable!Uninitialized");
    }
    return path;
  }

  public Map<String, EntryMap> reqEntryMapMap() {
    Map<String, EntryMap> entryMapMap = this.entryMapMap;
    if (entryMapMap == null) {
      throw LocX.of("StringTable!Uninitialized");
    }
    return entryMapMap;
  }

  // ---------------------------------------------------------------------------------------------

  public EntryMap getEntryMap(String languageId) {
    return reqEntryMapMap().get(languageId);
  }

  // ---------------------------------------------------------------------------------------------

  public EntryMap reqEntryMap(String languageId) {
    EntryMap entryMap = getEntryMap(languageId);
    if (entryMap == null) {
      throw LocX.of("StringTable!MissingLanguage")
          .with("StringTable#languageId", languageId);
    }
    return entryMap;
  }

  // ---------------------------------------------------------------------------------------------
  // PublicStaticClasses
  // ---------------------------------------------------------------------------------------------

  @Getter
  @Setter
  public static final class EntryMap {

    private String languageId;
    // entryId -> cdataList
    private Map<String, List<String>> cdataListMap;

    // -----------------------------------------------------------------------------------------
    // PublicMethods
    // -----------------------------------------------------------------------------------------

    public String reqLanguageId() {
      String languageId = this.languageId;
      if (languageId == null) {
        throw LocX.of("StringTable!Uninitialized");
      }
      return languageId;
    }

    public Map<String, List<String>> reqCdataListMap() {
      Map<String, List<String>> cdataListMap = this.cdataListMap;
      if (cdataListMap == null) {
        throw LocX.of("StringTable!Uninitialized");
      }
      return cdataListMap;
    }

    // -----------------------------------------------------------------------------------------

    public String getCdata(String entryId) {
      List<String> cdata = getCdataList(entryId);
      if (cdata == null) {
        return null;
      }
      if (cdata.size() != 1) {
        throw LocX.of("StringTable!UnexpectedSizeOfEntryList")
            .with("StringTable#entryId", entryId)
            .with("txt[Expect]", 1)
            .with("txt[Actual]", cdata.size());
      }
      return cdata.get(0);
    }

    public List<String> getCdataList(String entryId) {
      return reqCdataListMap().get(entryId);
    }

    // -----------------------------------------------------------------------------------------

    public String reqCdata(String entryId) {
      String cdata = getCdata(entryId);
      if (cdata == null) {
        throw LocX.of("StringTable!MissingEntry")
            .with("StringTable#entryId", entryId);
      }
      return cdata;
    }

    public List<String> reqCdataList(String entryId) {
      List<String> cdataList = getCdataList(entryId);
      if (cdataList == null) {
        throw LocX.of("StringTable!MissingEntry")
            .with("StringTable#entryId", entryId);
      }
      return cdataList;
    }

  }

}
