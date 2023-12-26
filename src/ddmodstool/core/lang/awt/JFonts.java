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
package ddmodstool.core.lang.awt;

import ddmodstool.core.conf.Conf;
import java.awt.Font;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JavaAWT fonts.
 *
 * @author wautsns
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JFonts {

  public static final String defaultFamily = "Monospaced";

  // family -> size -> font
  private static final Map<String, Map<Integer, Font>> cache = new ConcurrentHashMap<>();

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static Font get(int size) {
    return get(Conf.view.fontFamily.get(), size);
  }

  public static Font get(String family, int size) {
    String _family = Objects.requireNonNullElse(family, defaultFamily);
    return cache.computeIfAbsent(_family, ignored -> new ConcurrentHashMap<>())
        .computeIfAbsent(size, _size -> new Font(_family, Font.PLAIN, _size));
  }

  // ---------------------------------------------------------------------------------------------

  public static void clear(String family) {
    cache.remove(Objects.requireNonNullElse(family, defaultFamily));
  }

  // ---------------------------------------------------------------------------------------------

  public static int width(JLabel label) {
    return width(label, label.getText());
  }

  public static int width(JComponent component, String text) {
    return (text == null) ? 0 : component.getFontMetrics(component.getFont()).stringWidth(text);
  }

}
