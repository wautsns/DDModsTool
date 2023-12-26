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

import ddmodstool.core.lang.function.ConsumerT;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * I/O utils.
 *
 * @author wautsns
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IO {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static String name(Path path) {
    return path.getFileName().toString();
  }

  // ---------------------------------------------------------------------------------------------

  public static Stream<Path> list(Path dir) {
    if (!Files.exists(dir)) {
      return Stream.empty();
    }
    try (Stream<Path> stream = Files.list(dir)) {
      return stream.toList().stream();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ---------------------------------------------------------------------------------------------

  public static void walk(Path start, ConsumerT<Path> action) {
    if (Files.exists(start)) {
      try (Stream<Path> stream = Files.walk(start)) {
        stream.forEach(action);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static void walkExt(Path start, String ext, ConsumerT<Path> action) {
    if (Files.exists(start)) {
      try (Stream<Path> stream = Files.walk(start)) {
        stream.filter(path -> name(path).endsWith(ext)).forEach(action);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  // ---------------------------------------------------------------------------------------------

  public static Path mkdir(Path dir) {
    try {
      return Files.createDirectories(dir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean copy(Path source, Path target) {
    if (!Files.exists(source)) {
      return false;
    }
    try {
      Files.copy(source, target);
      if (Files.isDirectory(source)) {
        list(source).forEach(sourceSubPath -> {
          copy(sourceSubPath, target.resolve(source.relativize(sourceSubPath)));
        });
      }
      return true;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ---------------------------------------------------------------------------------------------

  public static InputStream input(Path path) {
    try {
      return Files.newInputStream(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static OutputStream output(Path path) {
    try {
      return Files.newOutputStream(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ---------------------------------------------------------------------------------------------

  public static String readString(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeString(Path path, String string) {
    try {
      Files.writeString(path, string);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ---------------------------------------------------------------------------------------------

  public static void scanXml(Path path, DefaultHandler sax) {
    try (InputStream input = IO.input(path)) {
      SAXParserFactory.newInstance().newSAXParser().parse(input, sax);
    } catch (IOException | ParserConfigurationException | SAXException e) {
      throw new RuntimeException(e);
    }
  }

  // ---------------------------------------------------------------------------------------------

  public static BufferedImage readImage(Path path) {
    try {
      return ImageIO.read(input(path));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
