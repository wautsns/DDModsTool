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
package ddmodstool.core.lang.function;

import java.util.function.Consumer;

/**
 * ConsumerT.
 *
 * @param <T> the type of input
 * @author wautsns
 * @since 1.0.0
 */
@FunctionalInterface
public interface ConsumerT<T> extends Consumer<T> {

  void acceptT(T t) throws Exception;

  // ---------------------------------------------------------------------------------------------
  // @Override PublicMethods, Consumer
  // ---------------------------------------------------------------------------------------------

  @Override
  default void accept(T t) {
    try {
      acceptT(t);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
