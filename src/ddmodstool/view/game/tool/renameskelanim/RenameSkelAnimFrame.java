package ddmodstool.view.game.tool.renameskelanim;

import static java.lang.String.format;

import ddmodstool.core.conf.Conf;
import ddmodstool.core.game.base.data.localization.Loc;
import ddmodstool.core.game.base.file.skel.Skel;
import ddmodstool.core.lang.awt.JBasicFrame;
import ddmodstool.core.lang.awt.JLoading;
import ddmodstool.core.lang.awt.JMessage;
import ddmodstool.core.lang.awt.JModel;
import ddmodstool.core.lang.awt.JRenderer;
import ddmodstool.core.lang.awt.JToolTipText;
import ddmodstool.core.lang.util.IO;
import ddmodstool.core.lang.util.ObV;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

/**
 * Rename skel anim frame.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class RenameSkelAnimFrame extends JBasicFrame {

  private final ObV<Path> animPath = new ObV<>(null);
  private final ObV<Skel> selectedSkel = new ObV<>(null);

  // ---------------------------------------------------------------------------------------------
  // PublicConstructors
  // ---------------------------------------------------------------------------------------------

  public RenameSkelAnimFrame() {
    setLayout(null);

    setupScene1();

    setVisible(true);
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateMethods
  // ---------------------------------------------------------------------------------------------

  private void setupScene1() {
    // >>>> components
    int y = 8, w = 600;

    setTitle(format(Loc.G.text("RenameSkelAnimFrame#scene1#title"), 1, 4));

    JLabel lblSkelPathList = new JLabel();
    lblSkelPathList.setBounds(8, y, w - (8 + 160 + 8) - 8, 16 + 10);
    lblSkelPathList.setText(Loc.G.text("RenameSkelAnimFrame#scene1#lblSkelPathList#text"));
    JToolTipText.set(lblSkelPathList);
    add(lblSkelPathList);
    JButton btnSkelPathList = new JButton();
    btnSkelPathList.setBounds(w - (8 + 160), y, 160, 16 + 10);
    btnSkelPathList.setText(Loc.G.text("RenameSkelAnimFrame#scene1#btnSkelPathList#text"));
    JToolTipText.set(btnSkelPathList);
    add(btnSkelPathList);
    y += (16 + 10) + 8;
    JComboBox<Path> cbbSkelPathList = new JComboBox<>(JModel.comboBox());
    cbbSkelPathList.setRenderer(JRenderer.listCell(IO::name));
    cbbSkelPathList.setBounds(8, y, w - 8 - 8, 16 + 10);
    add(cbbSkelPathList);
    y += (16 + 10) + 8;
    JLabel lblAnimationList = new JLabel();
    lblAnimationList.setBounds(8, y, w - (8 + 160 + 8) - 8, 16 + 10);
    lblAnimationList.setText(Loc.G.text("RenameSkelAnimFrame#scene1#lblAnimationList#text"));
    JToolTipText.set(lblAnimationList);
    add(lblAnimationList);
    y += (16 + 10)/* + 8*/;
    JList<Skel.Animation> lstAnimationList = new JList<>(JModel.list());
    lstAnimationList.setCellRenderer(JRenderer.listCell(Skel.Animation::reqName));
    JScrollPane sclAnimationList = new JScrollPane(lstAnimationList);
    sclAnimationList.setBounds(8, y, w - 8 - 8, (16 + 4) * 10 + 10);
    add(sclAnimationList);
    y += ((16 + 4) * 10 + 10) + 8;
    JButton btnPreview = new JButton();
    btnPreview.setBounds(w - (8 + 160 + 8 + 160), y, 160, 16 + 10);
    btnPreview.setText(Loc.G.text("RenameSkelAnimFrame#scene1#btnPreview#text"));
    JToolTipText.set(btnPreview);
    add(btnPreview);
    JButton btnSave = new JButton();
    btnSave.setBounds(w - (8 + 160), y, 160, 16 + 10);
    btnSave.setText(Loc.G.text("RenameSkelAnimFrame#scene1#btnSave#text"));
    JToolTipText.set(btnSave);
    add(btnSave);
    y += (16 + 10) + 8;

    setSize(w + 16, y + 31 + 8);
    setResizable(false);
    setLocationRelativeTo(null);

    // >>>> menus
    Runnable renameAction = () -> {
      Skel.Animation skelAnim = lstAnimationList.getSelectedValue();
      String newSkelAnimName = JOptionPane.showInputDialog(this,
          Loc.G.text("RenameSkelAnimFrame#scene1#dlgRename#message"),
          skelAnim.reqName());
      if ((newSkelAnimName != null) && !newSkelAnimName.isBlank()) {
        skelAnim.setName(newSkelAnimName);
        lstAnimationList.repaint();
      }
    };
    JPopupMenu pmnAnimationList = new JPopupMenu();
    lstAnimationList.setComponentPopupMenu(pmnAnimationList);
    JModel.popupMenuItem.copy(lstAnimationList, Skel.Animation::clone);
    JMenuItem mniRename = new JMenuItem();
    mniRename.setText(Loc.G.text("RenameSkelAnimFrame#scene1#mniRename#text"));
    JToolTipText.set(mniRename);
    mniRename.addActionListener(event -> renameAction.run());
    pmnAnimationList.add(mniRename);
    pmnAnimationList.addSeparator();
    JModel.popupMenuItem.common(lstAnimationList);

    // >>>> actions
    btnSkelPathList.addActionListener(event -> {
      JFileChooser fcsSkel = new JFileChooser();
      fcsSkel.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      fcsSkel.setCurrentDirectory(animPath.opt().orElseGet(Conf.game.home::get).toFile());
      if (fcsSkel.showOpenDialog(RenameSkelAnimFrame.this) == JFileChooser.APPROVE_OPTION) {
        animPath.set(fcsSkel.getSelectedFile().toPath());
      }
    });
    // noinspection unused
    cbbSkelPathList.addItemListener(new ItemListener() {
      // strongly referenced by this instance
      final Object animPathObserver = animPath.observe((prev, curr) -> {
        if (curr == null) {
          cbbSkelPathList.setModel(JModel.comboBox());
        } else {
          List<Path> skelPathList = IO.list(curr)
              .filter(path -> IO.name(path).endsWith(".skel"))
              .collect(Collectors.toCollection(ArrayList::new));
          cbbSkelPathList.setModel(JModel.comboBox(skelPathList));
        }
        updateSelectedSkel((Path) cbbSkelPathList.getSelectedItem());
      }, false);

      @Override
      public void itemStateChanged(ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
          updateSelectedSkel((Path) event.getItem());
        }
      }

      private void updateSelectedSkel(Path skelPath) {
        if (skelPath == null) {
          selectedSkel.set(null);
        } else {
          JLoading.of(RenameSkelAnimFrame.this, () -> Skel.read(skelPath))
              .ifPresent(selectedSkel::set);
        }
      }
    });
    // noinspection unused
    lstAnimationList.addMouseListener(new MouseAdapter() {
      // strongly referenced by this instance
      final Object selectedSkelObserver = selectedSkel.observe((prev, curr) -> {
        if (curr == null) {
          lstAnimationList.setModel(JModel.list());
        } else {
          lstAnimationList.setModel(JModel.list(curr.reqAnimationList()));
        }
      }, false);

      @Override
      public void mouseClicked(MouseEvent event) {
        if ((event.getButton() == MouseEvent.BUTTON1) && (event.getClickCount() == 2)) {
          renameAction.run();
        }
      }
    });
    btnPreview.addActionListener(event -> {
      selectedSkel.opt().ifPresent(selectedSkel -> {
        try {
          Runtime.getRuntime().exec(new String[]{
              "java", "-jar", "DDSkelViewer.jar", selectedSkel.getPath().toString()});
        } catch (IOException e) {
          JMessage.error(this, e);
        }
      });
    });
    btnSave.addActionListener(event -> {
      selectedSkel.opt().ifPresent(selectedSkel -> {
        if (JLoading.of(this, () -> Skel.write(selectedSkel.getPath(), selectedSkel))) {
          JMessage.info(this, Loc.G.text("txt[ProcessingIsSuccessful]"));
        }
      });
    });
  }

}
