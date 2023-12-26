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
package ddmodstool.view.conf;

import static java.lang.String.format;

import ddmodstool.core.conf.Conf;
import ddmodstool.core.game.base.data.localization.Language;
import ddmodstool.core.game.base.data.localization.Loc;
import ddmodstool.core.lang.awt.JBasicFrame;
import ddmodstool.core.lang.awt.JFonts;
import ddmodstool.core.lang.awt.JStyle;
import ddmodstool.core.lang.awt.JToolTipText;
import ddmodstool.view.main.MainFrame;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * InitialSetupWizard frame.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class InitialSetupWizardFrame extends JBasicFrame {

  // ---------------------------------------------------------------------------------------------
  // PublicConstructors
  // ---------------------------------------------------------------------------------------------

  public InitialSetupWizardFrame() {
    setLayout(null);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        Conf.save();
        if (Conf.okay()) {
          new MainFrame();
        } else {
          System.exit(0);
        }
      }
    });

    setupScene1();

    setVisible(true);
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateMethods
  // ---------------------------------------------------------------------------------------------

  private void setupScene1() {
    removeAllAndRepaint();

    // >>>> components
    int y = 8, w = 600;

    JLabel lblLogo = new JLabel();
    lblLogo.setBounds(8, y, w - 8 - 8, 72);
    lblLogo.setText("DDModsTool");
    add(lblLogo);
    y += 72 + 8;

    JLabel lblWelcome1 = new JLabel();
    lblWelcome1.setBounds(8, y, w - (8 + 240 + 8) - 8, 24 + 10);
    add(lblWelcome1);
    JButton btnStart = new JButton();
    btnStart.setBounds(w - (8 + 240), y, 240, 24 + 10);
    add(btnStart);
    y += (24 + 10) + 8;
    JTextArea lblWelcome2 = new JTextArea();
    lblWelcome2.setEditable(false);
    lblWelcome2.setLineWrap(true);
    lblWelcome2.setWrapStyleWord(true);
    JScrollPane sclWelcome2 = new JScrollPane(lblWelcome2);
    sclWelcome2.setBounds(8, y, w - 8 - 8, (16 + 4) * 6 + 10);
    sclWelcome2.setBorder(BorderFactory.createEtchedBorder());
    add(sclWelcome2);
    y += ((16 + 4) * 6 + 10) + 8;

    JLabel lblLanguage = new JLabel();
    lblLanguage.setBounds(8, y, w - (8 + 480 + 8) - 8, 16 + 10);
    lblLanguage.setHorizontalAlignment(JLabel.RIGHT);
    add(lblLanguage);
    JComboBox<Language> cbbLanguage = new JComboBox<>(Language.values());
    cbbLanguage.setBounds(w - (8 + 480), y, 480, 16 + 10);
    add(cbbLanguage);
    y += (16 + 10) + 8;

    JLabel lblStyle = new JLabel();
    lblStyle.setBounds(8, y, w - (8 + 480 + 8) - 8, 16 + 10);
    lblStyle.setHorizontalAlignment(JLabel.RIGHT);
    add(lblStyle);
    JComboBox<JStyle> cbbStyle = new JComboBox<>(JStyle.values());
    cbbStyle.setBounds(w - (8 + 480), y, 480, 16 + 10);
    add(cbbStyle);
    y += (16 + 10) + 8;

    JLabel lblFontFamily = new JLabel();
    lblFontFamily.setBounds(8, y, w - (8 + 480 + 8) - 8, 16 + 10);
    lblFontFamily.setHorizontalAlignment(JLabel.RIGHT);
    add(lblFontFamily);
    JComboBox<String> cbbFontFamily = new JComboBox<>(
        GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    cbbFontFamily.setBounds(w - (8 + 480), y, 480, 16 + 10);
    add(cbbFontFamily);
    y += (16 + 10) + 8;

    setResizable(false);
    setSize(w + 16, y + 31 + 8);
    setLocationRelativeTo(null);

    // >>>> actions
    cbbLanguage.setSelectedItem(null);
    cbbLanguage.addItemListener(event -> {
      if (event.getStateChange() == ItemEvent.SELECTED) {
        Conf.view.language.set((Language) event.getItem());
        setTitle(format(Loc.G.text("InitialSetupWizardFrame#title"), 1, 2));
        lblWelcome1.setText(Loc.G.text("InitialSetupWizardFrame#scene1#lblWelcome1#text"));
        JToolTipText.set(lblWelcome1);
        lblWelcome2.setText(Loc.G.text("InitialSetupWizardFrame#scene1#lblWelcome2#text"));
        lblWelcome2.setCaretPosition(0);
        lblLanguage.setText(Loc.G.text("InitialSetupWizardFrame#scene1#lblLanguage#text"));
        JToolTipText.set(lblLanguage);
        lblStyle.setText(Loc.G.text("InitialSetupWizardFrame#scene1#lblStyle#text"));
        JToolTipText.set(lblStyle);
        lblFontFamily.setText(Loc.G.text("InitialSetupWizardFrame#scene1#lblFontFamily#text"));
        JToolTipText.set(lblFontFamily);
        cbbStyle.repaint();
        btnStart.setText(Loc.G.text("InitialSetupWizardFrame#scene1#btnStart#text"));
        JToolTipText.set(btnStart);
      }
      JToolTipText.set(cbbStyle);
    });
    cbbLanguage.setSelectedItem(Conf.view.language.get());

    cbbStyle.setSelectedItem(null);
    cbbStyle.addItemListener(event -> {
      if (event.getStateChange() == ItemEvent.SELECTED) {
        Conf.view.style.set((JStyle) event.getItem());
        SwingUtilities.updateComponentTreeUI(this);
      }
      JToolTipText.set(cbbStyle);
    });
    cbbStyle.setSelectedItem(Conf.view.style.get());

    cbbFontFamily.setSelectedItem(null);
    cbbFontFamily.addItemListener(event -> {
      if (event.getStateChange() == ItemEvent.SELECTED) {
        Conf.view.fontFamily.set((String) event.getItem());
        lblLogo.setFont(JFonts.get(72));
        lblWelcome1.setFont(JFonts.get(24));
        lblWelcome2.setFont(JFonts.get(16));
        lblLanguage.setFont(JFonts.get(16));
        cbbLanguage.setFont(JFonts.get(16));
        lblStyle.setFont(JFonts.get(16));
        cbbStyle.setFont(JFonts.get(16));
        lblFontFamily.setFont(JFonts.get(16));
        cbbFontFamily.setFont(JFonts.get(JFonts.defaultFamily, 16));
        btnStart.setFont(JFonts.get(24));
        SwingUtilities.updateComponentTreeUI(this);
      }
      JToolTipText.set(cbbFontFamily);
    });
    cbbFontFamily.setSelectedItem(Conf.view.fontFamily.get());

    btnStart.addActionListener(event -> setupScene2());
  }

  private void setupScene2() {
    removeAllAndRepaint();

    // >>>> components
    int y = 8, w = 600;

    setTitle(format(Loc.G.text("InitialSetupWizardFrame#title"), 2, 2));

    JLabel lblGameHome = new JLabel();
    lblGameHome.setBounds(8, y, w - (8 + 160 + 8) - 8, 16 + 10);
    lblGameHome.setText(Loc.G.text("InitialSetupWizardFrame#scene2#lblGameHome#text"));
    JToolTipText.set(lblGameHome);
    add(lblGameHome);
    JButton btnGameHome = new JButton();
    btnGameHome.setBounds(w - (8 + 160), y, 160, 16 + 10);
    btnGameHome.setText(Loc.G.text("InitialSetupWizardFrame#scene2#btnGameHome#text"));
    JToolTipText.set(btnGameHome);
    add(btnGameHome);
    y += (16 + 10) + 8;
    JTextField txfGameHome = new JTextField();
    txfGameHome.setBounds(8, y, w - 8 - 8, 26);
    txfGameHome.setText(Conf.game.home.opt().map(Path::toString).orElse(null));
    JToolTipText.set(txfGameHome);
    txfGameHome.setEditable(false);
    add(txfGameHome);
    y += (16 + 10) + 8;

    JButton btnPrevStep = new JButton();
    btnPrevStep.setFont(JFonts.get(24));
    btnPrevStep.setBounds(w - (8 + 240 + 8 + 240 + 8), y, 240, 24 + 10);
    btnPrevStep.setText(Loc.G.text("txt[PrevStep]"));
    JToolTipText.set(btnPrevStep);
    add(btnPrevStep);
    JButton btnNextStep = new JButton();
    btnNextStep.setFont(JFonts.get(24));
    btnNextStep.setBounds(w - (8 + 240), y, 240, 24 + 10);
    btnNextStep.setText(Loc.G.text("txt[Complete]"));
    JToolTipText.set(btnNextStep);
    add(btnNextStep);
    y += (24 + 10) + 8;

    setSize(w + 16, y + 31 + 8);
    setResizable(false);
    setLocationRelativeTo(null);

    // >>>> actions
    btnGameHome.addActionListener(event -> {
      JFileChooser fcsGameHome = new JFileChooser();
      fcsGameHome.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      Path gameHome = Conf.game.home.get();
      if ((gameHome != null) && Files.exists(gameHome)) {
        fcsGameHome.setCurrentDirectory(gameHome.toFile());
      }
      if (fcsGameHome.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        gameHome = fcsGameHome.getSelectedFile().toPath();
        Conf.game.home.set(gameHome);
        txfGameHome.setText(gameHome.toString());
        JToolTipText.set(txfGameHome);
      }
    });

    btnPrevStep.addActionListener(event -> setupScene1());
    btnNextStep.addActionListener(event -> {
      dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    });
  }

  // ---------------------------------------------------------------------------------------------

  private void removeAllAndRepaint() {
    Container pnl = getContentPane();
    pnl.removeAll();
    pnl.repaint();
  }

}
