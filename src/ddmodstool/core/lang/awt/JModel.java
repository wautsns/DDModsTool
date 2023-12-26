package ddmodstool.core.lang.awt;

import ddmodstool.core.game.base.data.localization.Loc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.UnaryOperator;
import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MutableComboBoxModel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JavaAWT model.
 *
 * @author wautsns
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JModel {

  public static final PopupMenuItemMethods popupMenuItem = new PopupMenuItemMethods();

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static <E> ListM<E> list() {
    return new ListM<>(new ArrayList<>());
  }

  public static <E> ListM<E> list(List<E> delegate) {
    return new ListM<>(delegate);
  }

  public static <E> ListM<E> get(JList<E> list) {
    return (ListM<E>) list.getModel();
  }

  // ---------------------------------------------------------------------------------------------

  public static <E> ComboBoxM<E> comboBox() {
    return new ComboBoxM<>(new ArrayList<>());
  }

  public static <E> ComboBoxM<E> comboBox(List<E> delegate) {
    return new ComboBoxM<>(delegate);
  }

  public static <E> ComboBoxM<E> get(JComboBox<E> comboBox) {
    return (ComboBoxM<E>) comboBox.getModel();
  }

  // ---------------------------------------------------------------------------------------------
  // PublicStaticClasses
  // ---------------------------------------------------------------------------------------------

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class PopupMenuItemMethods {

    // -----------------------------------------------------------------------------------------
    // PublicStaticMethods
    // -----------------------------------------------------------------------------------------

    public <E> void common(JList<E> list) {
      JPopupMenu pmn = initPopupMenuIfAbsent(list);

      JMenuItem mniMoveUp = new JMenuItem();
      mniMoveUp.setText(Loc.G.text("JModel#mniMoveUp#text"));
      JToolTipText.set(mniMoveUp);
      mniMoveUp.addActionListener(event -> {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex >= 1) {
          JModel.get(list).swap(selectedIndex - 1, selectedIndex);
          list.setSelectedIndex(selectedIndex - 1);
        }
      });
      pmn.add(mniMoveUp);

      JMenuItem mniMoveDown = new JMenuItem();
      mniMoveDown.setText(Loc.G.text("JModel#mniMoveDown#text"));
      JToolTipText.set(mniMoveDown);
      mniMoveDown.addActionListener(event -> {
        int selectedIndex = list.getSelectedIndex();
        JModel.ListM<E> model = get(list);
        if (selectedIndex < (model.size() - 1)) {
          model.swap(selectedIndex, selectedIndex + 1);
          list.setSelectedIndex(selectedIndex + 1);
        }
      });
      pmn.add(mniMoveDown);

      JMenuItem mniRemove = new JMenuItem();
      mniRemove.setText(Loc.G.text("JModel#mniRemove#text"));
      JToolTipText.set(mniRemove);
      mniRemove.addActionListener(event -> {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex >= 0) {
          get(list).remove(selectedIndex);
        }
      });
      pmn.add(mniRemove);
    }

    public <E> void copy(JList<E> list, UnaryOperator<E> clone) {
      JPopupMenu pmn = initPopupMenuIfAbsent(list);

      JMenuItem mniCopy = new JMenuItem();
      mniCopy.setText(Loc.G.text("JModel#mniCopy#text"));
      JToolTipText.set(mniCopy);
      mniCopy.addActionListener(event -> {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex >= 0) {
          ListM<E> model = get(list);
          E copy = clone.apply(model.get(selectedIndex));
          model.add(selectedIndex + 1, copy);
          list.setSelectedIndex(selectedIndex + 1);
        }
      });
      pmn.add(mniCopy);
    }

    // -----------------------------------------------------------------------------------------
    // PrivateStaticMethods
    // -----------------------------------------------------------------------------------------

    private JPopupMenu initPopupMenuIfAbsent(JComponent component) {
      JPopupMenu pmn = component.getComponentPopupMenu();
      if (pmn == null) {
        pmn = new JPopupMenu();
        component.setComponentPopupMenu(pmn);
      }
      return pmn;
    }

  }

  // ---------------------------------------------------------------------------------------------

  public static class ListM<E> extends AbstractListModel<E> implements List<E> {

    private final List<E> delegate;

    // -----------------------------------------------------------------------------------------
    // PublicMethods
    // -----------------------------------------------------------------------------------------

    public void swap(int index1, int index2) {
      delegate.set(index2, delegate.set(index1, delegate.get(index2)));
      fireContentsChanged(this, index1, index2);
    }

    // -----------------------------------------------------------------------------------------
    // @Override PublicMethods, AbstractListModel
    // -----------------------------------------------------------------------------------------

    @Override
    public int getSize() {
      return delegate.size();
    }

    @Override
    public E getElementAt(int index) {
      return delegate.get(index);
    }

    // -----------------------------------------------------------------------------------------
    // @Override PublicMethods, List
    // -----------------------------------------------------------------------------------------

    @Override
    public int size() {
      return delegate.size();
    }

    @Override
    public boolean isEmpty() {
      return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
      return delegate.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
      return new ListItr(0);
    }

    @Override
    public Object[] toArray() {
      return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return delegate.toArray(a);
    }

    @Override
    public boolean add(E e) {
      int index = delegate.size();
      delegate.add(e);
      fireIntervalAdded(this, index, index);
      return true;
    }

    @Override
    public boolean remove(Object o) {
      int index = indexOf(o);
      if (index >= 0) {
        remove(index);
        return true;
      }
      return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      // noinspection SlowListContainsAll
      return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
      return delegate.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
      return delegate.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return delegate.retainAll(c);
    }

    @Override
    public void clear() {
      int index = delegate.size() - 1;
      if (index >= 0) {
        delegate.clear();
        fireIntervalRemoved(this, 0, index);
      }
    }

    @Override
    public E get(int index) {
      return delegate.get(index);
    }

    @Override
    public E set(int index, E element) {
      E prev = delegate.set(index, element);
      fireContentsChanged(this, index, index);
      return prev;
    }

    @Override
    public void add(int index, E element) {
      delegate.add(index, element);
      fireIntervalAdded(this, index, index);
    }

    @Override
    public E remove(int index) {
      E prev = delegate.remove(index);
      fireIntervalRemoved(this, index, index);
      return prev;
    }

    @Override
    public int indexOf(Object o) {
      return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
      return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
      return new ListItr(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
      return new ListItr(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
      return delegate.subList(fromIndex, toIndex);
    }

    // -----------------------------------------------------------------------------------------
    // @Override PublicMethods, Object
    // -----------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if ((o == null) || (getClass() != o.getClass())) {
        return false;
      }
      ListM<?> that = (ListM<?>) o;
      return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
      return delegate.hashCode();
    }

    // -----------------------------------------------------------------------------------------
    // ProtectedConstructors
    // -----------------------------------------------------------------------------------------

    protected ListM(List<E> delegate) {
      this.delegate = delegate;
    }

    // -----------------------------------------------------------------------------------------
    // PrivateClasses
    // -----------------------------------------------------------------------------------------

    private final class ListItr implements ListIterator<E> {

      int cursor;
      int lastRet = -1;

      @Override
      public boolean hasNext() {
        return cursor != size();
      }

      @Override
      public E next() {
        try {
          int i = cursor;
          E next = get(i);
          lastRet = i;
          cursor = i + 1;
          return next;
        } catch (IndexOutOfBoundsException e) {
          throw new NoSuchElementException(e);
        }
      }

      @Override
      public boolean hasPrevious() {
        return cursor != 0;
      }

      @Override
      public E previous() {
        try {
          int i = cursor - 1;
          E previous = get(i);
          lastRet = cursor = i;
          return previous;
        } catch (IndexOutOfBoundsException e) {
          throw new NoSuchElementException(e);
        }
      }

      @Override
      public int nextIndex() {
        return cursor;
      }

      @Override
      public int previousIndex() {
        return cursor - 1;
      }

      @Override
      public void remove() {
        if (lastRet < 0) {
          throw new IllegalStateException();
        }
        try {
          ListM.this.remove(lastRet);
          if (lastRet < cursor) {
            cursor--;
          }
          lastRet = -1;
        } catch (IndexOutOfBoundsException e) {
          throw new ConcurrentModificationException();
        }
      }

      @Override
      public void set(E e) {
        if (lastRet < 0) {
          throw new IllegalStateException();
        }
        try {
          ListM.this.set(lastRet, e);
        } catch (IndexOutOfBoundsException ex) {
          throw new ConcurrentModificationException();
        }
      }

      @Override
      public void add(E e) {
        try {
          int i = cursor;
          ListM.this.add(i, e);
          lastRet = -1;
          cursor = i + 1;
        } catch (IndexOutOfBoundsException ex) {
          throw new ConcurrentModificationException();
        }
      }

      ListItr(int index) {
        this.cursor = index;
      }

    }

  }

  public static class ComboBoxM<E> extends ListM<E> implements MutableComboBoxModel<E> {

    private Object selectedItem;

    // -----------------------------------------------------------------------------------------
    // @Override PublicMethods, MutableComboBoxModel
    // -----------------------------------------------------------------------------------------

    @Override
    public void setSelectedItem(Object anItem) {
      if (!Objects.equals(selectedItem, anItem)) {
        selectedItem = anItem;
        fireContentsChanged(this, -1, -1);
      }
    }

    @Override
    public E getSelectedItem() {
      // noinspection unchecked
      return (E) selectedItem;
    }

    @Override
    public void addElement(E item) {
      add(item);
    }

    @Override
    public void removeElement(Object obj) {
      remove(obj);
    }

    @Override
    public void insertElementAt(E item, int index) {
      add(index, item);
    }

    @Override
    public void removeElementAt(int index) {
      remove(index);
    }

    // -----------------------------------------------------------------------------------------
    // ProtectedMethods
    // -----------------------------------------------------------------------------------------

    protected ComboBoxM(List<E> delegate) {
      super(delegate);
    }

  }

}
