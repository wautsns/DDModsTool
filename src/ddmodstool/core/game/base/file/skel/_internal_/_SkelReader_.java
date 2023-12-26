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
package ddmodstool.core.game.base.file.skel._internal_;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import ddmodstool.core.game.base.data.localization.LocX;
import ddmodstool.core.game.base.file.skel.Skel;
import ddmodstool.core.game.base.file.skel.Skel.Animation;
import ddmodstool.core.lang.util.IO;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * [ *.skel ] reader.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class _SkelReader_ extends DataInputStream {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static Skel read(Path path) {
    Path absPath = path.toAbsolutePath();
    try (_SkelReader_ r = new _SkelReader_(absPath)) {
      return r.result;
    } catch (Exception e) {
      throw LocX.of(e).with("Skel#path", absPath);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private final Skel result = new Skel();

  private final boolean nonessential;

  // ---------------------------------------------------------------------------------------------
  // PrivateMethods
  // ---------------------------------------------------------------------------------------------

  private JSONObject readSkin(String name) throws IOException {
    JSONObject skin = new JSONObject();
    skin.put("name", name);
    skin.put("slotArray", readArray(() -> {
      JSONObject slot = new JSONObject();
      slot.put("index", readVarint());
      slot.put("attachmentArray", readArray(this::readAttachment));
      return slot;
    }));
    return skin;
  }

  private Animation readAnimation() throws IOException {
    Animation animation = new Animation();
    animation.put("name", readAsciiString());
    animation.put("slotArray", readArray(() -> {
      JSONObject slot = new JSONObject();
      slot.put("index", readVarint());
      slot.put("timelineArray", readArray(this::readSlotTimeline));
      return slot;
    }));
    animation.put("boneArray", readArray(() -> {
      JSONObject bone = new JSONObject();
      bone.put("index", readVarint());
      bone.put("timelineArray", readArray(this::readBoneTimeline));
      return bone;
    }));
    animation.put("ikTimelineArray", readArray(this::readIkTimeline));
    animation.put("ffdArray", readArray(() -> {
      JSONObject ffd = new JSONObject();
      ffd.put("index", readVarint());
      ffd.put("slotArray", readArray(() -> {
        JSONObject slot = new JSONObject();
        slot.put("index", readVarint());
        slot.put("timelineArray", readArray(this::readFfdTimeline));
        return slot;
      }));
      return ffd;
    }));
    animation.put("drawOrderTimeline", readDrawOrderTimeline());
    animation.put("eventTimeline", readEventTimeline());
    return animation;
  }

  // ---------------------------------------------------------------------------------------------

  private JSONObject readAttachment() throws IOException {
    JSONObject attachment = new JSONObject();
    attachment.put("name1", readAsciiString());
    attachment.put("name2", readAsciiString());
    byte type = readByte();
    attachment.put("type", type);
    return switch (type) {
      case 0 /* ATTACHMENT_REGION */ -> {
        attachment.put("path", readAsciiString());
        attachment.put("x", readFloat());
        attachment.put("y", readFloat());
        attachment.put("scaleX", readFloat());
        attachment.put("scaleY", readFloat());
        attachment.put("rotation", readFloat());
        attachment.put("width", readFloat());
        attachment.put("height", readFloat());
        attachment.put("color", readInt());
        yield attachment;
      }
      case 1 /* ATTACHMENT_BOUNDING_BOX */ -> {
        attachment.put("vertexArray", readFloatArray());
        yield attachment;
      }
      case 2 /* ATTACHMENT_MESH */ -> {
        attachment.put("path", readAsciiString());
        attachment.put("regionUVArray", readFloatArray());
        attachment.put("triangleArray", readShortArray());
        attachment.put("vertexArray", readFloatArray());
        attachment.put("color", readInt());
        // hullLength = _hullLengthX_ * 2
        attachment.put("_hullLengthX_", readVarint());
        if (nonessential) {
          attachment.put("edgeArray", readVarintArray());
          attachment.put("width", readFloat());
          attachment.put("height", readFloat());
        }
        yield attachment;
      }
      case 3 /* ATTACHMENT_SKINNED_MESH */ -> {
        attachment.put("path", readAsciiString());
        attachment.put("regionUVArray", readFloatArray());
        attachment.put("triangleArray", readShortArray());
        int vertexN = readVarint();
        attachment.put("vertexN", vertexN);
        JSONArray boneArray2d = new JSONArray();
        for (int i = 0; i < vertexN; i++) {
          // readFloat is correct
          int boneArrayN = (int) readFloat();
          JSONArray boneArray = new JSONArray(boneArrayN);
          for (int j = 0; j < boneArrayN; j++, i += 4) {
            JSONObject bone = new JSONObject();
            // readFloat is correct
            bone.put("index", (int) readFloat());
            bone.put("x", readFloat());
            bone.put("y", readFloat());
            bone.put("weight", readFloat());
            boneArray.add(bone);
          }
          boneArray2d.add(boneArray);
        }
        attachment.put("boneArray2d", boneArray2d);
        attachment.put("color", readInt());
        // hullLength = _hullLengthX_ * 2
        attachment.put("_hullLengthX_", readVarint());
        if (nonessential) {
          attachment.put("edgeArray", readVarintArray());
          attachment.put("width", readFloat());
          attachment.put("height", readFloat());
        }
        yield attachment;
      }
      default -> throw LocX.of("Skel!UnexpectedAttachmentType")
          .with("txt[Expect]", "{0,1,2,3}")
          .with("txt[Actual]", type);
    };
  }

  private JSONObject readSlotTimeline() throws IOException {
    JSONObject timeline = new JSONObject();
    byte type = readByte();
    timeline.put("type", type);
    return switch (type) {
      case 4 /* TIMELINE_COLOR */ -> {
        timeline.put("frameArray", readArray((n, i) -> {
          JSONObject frame = new JSONObject();
          frame.put("time", readFloat());
          frame.put("color", readInt());
          if ((i + 1) < n) {
            readAndSetCurve(frame);
          }
          return frame;
        }));
        yield timeline;
      }
      case 3 /* TIMELINE_ATTACHMENT */ -> {
        timeline.put("frameArray", readArray(() -> {
          JSONObject frame = new JSONObject();
          frame.put("time", readFloat());
          frame.put("attachmentName", readAsciiString());
          return frame;
        }));
        yield timeline;
      }
      default -> throw LocX.of("Skel!UnexpectedTimelineType")
          .with("txt[Expect]", "{3,4}")
          .with("txt[Actual]", type);
    };
  }

  private JSONObject readBoneTimeline() throws IOException {
    JSONObject timeline = new JSONObject();
    byte type = readByte();
    timeline.put("type", type);
    return switch (type) {
      case 1 /* TIMELINE_ROTATE */ -> {
        timeline.put("frameArray", readArray((n, i) -> {
          JSONObject frame = new JSONObject();
          frame.put("time", readFloat());
          frame.put("angle", readFloat());
          if ((i + 1) < n) {
            readAndSetCurve(frame);
          }
          return frame;
        }));
        yield timeline;
      }
      case 2 /* TIMELINE_TRANSLATE */, 0 /* TIMELINE_SCALE */ -> {
        timeline.put("frameArray", readArray((n, i) -> {
          JSONObject frame = new JSONObject();
          frame.put("time", readFloat());
          frame.put("x", readFloat());
          frame.put("y", readFloat());
          if ((i + 1) < n) {
            readAndSetCurve(frame);
          }
          return frame;
        }));
        yield timeline;
      }
      case 5 /* TIMELINE_FLIP_X */, 6 /* TIMELINE_FLIP_Y */ -> {
        timeline.put("frameArray", readArray((n, i) -> {
          JSONObject frame = new JSONObject();
          frame.put("time", readFloat());
          frame.put("flip", readBoolean());
          return frame;
        }));
        yield timeline;
      }
      default -> throw LocX.of("Skel!UnexpectedTimelineType")
          .with("txt[Expect]", "{0,1,2,5,6}")
          .with("txt[Actual]", type);
    };
  }

  private JSONObject readIkTimeline() throws IOException {
    JSONObject timeline = new JSONObject();
    timeline.put("ikIndex", readVarint());
    timeline.put("frameArray", readArray((n, i) -> {
      JSONObject frame = new JSONObject();
      frame.put("time", readFloat());
      frame.put("min", readFloat());
      frame.put("bendDirection", readByte());
      if ((i + 1) < n) {
        readAndSetCurve(frame);
      }
      return frame;
    }));
    return timeline;
  }

  private JSONObject readFfdTimeline() throws IOException {
    JSONObject timeline = new JSONObject();
    timeline.put("attachmentName", readAsciiString());
    timeline.put("frameArray", readArray((n, i) -> {
      JSONObject frame = new JSONObject();
      frame.put("time", readFloat());
      int end = readVarint();
      frame.put("end", end);
      if (end != 0) {
        frame.put("start", readVarint());
        frame.put("vertexArray", readFloatArray(end));
      }
      if ((i + 1) < n) {
        readAndSetCurve(frame);
      }
      return frame;
    }));
    return timeline;
  }

  private JSONObject readDrawOrderTimeline() throws IOException {
    JSONObject timeline = new JSONObject();
    timeline.put("frameArray", readNullableArray(() -> {
      JSONObject frame = new JSONObject();
      frame.put("slotArray", readArray(() -> {
        JSONObject slot = new JSONObject();
        slot.put("index", readVarint());
        slot.put("offset", readVarint());
        return slot;
      }));
      frame.put("time", readFloat());
      return frame;
    }));
    return timeline;
  }

  private JSONObject readEventTimeline() throws IOException {
    JSONObject timeline = new JSONObject();
    timeline.put("frameArray", readNullableArray(() -> {
      JSONObject frame = new JSONObject();
      frame.put("time", readFloat());
      frame.put("eventIndex", readVarint());
      // intValue = ((_intValue_ >>> 1) ^ -(_intValue_ & 1))
      frame.put("_intValue_", readVarint());
      frame.put("floatValue", readFloat());
      boolean hasStringValue = readBoolean();
      frame.put("hasStringValue", hasStringValue);
      if (hasStringValue) {
        frame.put("stringValue", readAsciiString());
      }
      return frame;
    }));
    return timeline;
  }

  // ---------------------------------------------------------------------------------------------

  private void readAndSetCurve(JSONObject frame) throws IOException {
    byte type = readByte();
    frame.put("curveType", type);
    if (type == 2 /* CURVE_BEZIER */) {
      frame.put("cx1", readFloat());
      frame.put("cy1", readFloat());
      frame.put("cx2", readFloat());
      frame.put("cy2", readFloat());
    }
  }

  // ---------------------------------------------------------------------------------------------

  private int readVarint() throws IOException {
    int b = readByte();
    int varint = b & 0x7F;
    if ((b & 0x80) != 0) {
      b = readByte();
      varint |= (b & 0x7F) << 7;
      if ((b & 0x80) != 0) {
        b = readByte();
        varint |= (b & 0x7F) << 14;
        if ((b & 0x80) != 0) {
          b = readByte();
          varint |= (b & 0x7F) << 21;
          if ((b & 0x80) != 0) {
            b = readByte();
            varint |= (b & 0x7F) << 28;
          }
        }
      }
    }
    return varint;
  }

  private String readAsciiString() throws IOException {
    int n = readVarint();
    if (n == 0) {
      return null;
    } else if (n == 1) {
      return "";
    }
    n = n - 1;
    StringBuilder bu = new StringBuilder(n);
    for (int i = 0; i < n; i++) {
      bu.append((char) (readByte() & 0xFF));
    }
    return bu.toString();
  }

  private short[] readShortArray() throws IOException {
    int n = readVarint();
    short[] array = new short[n];
    for (int i = 0; i < n; i++) {
      array[i] = readShort();
    }
    return array;
  }

  private int[] readVarintArray() throws IOException {
    int n = readVarint();
    int[] array = new int[n];
    for (int i = 0; i < n; i++) {
      array[i] = readVarint();
    }
    return array;
  }

  private float[] readFloatArray() throws IOException {
    return readFloatArray(readVarint());
  }

  private float[] readFloatArray(int n) throws IOException {
    float[] array = new float[n];
    for (int i = 0; i < n; i++) {
      array[i] = readFloat();
    }
    return array;
  }

  private JSONArray readArray(Reader reader) throws IOException {
    int n = readVarint();
    JSONArray array = new JSONArray(n);
    for (int i = 0; i < n; i++) {
      array.add(reader.read());
    }
    return array;
  }

  private JSONArray readArray(IndexedReader reader) throws IOException {
    int n = readVarint();
    JSONArray array = new JSONArray(n);
    for (int i = 0; i < n; i++) {
      array.add(reader.read(n, i));
    }
    return array;
  }

  private JSONArray readNullableArray(Reader reader) throws IOException {
    int n = readVarint();
    if (n == -1) {
      return null;
    }
    JSONArray array = new JSONArray(n);
    for (int i = 0; i < n; i++) {
      array.add(reader.read());
    }
    return array;
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateConstructors
  // ---------------------------------------------------------------------------------------------

  private _SkelReader_(Path path) throws IOException {
    super(IO.input(path));
    result.setPath(path);
    // --- do read ---
    String filename = IO.name(path);
    Matcher filenameMatcher = Pattern.compile("([^.]+)\\.sprite\\.(.*)\\.skel").matcher(filename);
    if (!filenameMatcher.find()) {
      throw LocX.of("Skel!InvalidFilename")
          .with("txt[Expect]", "*.sprite.*.skel")
          .with("txt[Actual]", filename);
    }
    result.setOwner(filenameMatcher.group(1));
    result.setIdent(filenameMatcher.group(2));
    result.put("hash", readAsciiString());
    result.put("version", readAsciiString());
    result.put("width", readFloat());
    result.put("height", readFloat());
    nonessential = readBoolean();
    result.put("nonessential", nonessential);
    if (nonessential) {
      result.put("imagesPath", readAsciiString());
    }
    result.put("boneArray", readArray(() -> {
      JSONObject bone = new JSONObject();
      bone.put("name", readAsciiString());
      bone.put("parentId", readVarint());
      bone.put("x", readFloat());
      bone.put("y", readFloat());
      bone.put("scaleX", readFloat());
      bone.put("scaleY", readFloat());
      bone.put("rotation", readFloat());
      bone.put("length", readFloat());
      bone.put("flipX", readBoolean());
      bone.put("flipY", readBoolean());
      bone.put("inheritScale", readBoolean());
      bone.put("inheritRotation", readBoolean());
      if (nonessential) {
        bone.put("color", readInt());
      }
      return bone;
    }));
    result.put("ikArray", readArray(() -> {
      JSONObject ik = new JSONObject();
      ik.put("name", readAsciiString());
      ik.put("boneIndexArray", readVarintArray());
      ik.put("targetBoneIndex", readVarint());
      ik.put("mix", readFloat());
      ik.put("bendDirection", readByte());
      return ik;
    }));
    result.put("slotArray", readArray(() -> {
      JSONObject slot = new JSONObject();
      slot.put("name", readAsciiString());
      slot.put("boneIndex", readVarint());
      slot.put("color", readInt());
      slot.put("attachmentName", readAsciiString());
      slot.put("blendMode", readVarint());
      return slot;
    }));
    result.put("defaultSkin", readSkin("default"));
    result.put("skinArray", readArray(() -> readSkin(readAsciiString())));
    result.put("eventArray", readArray(() -> {
      JSONObject event = new JSONObject();
      event.put("name", readAsciiString());
      // intValue = ((_intValue_ >>> 1) ^ -(_intValue_ & 1))
      event.put("_intValue_", readVarint());
      event.put("floatValue", readFloat());
      event.put("stringValue", readAsciiString());
      return event;
    }));
    result.put("animationArray", readArray(this::readAnimation));
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateStaticClasses
  // ---------------------------------------------------------------------------------------------

  private interface Reader {

    JSONObject read() throws IOException;

  }

  private interface IndexedReader {

    JSONObject read(int n, int i) throws IOException;

  }

}
