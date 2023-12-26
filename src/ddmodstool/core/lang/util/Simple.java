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
package ddmodstool.core.lang.util;

import ddmodstool.core.lang.function.RunnableT;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Simple.
 *
 * @author wautsns
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Simple {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static <T> T init(Supplier<T> supplier) {
    return supplier.get();
  }

  // ---------------------------------------------------------------------------------------------

  public static void lock(Lock lock, RunnableT action) {
    lock.lock();
    try {
      action.run();
    } finally {
      lock.unlock();
    }
  }

  public static <T> T lock(Lock lock, Callable<T> action) {
    lock.lock();
    try {
      return action.call();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

}
