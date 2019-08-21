package Contract;

import java.util.concurrent.ScheduledExecutorService;
import java.util.Map;
import java.util.HashMap;
import com.google.gson.JsonObject;

import Main.Global;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

///////////////////////////////////////////////////////////////
// Currently Contract_TX send to SCA three types of Message  //
// three type is TxAck, BlkNoti, BlkGeninfo                  //
// Data Format between SCA and NNA is JSON Format            //
///////////////////////////////////////////////////////////////

@SuppressWarnings("unchecked")
enum TX_Field_Length {
	TXLen(40), HashLen(32), DBKeyLen(8);
	
	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private TX_Field_Length(int value) {
		this.value = value;
	}
	
	static {
		for(TX_Field_Length txFLen : TX_Field_Length.values()) {
			map.put(txFLen.value, txFLen);
		}
	}
	
	public static TX_Field_Length valueOf(int dbFLen) {
		return (TX_Field_Length) map.get(dbFLen);
	}
	
	public int getValue() {
		return value;
	}
}

public class Contract_TX {
	private Global global;
	
	public Contract_TX(Global global) {
		this.global = global;		
	}
	
	public boolean WriteDatas(ChannelHandlerContext ctx, byte[] msg, ScheduledExecutorService tm) {
		ChannelFuture cf = ctx.writeAndFlush(Unpooled.copiedBuffer(msg));
		try {
			cf.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if(future.isSuccess()) {
						global.getCLogger().OutConsole(global.getCLogger().LOG_INFO, "Successfully Write Data");
					} else {
						global.getCLogger().OutConsole(global.getCLogger().LOG_INFO, "Fail to Write Data");
					}
				}
			});			
		} catch (Exception e) {
			e.printStackTrace();
			cf = null;
			return false;
		}
		cf = null;
		return true;
	}
	
	// TxAck is Ack to SCA Tx Data is spread
	public boolean TxAckToSCA(byte[] BN, byte TxInfoCnt, byte[] TxInfoList) {
		ChannelHandlerContext ctx;

		ctx = this.global.getCP2P().getSockCtxs().get(this.global.getCContract().getSCASocketAddress());
		
		// if don't know about SCA that mean SCA not connected
		if (ctx == null)
		{
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SCA's ctx is null");
			ctx = null;
			return false;
		}
		
		JsonObject TxAck_json = new JsonObject();
		byte[] DB_Key = new byte[TX_Field_Length.DBKeyLen.getValue()];
		byte[] Hash = new byte[TX_Field_Length.HashLen.getValue()];
		
		System.arraycopy(TxInfoList, 0, DB_Key, 0, DB_Key.length);
		System.arraycopy(TxInfoList, DB_Key.length, Hash, 0, Hash.length);
		
		TxAck_json.addProperty("table_name", "tx");
		TxAck_json.addProperty("block_num", Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(BN)));
		TxAck_json.addProperty("db_key", Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(DB_Key)));
		TxAck_json.addProperty("hash", this.global.getCTypeCast().ByteHexToString(Hash));
		
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send TxAck to SCA");
		boolean ret = (WriteDatas(ctx, TxAck_json.toString().getBytes(), null));
		ctx = null;
		TxAck_json = null;

		DB_Key = null;
		Hash = null;
		
		return ret;
	}
	
	// BlkNotiToSCA is Notification to SCA the new Block Generated
	public boolean BlkNotiToSCA(byte[] BN, byte[] blkGenTime, byte[] tx_count, byte[] Hash) {
		ChannelHandlerContext ctx;

		ctx = this.global.getCP2P().getSockCtxs().get(this.global.getCContract().getSCASocketAddress());

		// if don't know about SCA that mean SCA not connected
		if (ctx == null)
		{
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SCA's ctx is null");
			ctx = null;
			return false;
		}

		JsonObject blockNoti_json = new JsonObject();
		
		blockNoti_json.addProperty("table_name", "block_noti");
		blockNoti_json.addProperty("block_gen_time", Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(blkGenTime)));
		blockNoti_json.addProperty("block_num", Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(BN)));
		// tx_count is number of Tx the block has
		blockNoti_json.addProperty("tx_count", Integer.toString(this.global.getCTypeCast().ByteArrayToInt(tx_count)));
		blockNoti_json.addProperty("hash", this.global.getCTypeCast().ByteHexToString(Hash));
		
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send BlkNoti to SCA");
		boolean ret = (WriteDatas(ctx, blockNoti_json.toString().getBytes(), null));
		ctx = null;
		blockNoti_json = null;
		return ret;
	}
	
	// BlkGenInfoToSCA is Notification to SCA Block Generation Start so, created genesis block
	public boolean BlkGenInfoToSCA(byte[] BN) {
		ChannelHandlerContext ctx;
		
		ctx = this.global.getCP2P().getSockCtxs().get(this.global.getCContract().getSCASocketAddress());
		
		if (ctx == null) 
		{
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "SCA's ctx is null");
			ctx = null;
			return false;
		}
		
		JsonObject blkGeninfo_json = new JsonObject();
		
		blkGeninfo_json.addProperty("table_name", "genesis_block");
		blkGeninfo_json.addProperty("block_num", this.global.getCTypeCast().ByteHexToString(BN));
		
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send BlkGenInfo to SCA");
		boolean ret = (WriteDatas(ctx, blkGeninfo_json.toString().getBytes(), null));
		ctx = null;
		blkGeninfo_json = null;
		return ret;
	}
}
