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
import ddmodstool.core.lang.util.IO;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * [ *.skel ] writer.
 *
 * @author wautsns
 * @since 1.0.0
 */
public final class _SkelWriter_ extends DataOutputStream {

  // ---------------------------------------------------------------------------------------------
  // PublicStaticMethods
  // ---------------------------------------------------------------------------------------------

  public static void write(Path path, Skel skel) {
    Path absPath = path.toAbsolutePath();
    try (_SkelWriter_ ignored = new _SkelWriter_(IO.output(absPath), skel)) {
      // write okay
    } catch (Exception e) {
      throw LocX.of(e).with("Skel#path", absPath);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // ---------------------------------------------------------------------------------------------

  private final boolean nonessential;

  // ---------------------------------------------------------------------------------------------
  // PrivateMethods
  // ---------------------------------------------------------------------------------------------

  private void writeSkin(JSONObject skin, boolean writeName) throws IOException {
    if (writeName) {
      writeAsciiString(skin.getString("name"));
    }
    writeArray(skin.getJSONArray("slotArray"), slot -> {
      writeVarint(slot.getInteger("index"));
      writeArray(slot.getJSONArray("attachmentArray"), this::writeAttachment);
    });
  }

  private void writeAnimation(JSONObject animation) throws IOException {
    writeAsciiString(animation.getString("name"));
    writeArray(animation.getJSONArray("slotArray"), slot -> {
      writeVarint(slot.getInteger("index"));
      writeArray(slot.getJSONArray("timelineArray"), this::writeSlotTimeline);
    });
    writeArray(animation.getJSONArray("boneArray"), slot -> {
      writeVarint(slot.getInteger("index"));
      writeArray(slot.getJSONArray("timelineArray"), this::writeBoneTimeline);
    });
    writeArray(animation.getJSONArray("ikTimelineArray"), this::writeIkTimeline);
    writeArray(animation.getJSONArray("ffdArray"), ffd -> {
      writeVarint(ffd.getInteger("index"));
      writeArray(ffd.getJSONArray("slotArray"), slot -> {
        writeVarint(slot.getInteger("index"));
        writeArray(slot.getJSONArray("timelineArray"), this::writeFfdTimeline);
      });
    });
    writeDrawOrderTimeline(animation.getJSONObject("drawOrderTimeline"));
    writeEventTimeline(animation.getJSONObject("eventTimeline"));
  }

  // ---------------------------------------------------------------------------------------------

  private void writeAttachment(JSONObject attachment) throws IOException {
    writeAsciiString(attachment.getString("name1"));
    writeAsciiString(attachment.getString("name2"));
    byte type = attachment.getByte("type");
    writeByte(type);
    switch (type) {
      case 0 /* ATTACHMENT_REGION */ -> {
        writeAsciiString(attachment.getString("path"));
        writeFloat(attachment.getFloat("x"));
        writeFloat(attachment.getFloat("y"));
        writeFloat(attachment.getFloat("scaleX"));
        writeFloat(attachment.getFloat("scaleY"));
        writeFloat(attachment.getFloat("rotation"));
        writeFloat(attachment.getFloat("width"));
        writeFloat(attachment.getFloat("height"));
        writeInt(attachment.getInteger("color"));
      }
      case 1 /* ATTACHMENT_BOUNDING_BOX */ -> {
        writeFloatArray(attachment, "vertexArray");
      }
      case 2 /* ATTACHMENT_MESH */ -> {
        writeAsciiString(attachment.getString("path"));
        writeFloatArray(attachment, "regionUVArray");
        writeShortArray(attachment, "triangleArray");
        writeFloatArray(attachment, "vertexArray");
        writeInt(attachment.getInteger("color"));
        writeVarint(attachment.getInteger("_hullLengthX_"));
        if (nonessential) {
          writeVarintArray(attachment, "edgeArray");
          writeFloat(attachment.getFloat("width"));
          writeFloat(attachment.getFloat("height"));
        }
      }
      case 3 /* ATTACHMENT_SKINNED_MESH */ -> {
        writeAsciiString(attachment.getString("path"));
        writeFloatArray(attachment, "regionUVArray");
        writeShortArray(attachment, "triangleArray");
        writeVarint(attachment.getInteger("vertexN"));
        JSONArray boneArray2d = attachment.getJSONArray("boneArray2d");
        for (int i = 0; i < boneArray2d.size(); i++) {
          JSONArray boneArray = boneArray2d.getJSONArray(i);
          // writeFloat is correct
          writeFloat(boneArray.size());
          for (int j = 0; j < boneArray.size(); j++) {
            JSONObject bone = boneArray.getJSONObject(j);
            // writeFloat is correct
            writeFloat(bone.getInteger("index"));
            writeFloat(bone.getFloat("x"));
            writeFloat(bone.getFloat("y"));
            writeFloat(bone.getFloat("weight"));
          }
        }
        writeInt(attachment.getInteger("color"));
        writeVarint(attachment.getInteger("_hullLengthX_"));
        if (nonessential) {
          writeVarintArray(attachment, "edgeArray");
          writeFloat(attachment.getFloat("width"));
          writeFloat(attachment.getFloat("height"));
        }
      }
      default -> throw LocX.of("Skel!UnexpectedAttachmentType")
          .with("txt[Expect]", "{0,1,2,3}")
          .with("txt[Actual]", type);
    }
  }

  private void writeSlotTimeline(JSONObject timeline) throws IOException {
    byte type = timeline.getByte("type");
    writeByte(type);
    switch (type) {
      case 4 /* TIMELINE_COLOR */ -> {
        writeArray(timeline.getJSONArray("frameArray"), (n, i, frame) -> {
          writeFloat(frame.getFloat("time"));
          writeInt(frame.getInteger("color"));
          if ((i + 1) < n) {
            writeCurve(frame);
          }
        });
      }
      case 3 /* TIMELINE_ATTACHMENT */ -> {
        writeArray(timeline.getJSONArray("frameArray"), frame -> {
          writeFloat(frame.getFloat("time"));
          writeAsciiString(frame.getString("attachmentName"));
        });
      }
      default -> throw LocX.of("Skel!UnexpectedTimelineType")
          .with("txt[Expect]", "{3,4}")
          .with("txt[Actual]", type);
    }
  }

  private void writeBoneTimeline(JSONObject timeline) throws IOException {
    byte type = timeline.getByte("type");
    writeByte(type);
    switch (type) {
      case 1 /* TIMELINE_ROTATE */ -> {
        writeArray(timeline.getJSONArray("frameArray"), (n, i, frame) -> {
          writeFloat(frame.getFloat("time"));
          writeFloat(frame.getFloat("angle"));
          if ((i + 1) < n) {
            writeCurve(frame);
          }
        });
      }
      case 2 /* TIMELINE_TRANSLATE */, 0 /* TIMELINE_SCALE */ -> {
        writeArray(timeline.getJSONArray("frameArray"), (n, i, frame) -> {
          writeFloat(frame.getFloat("time"));
          writeFloat(frame.getFloat("x"));
          writeFloat(frame.getFloat("y"));
          if ((i + 1) < n) {
            writeCurve(frame);
          }
        });
      }
      case 5 /* TIMELINE_FLIP_X */, 6 /* TIMELINE_FLIP_Y */ -> {
        writeArray(timeline.getJSONArray("frameArray"), frame -> {
          writeFloat(frame.getFloat("time"));
          writeBoolean(frame.getBoolean("flip"));
        });
      }
      default -> throw LocX.of("Skel!UnexpectedTimelineType")
          .with("txt[Expect]", "{0,1,2,5,6}")
          .with("txt[Actual]", type);
    }
  }

  private void writeIkTimeline(JSONObject timeline) throws IOException {
    writeVarint(timeline.getInteger("ikIndex"));
    writeArray(timeline.getJSONArray("frameArray"), (n, i, frame) -> {
      writeFloat(frame.getFloat("time"));
      writeFloat(frame.getFloat("min"));
      writeByte(frame.getByte("bendDirection"));
      if ((i + 1) < n) {
        writeCurve(frame);
      }
    });
  }

  private void writeFfdTimeline(JSONObject timeline) throws IOException {
    writeAsciiString(timeline.getString("attachmentName"));
    writeArray(timeline.getJSONArray("frameArray"), (n, i, frame) -> {
      writeFloat(frame.getFloat("time"));
      int end = frame.getInteger("end");
      writeVarint(end);
      if (end != 0) {
        writeVarint(frame.getInteger("start"));
        for (float vertex : frame.getObject("vertexArray", float[].class)) {
          writeFloat(vertex);
        }
      }
      if ((i + 1) < n) {
        writeCurve(frame);
      }
    });
  }

  private void writeDrawOrderTimeline(JSONObject timeline) throws IOException {
    writeNullableArray(timeline.getJSONArray("frameArray"), frame -> {
      writeArray(frame.getJSONArray("slotArray"), slot -> {
        writeVarint(slot.getInteger("index"));
        writeVarint(slot.getInteger("offset"));
      });
      writeFloat(frame.getFloat("time"));
    });
  }

  private void writeEventTimeline(JSONObject timeline) throws IOException {
    writeNullableArray(timeline.getJSONArray("frameArray"), frame -> {
      writeFloat(frame.getFloat("time"));
      writeVarint(frame.getInteger("eventIndex"));
      writeVarint(frame.getInteger("_intValue_"));
      writeFloat(frame.getFloat("floatValue"));
      boolean hasStringValue = frame.getBoolean("hasStringValue");
      writeBoolean(hasStringValue);
      if (hasStringValue) {
        writeAsciiString(frame.getString("stringValue"));
      }
    });
  }

  private void writeCurve(JSONObject frame) throws IOException {
    int type = frame.getInteger("curveType");
    writeByte(type);
    if (type == 2 /* CURVE_BEZIER */) {
      writeFloat(frame.getFloat("cx1"));
      writeFloat(frame.getFloat("cy1"));
      writeFloat(frame.getFloat("cx2"));
      writeFloat(frame.getFloat("cy2"));
    }
  }

  // ---------------------------------------------------------------------------------------------

  private void writeVarint(int value) throws IOException {
    while (true) {
      byte b = (byte) (value & 0x7F);
      value = value >>> 7;
      if (value != 0) {
        writeByte(b | 0x80);
      } else {
        writeByte(b);
        break;
      }
    }
  }

  private void writeAsciiString(String value) throws IOException {
    if (value == null) {
      writeVarint(0);
    } else {
      writeVarint(value.length() + 1);
      for (int i = 0, l = value.length(); i < l; i++) {
        writeByte(value.charAt(i));
      }
    }
  }

  private void writeShortArray(JSONObject object, String field) throws IOException {
    short[] array = object.getObject(field, short[].class);
    writeVarint(array.length);
    for (short value : array) {
      writeShort(value);
    }
  }

  private void writeVarintArray(JSONObject object, String field) throws IOException {
    int[] array = object.getObject(field, int[].class);
    writeVarint(array.length);
    for (int value : array) {
      writeVarint(value);
    }
  }

  private void writeFloatArray(JSONObject object, String field) throws IOException {
    float[] array = object.getObject(field, float[].class);
    writeVarint(array.length);
    for (float value : array) {
      writeFloat(value);
    }
  }

  private void writeArray(JSONArray array, Writer writer) throws IOException {
    int n = array.size();
    writeVarint(n);
    for (int i = 0; i < n; i++) {
      writer.write(array.getJSONObject(i));
    }
  }

  private void writeArray(JSONArray array, IndexedWriter writer) throws IOException {
    int n = array.size();
    writeVarint(n);
    for (int i = 0; i < n; i++) {
      writer.write(n, i, array.getJSONObject(i));
    }
  }

  private void writeNullableArray(JSONArray array, Writer writer) throws IOException {
    if (array == null) {
      writeVarint(-1);
    } else {
      writeArray(array, writer);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateConstructors
  // ---------------------------------------------------------------------------------------------

  private _SkelWriter_(OutputStream output, Skel skel) throws IOException {
    super(output);
    this.nonessential = skel.getBoolean("nonessential");
    // --- do write ---
    writeAsciiString(skel.getString("hash"));
    writeAsciiString(skel.getString("version"));
    writeFloat(skel.getFloat("width"));
    writeFloat(skel.getFloat("height"));
    writeBoolean(nonessential);
    if (nonessential) {
      writeAsciiString(skel.getString("imagesPath"));
    }
    writeArray(skel.getJSONArray("boneArray"), bone -> {
      writeAsciiString(bone.getString("name"));
      writeVarint(bone.getInteger("parentId"));
      writeFloat(bone.getFloat("x"));
      writeFloat(bone.getFloat("y"));
      writeFloat(bone.getFloat("scaleX"));
      writeFloat(bone.getFloat("scaleY"));
      writeFloat(bone.getFloat("rotation"));
      writeFloat(bone.getFloat("length"));
      writeBoolean(bone.getBoolean("flipX"));
      writeBoolean(bone.getBoolean("flipY"));
      writeBoolean(bone.getBoolean("inheritScale"));
      writeBoolean(bone.getBoolean("inheritRotation"));
      if (nonessential) {
        writeInt(bone.getInteger("color"));
      }
    });
    writeArray(skel.getJSONArray("ikArray"), ik -> {
      writeAsciiString(ik.getString("name"));
      writeVarintArray(ik, "boneIndexArray");
      writeVarint(ik.getInteger("targetBoneIndex"));
      writeFloat(ik.getFloat("mix"));
      writeByte(ik.getByte("bendDirection"));
    });
    writeArray(skel.getJSONArray("slotArray"), slot -> {
      writeAsciiString(slot.getString("name"));
      writeVarint(slot.getInteger("boneIndex"));
      writeInt(slot.getInteger("color"));
      writeAsciiString(slot.getString("attachmentName"));
      writeVarint(slot.getInteger("blendMode"));
    });
    writeSkin(skel.getJSONObject("defaultSkin"), false);
    writeArray(skel.getJSONArray("skinArray"), skin -> writeSkin(skin, true));
    writeArray(skel.getJSONArray("eventArray"), event -> {
      writeAsciiString(event.getString("name"));
      writeVarint(event.getInteger("_intValue_"));
      writeFloat(event.getFloat("floatValue"));
      writeAsciiString(event.getString("stringValue"));
    });
    writeArray(skel.getJSONArray("animationArray"), this::writeAnimation);
  }

  // ---------------------------------------------------------------------------------------------
  // PrivateStaticClasses
  // ---------------------------------------------------------------------------------------------

  private interface Writer {

    void write(JSONObject value) throws IOException;

  }

  private interface IndexedWriter {

    void write(int n, int i, JSONObject value) throws IOException;

  }

}
