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

import ddmodstool.core.lang.util.ObV;
import ddmodstool.core.lang.util.Simple;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Conf for game.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class ConfGame {

  public final ObV<Path> home;

  // ---------------------------------------------------------------------------------------------
  // PackageConstructors
  // ---------------------------------------------------------------------------------------------

  ConfGame() {
    home = Simple.init(() -> {
      ObV<Path> home = Conf.init("game.home", (Path) null);
      Path path = home.get();
      if ((path != null) && !Files.exists(path)) {
        home.set(null);
      }
      return home;
    });
  }

}
