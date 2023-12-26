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
package ddmodstool.view;

import ddmodstool.core.conf.Conf;
import ddmodstool.view.conf.InitialSetupWizardFrame;
import ddmodstool.view.main.MainFrame;

/**
 * DDModsTool view.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class DDModsToolView {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static void main(String[] args) {
    if (Conf.okay()) {
      new MainFrame();
    } else {
      new InitialSetupWizardFrame();
    }
  }

}
