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
package ddmodstool.core.game.base.file.darkest;

import static java.lang.String.format;

import ddmodstool.core.game.base.data.localization.LocX;
import ddmodstool.core.game.base.file.darkest._internal_._DarkestReader_;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * [ *.darkest ] object.
 *
 * @author wautsns
 * @since 1.0.0
 */
@Getter
@Setter
public final class Darkest {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static Darkest read(Path path) {
    return _DarkestReader_.read(path);
  }

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private Path path;
  // lineType -> lineList
  private Map<String, List<Line>> lineListMap;

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public Path reqPath() {
    Path path = this.path;
    if (path == null) {
      throw LocX.of("Darkest!Uninitialized");
    }
    return path;
  }

  public Map<String, List<Line>> reqLineListMap() {
    Map<String, List<Line>> lineListMap = this.lineListMap;
    if (lineListMap == null) {
      throw LocX.of("Darkest!Uninitialized");
    }
    return lineListMap;
  }

  // ---------------------------------------------------------------------------------------------

  public Line getLine(String type) {
    List<Line> lineList = getLineList(type, 1);
    return (lineList == null) ? null : lineList.get(0);
  }

  public Line getLine(String type, String uniqueItemName, String uniqueItemValue) {
    List<Line> lineList = getLineList(type);
    if (lineList == null) {
      return null;
    }
    Line lineToReturn = null;
    for (Iterator<Line> itr = lineList.iterator(); itr.hasNext(); ) {
      Line line = itr.next();
      if (!line.reqItemValue(uniqueItemName).equals(uniqueItemValue)) {
        continue;
      }
      if (lineToReturn == null) {
        lineToReturn = line;
      } else {
        itr.remove();
        Map<String, Item> itemMapToReturn = lineToReturn.reqItemMap();
        for (Item item : line.reqItemMap().values()) {
          Item itemToReturn = lineToReturn.getItem(item.reqName());
          if (itemToReturn == null) {
            itemMapToReturn.put(item.reqName(), item);
          } else if (!itemToReturn.reqValueList().equals(item.reqValueList())) {
            throw LocX.of("Darkest!ConflictingItems")
                .with("Darkest#path", path)
                .with("Darkest#lineType", line.getType())
                .with("Darkest#itemName", item.getName());
          }
        }
      }
    }
    return lineToReturn;
  }

  public List<Line> getLineList(String type) {
    return reqLineListMap().get(type);
  }

  public List<Line> getLineList(String type, int size) {
    return getLineList(type, size, size);
  }

  public List<Line> getLineList(String type, int minSize, int maxSize) {
    List<Line> lineList = getLineList(type);
    if (lineList == null) {
      return null;
    }
    if ((lineList.size() < minSize) || (lineList.size() > maxSize)) {
      throw LocX.of("Darkest!UnexpectedSizeOfLineList")
          .with("Darkest#path", path)
          .with("Darkest#lineType", type)
          .with("txt[Expect]", (minSize == maxSize) ?
              Integer.toString(minSize) : format("[%d,%d]", minSize, maxSize))
          .with("txt[Actual]", lineList.size());
    }
    return lineList;
  }

  // ---------------------------------------------------------------------------------------------

  public Line reqLine(String type) {
    return reqLineList(type, 1).get(0);
  }

  public Line reqLine(String type, String uniqueItemName, String uniqueItemValue) {
    Line line = getLine(type, uniqueItemName, uniqueItemValue);
    if (line == null) {
      throw LocX.of("Darkest!MissingLine")
          .with("Darkest#path", path)
          .with("Darkest#lineType", type)
          .with("Darkest#itemName", uniqueItemName)
          .with("Darkest#itemValue", uniqueItemValue);
    }
    return line;
  }

  public List<Line> reqLineList(String type) {
    List<Line> lineList = getLineList(type);
    if (lineList == null) {
      throw LocX.of("Darkest!MissingLine")
          .with("Darkest#path", path)
          .with("Darkest#lineType", type);
    }
    return lineList;
  }

  public List<Line> reqLineList(String type, int size) {
    return reqLineList(type, size, size);
  }

  public List<Line> reqLineList(String type, int minSize, int maxSize) {
    List<Line> lineList = getLineList(type, minSize, maxSize);
    if (lineList == null) {
      throw LocX.of("Darkest!MissingLine")
          .with("Darkest#path", path)
          .with("Darkest#lineType", type);
    }
    return lineList;
  }

  // ---------------------------------------------------------------------------------------------

