package de.janmm14.mcunblockerultimate.ping;

import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;

public class Util {

	public static int readVarInt(ByteBuf input) {
		return readVarInt(input, 5);
	}

	public static int readVarInt(ByteBuf input, int maxBytes) {
		int out = 0;
		int bytes = 0;
		byte in;
		while (true) {
			in = input.readByte();

			out |= (in & 0x7F) << (bytes++ * 7);

			if (bytes > maxBytes) {
				throw new RuntimeException("VarInt too big");
			}

			if ((in & 0x80) != 0x80) {
				break;
			}
		}

		return out;
	}

	public static void writeVarInt(int value, ByteBuf output) {
		int part;
		while (true) {
			part = value & 0x7F;

			value >>>= 7;
			if (value != 0) {
				part |= 0x80;
			}

			output.writeByte(part);

			if (value == 0) {
				break;
			}
		}
	}

	public static void writeString(String s, ByteBuf buf) {
		if (s.length() > Short.MAX_VALUE) {
			throw new RuntimeException(String.format("Cannot send string longer than Short.MAX_VALUE (got %s characters)", s.length()));
		}

		byte[] b = s.getBytes(Charsets.UTF_8);
		writeVarInt(b.length, buf);
		buf.writeBytes(b);
	}

	public static String readString(ByteBuf buf) {
		int len = readVarInt(buf);
		if (len > Short.MAX_VALUE * 4) {
			throw new RuntimeException(String.format("Cannot receive string longer than Short.MAX_VALUE (got %s characters)", len));
		}

		byte[] b = new byte[len];
		buf.readBytes(b);

		return new String(b, Charsets.UTF_8);
	}
}
