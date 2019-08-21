package Util;

import java.util.zip.Checksum;

import Main.Global;

// Calculate CRC32 value 

public class CRC32 {
	private Global global;
	
	public CRC32(Global global) {
		this.global = global;
	}
	
	public byte[] CalcCRC32(byte[] data) {
		Checksum crc = (Checksum) new java.util.zip.CRC32();
		crc.update(data, 0, data.length);
		long calculated = crc.getValue();
		crc.reset();
		crc = null;
		
		return this.global.getCTypeCast().IntToByteArray((int)(long)calculated);
	}
}
