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

import static java.lang.String.format;

import ddmodstool.core.game.base.data.localization.Loc;
import javax.swing.UIManager;
import lombok.RequiredArgsConstructor;

/**
 * JavaAWT style.
 *
 * @author wautsns
 * @since 1.0.0
 */
@RequiredArgsConstructor
public enum JStyle {

  FlatLight("com.formdev.flatlaf.FlatLightLaf"),
  FlatIntelliJ("com.formdev.flatlaf.FlatIntelliJLaf"),
  FlatMacLight("com.formdev.flatlaf.themes.FlatMacLightLaf"),
  FlatDark("com.formdev.flatlaf.FlatDarkLaf"),
  FlatDarcula("com.formdev.flatlaf.FlatDarculaLaf"),
  FlatMacDark("com.formdev.flatlaf.themes.FlatMacDarkLaf"),

  Metal("javax.swing.plaf.metal.MetalLookAndFeel"),
  Nimbus("javax.swing.plaf.nimbus.NimbusLookAndFeel"),

  ;

  public static final JStyle defaults = JStyle.FlatLight;

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private final String className;

  private final String locEntryId = format("JStyle[%s]", name());

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public void setup() {
    try {
      UIManager.setLookAndFeel(className);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // @Override PublicMethods, Object
  // ---------------------------------------------------------------------------------------------

  @Override
  public String toString() {
    return Loc.G.text(locEntryId);
  }

}
