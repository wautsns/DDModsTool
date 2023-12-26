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

import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Weak referent set.
 *
 * @param <T> the type of weak referent
 * @author wautsns
 * @since 1.0.0
 */
public final class WeakSet<T> {

  private final WeakNode<T> head = new WeakNode<>(null);

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public synchronized boolean isEmpty() {
    WeakNode<T> prev = head, curr = prev.next;
    while (curr != null) {
      T ref = curr.get();
      if (ref == null) {
        prev.next = curr.next;
      } else {
        return false;
      }
      curr = curr.next;
    }
    return true;
  }

  // ---------------------------------------------------------------------------------------------

  public synchronized T get(Predicate<T> selector) {
    WeakNode<T> prev = head, curr = prev.next;
    while (curr != null) {
      T ref = curr.get();
      if (ref == null) {
        prev.next = curr.next;
      } else if (selector.test(ref)) {
        return ref;
      } else {
        prev = curr;
      }
      curr = curr.next;
    }
    return null;
  }

  public synchronized void forEach(Consumer<T> action) {
    WeakNode<T> prev = head, curr = prev.next;
    while (curr != null) {
      T ref = curr.get();
      if (ref == null) {
        prev.next = curr.next;
      } else {
        action.accept(ref);
        prev = curr;
      }
      curr = curr.next;
    }
  }

  // ---------------------------------------------------------------------------------------------

  public synchronized T add(T referent) {
    WeakNode<T> prev = head, curr = prev.next;
    while (curr != null) {
      T ref = curr.get();
      if (ref == null) {
        prev.next = curr.next;
      } else if (ref.equals(referent)) {
        return ref;
      } else {
        prev = curr;
      }
      curr = curr.next;
    }
    prev.next = new WeakNode<>(referent);
    return referent;
  }

  public synchronized T add(Predicate<T> selector, Supplier<T> supplier) {
    WeakNode<T> prev = head, curr = prev.next;
    while (curr != null) {
      T ref = curr.get();
      if (ref == null) {
        prev.next = curr.next;
      } else if (selector.test(ref)) {
        return ref;
      } else {
        prev = curr;
      }
      curr = curr.next;
    }
    T ref = supplier.get();
    prev.next = new WeakNode<>(ref);
    return ref;
  }

  // ---------------------------------------------------------------------------------------------

  public synchronized void remove(Object referent) {
    WeakNode<T> prev = head, curr = prev.next;
    while (curr != null) {
      T ref = curr.get();
      if (ref == null) {
        prev.next = curr.next;
      } else if (ref.equals(referent)) {
        prev.next = curr.next;
        return;
      } else {
        prev = curr;
      }
      curr = curr.next;
    }
  }

  public synchronized void remove(Predicate<T> filter) {
    WeakNode<T> prev = head, curr = prev.next;
    while (curr != null) {
      T ref = curr.get();
      if (ref == null) {
        prev.next = curr.next;
      } else if (filter.test(ref)) {
        prev.next = curr.next;
      } else {
        prev = curr;
      }
      curr = curr.next;
    }
  }

  public synchronized void clear() {
    head.next = null;
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateStaticClasses
  // ---------------------------------------------------------------------------------------------

  private static final class WeakNode<T> extends WeakReference<T> {

    WeakNode<T> next;

    WeakNode(T referent) {
      super(referent);
    }

  }

}
