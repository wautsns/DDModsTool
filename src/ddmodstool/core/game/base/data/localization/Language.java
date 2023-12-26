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

import ddmodstool.core.lang.util.Simple;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Language enumeration.
 *
 * @author wautsns
 * @since 1.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Language {

  schinese(Locale.SIMPLIFIED_CHINESE),

  english(Locale.ENGLISH),

  ;

  public static final Language defaults = Simple.init(() -> {
    Language[] valueArray = values();
    Locale defaultLocale = Locale.getDefault();
    for (Language value : valueArray) {
      if (value.locale.equals(defaultLocale)) {
        return value;
      }
    }
    String defaultLanguage = defaultLocale.getLanguage();
    for (Language value : valueArray) {
      if (value.locale.getLanguage().equals(defaultLanguage)) {
        return value;
      }
    }
    return Language.schinese;
  });

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private final @Getter Locale locale;

  private final String locEntryId = format("Language[%s]", name());

  // ---------------------------------------------------------------------------------------------
  // @Override PublicMethods, Object
  // ---------------------------------------------------------------------------------------------

  @Override
  public String toString() {
    return Loc.G.text(locEntryId);
  }

}
