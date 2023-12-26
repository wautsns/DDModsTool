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

import ddmodstool.core.game.base.data.localization.LocX;
import ddmodstool.core.lang.function.ConsumerT;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * JavaAWT transfer handler.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class JTransferHandler {

  public static TransferHandler file(ConsumerT<Path> action) {
    return new TransferHandler() {
      @Override
      public boolean importData(JComponent comp, Transferable t) {
        List<File> fileList;
        try {
          // noinspection unchecked
          fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
          throw new RuntimeException(e);
        }
        if (fileList.size() > 1) {
          throw LocX.of("JTransferHandler#file!OnlySupportsSingleFile");
        }
        action.accept(fileList.get(0).toPath());
        return true;
      }

      @Override
      public boolean canImport(JComponent comp, DataFlavor[] flavors) {
        for (DataFlavor flavor : flavors) {
          if (DataFlavor.javaFileListFlavor.equals(flavor)) {
            return true;
          }
        }
        return false;
      }
    };
  }

}
