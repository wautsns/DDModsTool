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
package ddmodstool.core.conf;

import ddmodstool.core.game.base.data.localization.Language;
import ddmodstool.core.lang.awt.JFonts;
import ddmodstool.core.lang.awt.JStyle;
import ddmodstool.core.lang.util.ObV;
import java.awt.Font;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Conf for view.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class ConfView {

  public final ObV<Language> language;
  public final ObV<JStyle> style;
  public final ObV<String> fontFamily;

  // ---------------------------------------------------------------------------------------------
  // PackageConstructors
  // ---------------------------------------------------------------------------------------------

  ConfView() {
    String scale = Conf.properties.getProperty("view.scale", "1");
    System.setProperty("sun.java2d.uiScale", scale);
    Conf.properties.put("view.scale", scale);

    language = Conf.init("view.language", Language.defaults, Language::valueOf);
    style = Conf.init("view.style", JStyle.defaults, JStyle::valueOf);
    fontFamily = Conf.init("view.fontFamily", JFonts.defaultFamily);

    Conf.observerList.add(language.observe((prev, curr) -> {
      Locale.setDefault(curr.getLocale());
      JComponent.setDefaultLocale(curr.getLocale());
    }, true));
    Conf.observerList.add(style.observe((prev, curr) -> {
      Objects.requireNonNullElse(curr, JStyle.defaults).setup();
    }, true));
    Conf.observerList.add(fontFamily.observe((prev, curr) -> {
      JFonts.clear(prev);
      Font font = JFonts.get(curr, 16);
      UIDefaults ui = UIManager.getDefaults();
      ui.keySet().stream()
          .filter(obj -> (obj instanceof String key) && key.contains("font"))
          .forEach(key -> ui.put(key, font));
    }, true));
  }

}
