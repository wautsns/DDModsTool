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

import ddmodstool.core.game.base.data.localization.Loc;
import ddmodstool.core.lang.function.RunnableT;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * JavaAWT loading.
 *
 * @author wautsns
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JLoading {

  private static final ExecutorService executor = Executors.newCachedThreadPool();
  private static final ThreadLocal<Consumer<String>> threadLocalMessenger = new ThreadLocal<>();

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static boolean of(JFrame owner, RunnableT action) {
    return of(owner, () -> {
      action.run();
      return true;
    }).isPresent();
  }

  public static <T> Optional<T> of(JFrame owner, Supplier<T> supplier) {
    JDialog dlg = new JDialog(owner, true);
    dlg.setUndecorated(true);
    dlg.setAlwaysOnTop(true);

    JLabel lblLoading = new JLabel();
    lblLoading.setOpaque(true);
    lblLoading.setBackground(Color.YELLOW);
    lblLoading.setFont(JFonts.get(48));
    lblLoading.setHorizontalAlignment(JLabel.CENTER);
    lblLoading.setText(Loc.G.text("JLoading#lblLoading#text"));
    lblLoading.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.BLACK),
        BorderFactory.createEmptyBorder(25, 50, 20, 50)));
    dlg.add(lblLoading, BorderLayout.CENTER);

    JLabel lblMessage = new JLabel();
    lblMessage.setOpaque(true);
    lblMessage.setBackground(Color.YELLOW);
    lblMessage.setFont(JFonts.get(16));
    lblMessage.setText("                                                                ");
    lblMessage.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.BLACK),
        BorderFactory.createEmptyBorder(0, 8, 0, 8)));
    dlg.add(lblMessage, BorderLayout.SOUTH);

    dlg.pack();
    dlg.setLocationRelativeTo(owner);

    Future<T> futureResult = executor.submit(() -> {
      threadLocalMessenger.set(message -> {
        lblMessage.setText(message);
        if (JFonts.width(lblMessage) > lblMessage.getSize().width) {
          dlg.pack();
          dlg.setLocationRelativeTo(owner);
          dlg.repaint();
        }
      });
      try {
        message(Loc.G.text("txt[Processing]"));
        return supplier.get();
      } catch (Exception e) {
        JMessage.error(owner, e);
        return null;
      } finally {
        dlg.dispose();
        threadLocalMessenger.remove();
      }
    });

    dlg.setVisible(true);

    try {
      return Optional.ofNullable(futureResult.get());
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  // ---------------------------------------------------------------------------------------------

  public static void message(String message) {
    Consumer<String> messenger = threadLocalMessenger.get();
    if (messenger != null) {
      messenger.accept(message);
    }
  }

}
