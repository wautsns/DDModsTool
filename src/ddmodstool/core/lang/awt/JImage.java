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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * JavaAWT image.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class JImage extends JPanel {

  private Image image;

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public void setImage(Image image) {
    Image oldValue = this.image;
    this.image = image;
    firePropertyChange("image", oldValue, image);
    if (oldValue != image) {
      revalidate();
      repaint();
    }
  }

  // ---------------------------------------------------------------------------------------------
  // @Override PublicMethods, JPanel
  // ---------------------------------------------------------------------------------------------

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (image != null) {
      if (g instanceof Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      }
      g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    } else {
      g.setColor(Color.GRAY);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(Color.RED);
      g.drawLine(0, 0, getWidth(), getHeight());
      g.drawLine(getWidth(), 0, 0, getHeight());
    }
  }

}
