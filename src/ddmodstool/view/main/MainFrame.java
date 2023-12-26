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
package ddmodstool.view.main;

import ddmodstool.core.game.base.data.localization.Loc;
import ddmodstool.core.lang.awt.JBasicFrame;
import ddmodstool.core.lang.awt.JMessage;
import ddmodstool.core.lang.awt.JToolTipText;
import ddmodstool.view.conf.InitialSetupWizardFrame;
import ddmodstool.view.game.tool.renameskelanim.RenameSkelAnimFrame;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

/**
 * Main frame.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class MainFrame extends JBasicFrame {

  // ---------------------------------------------------------------------------------------------
  // PublicConstructors
  // ---------------------------------------------------------------------------------------------

  public MainFrame() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    setTitle(Loc.G.text("MainFrame#title"));

    setupNorth();
    setupCenter();
    setupSystemTrayIfSupported();

    setSize(800 + 16, 600 + 31 + 8);
    setResizable(false);
    setLocationRelativeTo(null);

    setVisible(true);
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateMethods
  // ---------------------------------------------------------------------------------------------

  private void setupNorth() {
    JMenuBar mnb = new JMenuBar();

    JMenu mnuSettings = new JMenu();
    mnuSettings.setText(Loc.G.text("MainFrame#mnuSettings#text"));
    JToolTipText.set(mnuSettings);
    mnb.add(mnuSettings);

    JMenuItem mniInitialSetupWizard = new JMenuItem();
    mniInitialSetupWizard.setText(Loc.G.text("MainFrame#mniInitialSetupWizard#text"));
    JToolTipText.set(mniInitialSetupWizard);
    mniInitialSetupWizard.addActionListener(event -> {
      dispose();
      new InitialSetupWizardFrame();
    });
    mnuSettings.add(mniInitialSetupWizard);

    add(mnb, BorderLayout.NORTH);
  }

  private void setupCenter() {
    JPanel pnlFunc = new JPanel();
    pnlFunc.setLayout(null);

    List<JButton> btnList = new ArrayList<>();

    JButton btnRenameSkelAnim = new JButton();
    btnRenameSkelAnim.setText(Loc.G.text("MainFrame#btnRenameSkelAnim#text"));
    btnRenameSkelAnim.addActionListener(event -> new RenameSkelAnimFrame());
    btnList.add(btnRenameSkelAnim);

    for (int i = 0; i < btnList.size(); i++) {
      int r = i / 5, c = i % 5;
      JButton btnFunc = btnList.get(i);
      btnFunc.setBounds(8 + c * (160 + 8), 8 + r * (26 + 8), 160, 16 + 10);
      JToolTipText.set(btnFunc);
      pnlFunc.add(btnFunc);
    }
    add(pnlFunc, BorderLayout.CENTER);
  }

  private void setupSystemTrayIfSupported() {
    if (!SystemTray.isSupported()) {
      return;
    }
    TrayIcon trayIcon = new TrayIcon(getIconImage(), "DDModsTool");
    trayIcon.setImageAutoSize(true);
    SystemTray systemTray = SystemTray.getSystemTray();
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowIconified(WindowEvent event) {
        try {
          systemTray.add(trayIcon);
          setVisible(false);
        } catch (AWTException e) {
          JMessage.error(MainFrame.this, e);
        }
      }
    });
    trayIcon.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent event) {
        systemTray.remove(trayIcon);
        setVisible(true);
        setExtendedState(Frame.NORMAL);
      }
    });
  }

}