  public void mergeLinesWithTheSameTypeAndItem(String type, String itemName) {
    List<Line> lineList = getLineList(type);
    if ((lineList == null) || (lineList.size() == 1)) {
      return;
    }
    Map<String, Line> mergedLineMap = new LinkedHashMap<>();
    for (Iterator<Line> itr = lineList.iterator(); itr.hasNext(); ) {
      Line line = itr.next();
      String itemValue = line.reqItemValue(itemName);
      Line mergedLine = mergedLineMap.get(itemValue);
      if (mergedLine == null) {
        mergedLineMap.put(itemValue, line);
      } else {
        itr.remove();
        Map<String, Item> mergedItemMap = mergedLine.reqItemMap();
        for (Item item : line.reqItemMap().values()) {
          Item mergedItem = mergedItemMap.get(item.reqName());
          if (mergedItem == null) {
            mergedItemMap.put(item.reqName(), item);
          } else if (!mergedItem.reqValueList().equals(item.reqValueList())) {
            throw LocX.of("Darkest!ConflictingItems")
                .with("Darkest#path", path)
                .with("Darkest#lineType", line.getType())
                .with("Darkest#itemName", item.getName());
          }
        }
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // PublicStaticClasses
  // ---------------------------------------------------------------------------------------------

  @Getter
  @Setter
  public static final class Line {

    private String type;
    // itemName -> item
    private Map<String, Item> itemMap;

    // -----------------------------------------------------------------------------------------
    // PublicMethods
    // -----------------------------------------------------------------------------------------

    public String reqType() {
      String type = this.type;
      if (type == null) {
        throw LocX.of("Darkest!Uninitialized");
      }
      return type;
    }

    public Map<String, Item> reqItemMap() {
      Map<String, Item> itemMap = this.itemMap;
      if (itemMap == null) {
        throw LocX.of("Darkest!Uninitialized");
      }
      return itemMap;
    }

    // -----------------------------------------------------------------------------------------

    public Item getItem(String name) {
      return reqItemMap().get(name);
    }

    public String getItemValue(String name) {
      Item item = getItem(name);
      return (item == null) ? null : item.reqValue();
    }

    public List<String> getItemValueList(String name) {
      Item item = getItem(name);
      return (item == null) ? null : item.reqValueList();
    }

    public List<String> getItemValueList(String name, int size) {
      Item item = getItem(name);
      return (item == null) ? null : item.reqValueList(size);
    }

    public List<String> getItemValueList(String name, int minSize, int maxSize) {
      Item item = getItem(name);
      return (item == null) ? null : item.reqValueList(minSize, maxSize);
    }

    // -----------------------------------------------------------------------------------------

    public Item reqItem(String name) {
      Item item = reqItemMap().get(name);
      if (item == null) {
        throw LocX.of("Darkest!MissingItem")
            .with("Darkest#lineType", type)
            .with("Darkest#itemName", name);
      }
      return item;
    }

    public String reqItemValue(String name) {
      return reqItem(name).reqValue();
    }

    public List<String> reqItemValueList(String name) {
      return reqItem(name).reqValueList();
    }

    public List<String> reqItemValueList(String name, int size) {
      return reqItem(name).reqValueList(size);
    }

    public List<String> reqItemValueList(String name, int minSize, int maxSize) {
      return reqItem(name).reqValueList(minSize, maxSize);
    }

  }

  @Getter
  @Setter
  public static final class Item {

    private String name;
    private List<String> valueList;

    // -----------------------------------------------------------------------------------------
    // PublicMethods
    // -----------------------------------------------------------------------------------------

    public String reqName() {
      String name = this.name;
      if (name == null) {
        throw LocX.of("Darkest!Uninitialized");
      }
      return name;
    }

    public List<String> reqValueList() {
      List<String> valueList = this.valueList;
      if (valueList == null) {
        throw LocX.of("Darkest!Uninitialized");
      }
      return valueList;
    }

    // -----------------------------------------------------------------------------------------

    public String reqValue() {
      return reqValueList(1).get(0);
    }

    public List<String> reqValueList(int size) {
      return reqValueList(size, size);
    }

    public List<String> reqValueList(int minSize, int maxSize) {
      List<String> valueList = reqValueList();
      if ((valueList.size() < minSize) || (valueList.size() > maxSize)) {
        throw LocX.of("Darkest!UnexpectedSizeOfItemValueList")
            .with("Darkest#itemName", name)
            .with("txt[Expect]", (minSize == maxSize) ?
                Integer.toString(minSize) : format("[%d,%d]", minSize, maxSize))
            .with("txt[Actual]", valueList.size());
      }
      return valueList;
    }

  }

}
