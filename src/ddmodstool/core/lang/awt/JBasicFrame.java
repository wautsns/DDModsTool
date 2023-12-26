package ddmodstool.core.lang.awt;

import ddmodstool.core.conf.Conf;
import ddmodstool.core.lang.util.IO;
import java.awt.Image;
import javax.swing.JFrame;

/**
 * JavaAWT basic frame.
 *
 * @author wautsns
 * @since 1.0.0
 */
public class JBasicFrame extends JFrame {

  private static final Image ICON = IO.readImage(Conf.home.resolve("res/image/preview_icon.png"));

  // ---------------------------------------------------------------------------------------------
  // PublicConstructors
  // ---------------------------------------------------------------------------------------------

  public JBasicFrame() {
    setIconImage(ICON);
  }

}
