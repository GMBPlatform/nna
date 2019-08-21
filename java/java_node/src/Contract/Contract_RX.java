package Contract;

import Main.Global;
import Consensus.Consensus_TX;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

///////////////////////////////////////////////////////////////
// Currently Contract_RX recv from SCA only Tx data          //
// Parsing Json Format and Send Tx to CN                     //
// Data Format between SCA and NNA is JSON Format            //
///////////////////////////////////////////////////////////////

public class Contract_RX {
	private Global global;
	private Consensus_TX CN_writer;
	
	public Contract_RX(Global global) {
		this.global = global;
		this.CN_writer = new Consensus_TX(global);
	}
	
	public void ReadData(ByteBuf msg, ChannelHandlerContext ctx) {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Received Transaction From SCA");
		String msg_str = this.global.getCTypeCast().ByteArrayToUTF8String(this.global.getCTypeCast().ByteBufToByteArr(msg));
		
		// Parsing json
		// SCA Send 2 data (db_key, hash)
		JsonParser parser = new JsonParser();
		Object json = parser.parse(msg_str);
		JsonObject contract_json = (JsonObject)json;
		
		String block_num = Long.toUnsignedString(this.global.getCTypeCast().ByteArrayToLong(this.global.getCConsensus().getBN()));
		String DB_Key = contract_json.get("db_key").getAsString();
		String Hash = contract_json.get("hash").getAsString();
		
		// Start Insert DB Routine;
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Insert Transaction Data to Database");
		if(!this.global.getCDBHelper().InsertTransactionToDB(block_num, DB_Key, Hash)) {
			// TODO Insert Fail Routine
		}
		// End Insert DB Routine;
		
		// Start Send Transaction to CN
		byte[] db_key = this.global.getCTypeCast().LongToByteArray(Long.parseLong(DB_Key));
		byte[] hash = this.global.getCTypeCast().HexStrToByteArray(Hash);
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send Transacation Data to current CN");
		
		parser = null;
		json = null;
		contract_json = null;
		
		msg_str = null;
		block_num = null;
		DB_Key = null;
		Hash = null;
		
		this.CN_writer.Tx(db_key, hash);
		
		db_key = null;
		hash = null;
	}
}
