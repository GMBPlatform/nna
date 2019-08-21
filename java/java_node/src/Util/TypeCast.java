package Util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;

import Main.Global;

import io.netty.buffer.ByteBuf;

public class TypeCast {
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private Global global;
	
	public TypeCast(Global global) {
		this.global = global;
	}
	
	// HexByteArray to String
	public String ByteHexToString(byte[] HexByte) {

		char[] hexChars = new char[HexByte.length * 2];
		for (int i = 0; i < HexByte.length; i++) {
			int v = HexByte[i] & 0xff;
			hexChars[i * 2] = hexArray[v >>> 4];
			hexChars[i * 2 + 1] = hexArray[v & 0x0f];
		}
		return new String(hexChars);
	}

	// String to HexString
	public String StringToHexString(String s) {
		String HexString = "";
		for (int i = 0; i < s.length(); i++) {
			HexString += String.format("%02X ", (int) s.charAt(i));
		}
		return HexString;
	}

	public byte[] StringToByteArray(String str, int notation) {
		return new java.math.BigInteger(str, notation).toByteArray();
	}

	public int ByteToUnsignedInt(byte b) {
		return (int) b & 0xff;
	}

	public long ByteArrayToLong(byte[] ByteArray) {
		return ByteBuffer.wrap(ByteArray).getLong();
	}

	public int ByteArrayToInt(byte[] ByteArray) {
		return ByteBuffer.wrap(ByteArray).getInt();
	}

	public int ByteArrayToShort(byte[] ByteArray) {
		return ByteBuffer.wrap(ByteArray).getShort();
	}

	public String ByteArrayToUTF8String(byte[] ByteArray) {
		return new String(ByteArray, Charset.forName("UTF-8"));
	}

	// Byte[] to Int
	public int ByteArrayToInt2(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.put(bytes);
		buffer.flip();// need flip
		return buffer.getInt();
	}

	// Byte[] to Long
	public long ByteArrayToLong2(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();// need flip
		return buffer.getLong();
	}

	public byte[] ByteBufToByteArr(ByteBuf msg) {
		int length = msg.readableBytes();
		byte[] byteArr = new byte[length];
		for (int i = 0; i < length; i++) {
			byteArr[i] = msg.getByte(i);
		}
		return byteArr;
	}

	// Hex String to Byte Array
	public byte[] HexStrToByteArray(String hex) {
		if (hex == null || hex.length() == 0)
			return null;
		byte[] ba = new byte[hex.length() / 2];
		for (int i = 0; i < ba.length; i++) {
			ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return ba;
	}

	public String ByteBufToString(ByteBuf msg) {
		int length = msg.readableBytes();
		byte[] m_Msg = new byte[length];
		m_Msg = new byte[length];

		for (int i = 0; i < length; i++) {
			m_Msg[i] = msg.getByte(i);
		}

		StringBuilder sb = new StringBuilder();
		for (final byte b : m_Msg)
			sb.append(String.format("%02x", b & 0xff));
		m_Msg = null;
		return sb.toString();
	}

	public int ByteHexToDecimalInt(byte[] HexData) {
		StringBuffer sb = new StringBuffer(HexData.length * 2);
		String hexaDecimal;

		for (int x = 0; x < HexData.length; x++) {
			hexaDecimal = "0" + Integer.toHexString(0xff & HexData[x]);
			sb.append(hexaDecimal.substring(hexaDecimal.length() - 2));
		}
		hexaDecimal = null;
		return Integer.parseInt(sb.toString(), 16);
	}

	// Long to Byte[]
	public byte[] LongToByteArray(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(x);
		return buffer.array();
	}
	
	public byte[] LongListToByteArray(List<Long> arrayList) {
		byte[] bytes = new byte[arrayList.size() * 8];
		int pos = 0;
		for(int i = 0; i < arrayList.size(); i++) {
			System.arraycopy(LongToByteArray(arrayList.get(i)), 0, bytes, pos, 8); 
			pos += 8;
		}
		return bytes;
	}

	// Byte[] to Long
//	public long ByteArrayToLong(byte[] bytes) {
//	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
//	    buffer.put(bytes);
//	    buffer.flip();//need flip 
//	    return buffer.getLong();
//	}

	public byte[] BytesArrayLE3(byte[] bytes) {
		int i, j;

		for (i = 0, j = bytes.length - 1; i < j; i++, j--) {
			byte b = bytes[i];
			bytes[i] = bytes[j];
			bytes[j] = b;
		}

		return bytes;
	}

	public byte[] BytesArrayLE2(byte[] bytes) {
		int i, j;
		byte[] RevBytes = new byte[bytes.length];

		for (i = 0, j = bytes.length - 1; i < j; i++, j--) {
			byte b = bytes[i];
			RevBytes[i] = bytes[j];
			RevBytes[j] = b;
		}

		return RevBytes;
	}

	public byte[] BytesArrayLE(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		buffer.put(bytes);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		return buffer.array();
	}

	public byte[] IntToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}

	public byte[] shortToByteArray(short value) {
	    return new byte[] {
	            (byte)(value >>> 8),
	            (byte)value};
	}

	public void ByteArrayToPrint(byte[] bytes, int notation) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "BA2STR : " + this.ByteHexToString(bytes));
	}

	public int StringToInt(String str) {
		return (Integer.parseInt(str));
	}

	public String IntToString(int value) {
		return (Integer.toString(value));
	}

	public long StringToLong(String str) {
		return (Long.parseLong(str));
	}

	public String LongToString(int value) {
		return (Long.toString(value));
	}

	public boolean ByteArrayCompare(byte[] bytes1, byte[] bytes2) {
		return (java.util.Arrays.equals(bytes1, bytes2));
	}

	public int next_idx(int a, int b) {
		return ((a + 1) % b);
	}

	public byte[] xor(byte[] a, byte[] key) {
		byte[] out = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			out[i] = (byte) (a[i] ^ key[i % key.length]);
		}
		return out;
	}

	public void memset_ByteArray(byte[] array, byte j) {
		for (int i = 0, len = array.length; i < len; i++)
			array[i] = j;
	}
	
	public void memset_ByteArray(byte[] array, byte j, int memLen) {
		for (int i = 0, len = memLen; i < len; i++)
			array[i] = j;
	}

	public void memsetIntArray(int[] array, int val) {
		for (int i = 0, len = array.length; i < len; i++)
			array[i] = val;
	}
	
	public void memsetIntArray(int[] array, int val, int memLen) {
		for (int i = 0, len = memLen; i < len; i++)
			array[i] = val;
	}

	public BigInteger byteArrayToBigInteger(byte[] b) {
		return new BigInteger(b);
	}

}
