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

import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

/**
 * Observable value.
 *
 * @param <T> the type of observable value
 * @author wautsns
 * @since 1.0.0
 */
public final class ObV<T> {

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final WeakSet<BiConsumer<T, T>> observerSet = new WeakSet<>();

  private volatile T value;

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public T get() {
    lock.readLock().lock();
    try {
      return value;
    } finally {
      lock.readLock().unlock();
    }
  }

  public Optional<T> opt() {
    return Optional.ofNullable(get());
  }

  // ---------------------------------------------------------------------------------------------

  public T set(T value) {
    lock.writeLock().lock();
    T oldValue;
    try {
      oldValue = this.value;
      this.value = value;
      lock.readLock().lock();
    } finally {
      lock.writeLock().unlock();
    }
    try {
      observerSet.forEach(observer -> observer.accept(oldValue, value));
    } finally {
      lock.readLock().unlock();
    }
    return oldValue;
  }

  // ---------------------------------------------------------------------------------------------

  public Object observe(BiConsumer<T, T> observer, boolean immediate) {
    lock.writeLock().lock();
    try {
      observerSet.add(observer);
      if (immediate) {
        lock.readLock().lock();
      }
    } finally {
      lock.writeLock().unlock();
    }
    if (immediate) {
      try {
        observer.accept(null, value);
      } finally {
        lock.readLock().unlock();
      }
    }
    return observer;
  }

  // ---------------------------------------------------------------------------------------------
  // PublicConstructors
  // ---------------------------------------------------------------------------------------------

  public ObV(T initialValue) {
    this.value = initialValue;
  }

}
