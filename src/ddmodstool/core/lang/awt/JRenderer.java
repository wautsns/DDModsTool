package ddmodstool.core.lang.awt;

import java.awt.Color;
import java.util.Locale;
import java.util.function.Function;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JavaAWT renderer.
 *
 * @author wautsns
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JRenderer {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static <E> ListCellRenderer<E> listCell(Function<E, String> toString) {
    return listCell(toString, null);
  }

  public static <E> ListCellRenderer<E> listCell(
      Function<E, String> toString, Function<E, String> getTooltip) {
    JLabel lbl = new JLabel();
    lbl.setOpaque(true);
    lbl.setName("List.cellRenderer");
    return (list, value, index, isSelected, cellHasFocus) -> {
      Locale locale = list.getLocale();
      lbl.setComponentOrientation(list.getComponentOrientation());
      Color bg = null;
      Color fg = null;
      JList.DropLocation dropLocation = list.getDropLocation();
      if (dropLocation != null && !dropLocation.isInsert() && dropLocation.getIndex() == index) {
        bg = UIManager.getColor("List.dropCellBackground", locale);
        fg = UIManager.getColor("List.dropCellForeground", locale);
        isSelected = true;
      }
      if (isSelected) {
        lbl.setBackground((bg == null) ? list.getSelectionBackground() : bg);
        lbl.setForeground((fg == null) ? list.getSelectionForeground() : fg);
      } else {
        lbl.setBackground(list.getBackground());
        lbl.setForeground(list.getForeground());
      }
      lbl.setEnabled(list.isEnabled());
      lbl.setFont(list.getFont());
      lbl.setText((value == null) ? "" : toString.apply(value));
      if ((getTooltip == null) || (value == null)) {
        JToolTipText.set(lbl);
      } else {
        lbl.setToolTipText(getTooltip.apply(value));
      }
      Border border;
      if (cellHasFocus) {
        if (isSelected) {
          border = UIManager.getBorder("List.focusSelectedCellHighlightBorder", locale);
        } else {
          border = UIManager.getBorder("List.focusCellHighlightBorder", locale);
        }
      } else {
        border = UIManager.getBorder("List.cellNoFocusBorder", locale);
      }
      lbl.setBorder(border);
      return lbl;
    };
  }

}
