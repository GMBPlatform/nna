package Util;

import java.sql.Timestamp;

import Main.Global;

public class TimeStamp {
	private Global global;
	
	public TimeStamp(Global global) {
		this.global = global;
	}
	
	// CurrentTimestamp
	public long getCurrentTimeStamp() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long ret = (timestamp.getTime());
		timestamp = null;
		return ret;
	}
	
	public long getCurrentTimeStampL() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long ret = timestamp.getTime();
		timestamp = null;
		return ret;
	}

	// CurrentTimeStamp ByteArray Form
	public byte[] getCurrentTimeStampBA() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		byte[] ret = (this.global.getCTypeCast().LongToByteArray(timestamp.getTime()));
		timestamp = null;
		return ret;
	}
}
