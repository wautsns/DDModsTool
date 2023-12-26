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

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JavaAWT tool tip text.
 *
 * @author wautsns
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JToolTipText {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static void set(JLabel label) {
    label.setToolTipText(label.getText());
  }

  public static void set(JTextComponent textComponent) {
    textComponent.setToolTipText(textComponent.getText());
  }

  public static void set(AbstractButton button) {
    button.setToolTipText(button.getText());
  }

  public static void set(JComboBox<?> comboBox) {
    Object selectedItem = comboBox.getSelectedItem();
    if (selectedItem == null) {
      comboBox.setToolTipText(null);
    } else {
      comboBox.setToolTipText(selectedItem.toString());
    }
  }

}
