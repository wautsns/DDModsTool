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
package ddmodstool.core.conf;

import ddmodstool.core.lang.util.IO;
import ddmodstool.core.lang.util.ObV;
import ddmodstool.core.lang.util.Simple;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Conf.
 *
 * @author wautsns
 * @since 1.0.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Conf {

  public static final Path home;
  public static final Properties properties;

  public static final ConfView view;
  public static final ConfGame game;

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static boolean okay() {
    Path gameHome = game.home.get();
    if (gameHome == null) {
      return false;
    } else if (Files.exists(gameHome)) {
      return true;
    } else {
      game.home.set(null);
      return false;
    }
  }

  public static void save() {
    Path path = home.resolve("conf.properties");
    try {
      String comments = "This file will be automatically overwritten and generated.\n"
          + "DO NOT modify without understanding!!!";
      properties.store(IO.output(path), comments);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // PackageStaticMethods ( & Fields )
  // ---------------------------------------------------------------------------------------------

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  static final List<Object> observerList; // strongly referenced by this class

  // ---------------------------------------------------------------------------------------------

  static ObV<String> init(String key, String defaults) {
    return init(key, defaults, t -> t, t -> t);
  }

  static ObV<Path> init(String key, Path defaults) {
    return init(key, defaults, Path::toString, Path::of);
  }

  static <T extends Enum<T>> ObV<T> init(String key, T defaults, Function<String, T> deserializer) {
    return init(key, defaults, Enum::name, deserializer);
  }

  static <T> ObV<T> init(
      String key, T defaults, Function<T, String> serializer, Function<String, T> deserializer) {
    T initialValue;
    String valueStr = properties.getProperty(key);
    if (valueStr == null) {
      initialValue = defaults;
      properties.put(key, "<null>");
    } else if ("<null>".equals(valueStr)) {
      initialValue = defaults;
    } else {
      initialValue = deserializer.apply(valueStr);
    }
    ObV<T> obV = new ObV<>(initialValue);
    observerList.add(obV.observe((prev, curr) -> {
      if (curr == null) {
        properties.put(key, "<null>");
      } else {
        properties.put(key, serializer.apply(curr));
      }
    }, false));
    return obV;
  }

  // ---------------------------------------------------------------------------------------------
  // StaticBlock
  // ---------------------------------------------------------------------------------------------

  static {
    home = Path.of(System.getProperty("DDModsTool.home", "./"));
    properties = Simple.init(() -> {
      Path path = home.resolve("conf.properties");
      Properties properties = new Properties() {
        @Override
        public Set<Map.Entry<Object, Object>> entrySet() {
          return super.entrySet().stream()
              .sorted(Comparator.comparing(entry -> (String) entry.getKey()))
              .collect(Collectors.toCollection(LinkedHashSet::new));
        }
      };
      if (Files.exists(path)) {
        try (InputStream input = IO.input(path)) {
          properties.load(input);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      return properties;
    });
    observerList = new LinkedList<>();

    view = new ConfView();
    game = new ConfGame();
  }

}
