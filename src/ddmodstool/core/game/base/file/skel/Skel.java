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
package ddmodstool.core.game.base.file.skel;

import com.alibaba.fastjson2.JSONObject;
import ddmodstool.core.game.base.data.localization.LocX;
import ddmodstool.core.game.base.file.skel._internal_._SkelReader_;
import ddmodstool.core.game.base.file.skel._internal_._SkelWriter_;
import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * [ {owner}.sprite.{ident}.skel ] object.
 *
 * @author wautsns
 * @since 1.0.0
 */
@Getter
@Setter
public final class Skel extends JSONObject {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static Skel read(Path path) {
    return _SkelReader_.read(path);
  }

  public static void write(Path path, Skel skel) {
    _SkelWriter_.write(path, skel);
  }

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private Path path;

  private String owner;
  private String ident;

  // ---------------------------------------------------------------------------------------------
  // PublicMethods
  // ---------------------------------------------------------------------------------------------

  public Path reqPath() {
    Path path = this.path;
    if (path == null) {
      throw LocX.of("Skel!Uninitialized");
    }
    return path;
  }

  public String reqOwner() {
    String owner = this.owner;
    if (owner == null) {
      throw LocX.of("Skel!Uninitialized");
    }
    return owner;
  }

  public String reqIdent() {
    String ident = this.ident;
    if (ident == null) {
      throw LocX.of("Skel!Uninitialized");
    }
    return ident;
  }

  // ---------------------------------------------------------------------------------------------

  public List<Animation> reqAnimationList() {
    // noinspection unchecked
    List<Animation> animationList = (List<Animation>) get("animationArray");
    if (animationList == null) {
      throw LocX.of("Skel!Uninitialized");
    }
    return animationList;
  }

  public List<String> reqAnimationNameList() {
    return reqAnimationList().stream().map(Animation::reqName).toList();
  }

  // ---------------------------------------------------------------------------------------------
  // PublicStaticClasses
  // ---------------------------------------------------------------------------------------------

  public static final class Animation extends JSONObject {

    // -----------------------------------------------------------------------------------------
    // PublicMethods
    // -----------------------------------------------------------------------------------------

    public String reqName() {
      String name = getString("name");
      if (name == null) {
        throw LocX.of("Skel!Uninitialized");
      }
      return name;
    }

    public String setName(String name) {
      return (String) put("name", name);
    }

    // -----------------------------------------------------------------------------------------
    // @Override PublicMethods, JSONObject
    // -----------------------------------------------------------------------------------------

    @Override
    public Animation clone() {
      Animation copy = new Animation();
      copy.putAll(this);
      return copy;
    }

  }

}
