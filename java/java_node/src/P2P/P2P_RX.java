package P2P;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import Main.Global;

import P2P.P2P_TX;

import Consensus.Consensus_RX;

/////////////////////////////////////////////////////////////////////////////////////
// P2P has three types of cmd (P2PJoinReq, P2PJoinRsp, P2PPublicKeyNoti)           //
// P2P protocol the type of cmd is parsing P2PHeader and Performing each Method    //
// P2P protocol the type of data is pass to Consensus                              //
/////////////////////////////////////////////////////////////////////////////////////

public class P2P_RX {
	private Global global;
	private P2P_TX p2p_tx;
	private Consensus_RX reader;
	
	private byte[] m_Msg;
	private byte[] m_MsgTmp;
	private byte[] m_DstAddr;
	private byte[] m_SrcAddr;
	private byte[] m_TimeStamp;
	private byte[] m_Version;
	private byte[] m_Type;
	private byte[] m_Cmd;
	private byte[] m_Rsvd;
	private byte[] m_SeqNum;
	private byte[] m_Len;
	private byte[] m_CRC32;

	public P2P_RX(Global global) {
		this.global = global;
		this.p2p_tx = new P2P_TX(this.global);
		this.reader = new Consensus_RX(global);

		this.m_DstAddr = new byte[P2P_Header_field.DstAddr.getValue()];
		this.m_SrcAddr = new byte[P2P_Header_field.SrcAddr.getValue()];
		this.m_TimeStamp = new byte[P2P_Header_field.TimeStamp.getValue()];
		this.m_Version = new byte[P2P_Header_field.Version.getValue()];
		this.m_Type = new byte[P2P_Header_field.Type.getValue()];
		this.m_Cmd = new byte[P2P_Header_field.Cmd.getValue()];
		this.m_Rsvd = new byte[P2P_Header_field.Rsvd.getValue()];
		this.m_SeqNum = new byte[P2P_Header_field.SeqNum.getValue()];
		this.m_Len = new byte[P2P_Header_field.Len.getValue()];
		this.m_CRC32 = new byte[P2P_Define.CRC32Len.getValue()];
	}

	public void ReadData(ByteBuf msg, ChannelHandlerContext ctx) {
		int pos;
		boolean chked_msg = false;

		do
		{
			if (this.m_MsgTmp == null)
			{
				this.m_Msg = this.global.getCTypeCast().ByteBufToByteArr(msg);
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "m_MsgTmp = null");
			}
			else
			{
				int msgLen = 0;

				if (chked_msg == false)
				{
					msgLen = msg.readableBytes();
				}
				
				this.m_Msg = new byte[this.m_MsgTmp.length + msgLen];
				
				System.arraycopy(this.m_MsgTmp, 0, this.m_Msg, 0, this.m_MsgTmp.length);
				System.arraycopy(this.global.getCTypeCast().ByteBufToByteArr(msg), 0, this.m_Msg, this.m_MsgTmp.length, msgLen);
				
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "m_MsgTmp is NOT null m_MsgTmp Len " + this.m_MsgTmp.length + " msgLen " + msgLen);
			
				this.m_MsgTmp = null;
			}

			chked_msg = true;
			
			int p2pHdrLen = P2P_Header_field.getTotalValue() + P2P_Header_field.Len.getValue();
			
			if (this.m_Msg.length < p2pHdrLen)
			{
				this.m_MsgTmp = this.m_Msg;
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Small than 1 P2P Header Length " + p2pHdrLen + " m_Msg Len " + this.m_Msg.length);
			
				return;
			}
		
			pos = ParseP2PHeader();
			
			if(pos > 0) {
				if(this.m_Type[0] == P2P_Type.CMD.ordinal()) {
					// Type == Cmd
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "P2P Type == Cmd");

					if(this.m_Cmd[0] == P2P_Cmd.P2P_JOIN_REQ.ordinal()) {
						// P2P Join Request
						if(!ParseP2PJoinReq(ctx)) 
							this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "P2P Join Request Parsing error");
					} else if(this.m_Cmd[0] == P2P_Cmd.P2P_JOIN_RESP.ordinal()) {
						// P2P Join Response
						if(!ParseP2PJoinRsp(ctx)) 
							this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "P2P Join Response Parsing error");
					} else if(this.m_Cmd[0] == P2P_Cmd.P2P_PUBKEY_NOTI.ordinal()) {
						// P2P Public Key Notification
						if(!ParseP2PPubKeyNoti(ctx)) 
							this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Public Key Notification Parsing error");
					} else {
						this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "This cmd is not element of P2P CMD set");
					}
				}
				else if(this.m_Type[0] == P2P_Type.MGMT.ordinal()) {
					// Type == Mgmt
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "P2P Type == Mgmt");
				}
				else if(this.m_Type[0] == P2P_Type.DATA.ordinal()) {
					// Type == Data
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "P2P Type == Data");
					if(!PassToConesnsusRX(ctx, pos))
						this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "PassToConesnsusRX Parsing error");
				}
				else {
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "This type is not element of P2P TYPE set");
				}
			}
			else if (pos == 0) {
				return;
			}
			
			this.m_Msg = null;
		} while (this.m_MsgTmp != null);
	}

	synchronized public int ParseP2PHeader() {
		try {
			int pos = 0;
			
			//
			System.arraycopy(this.m_Msg, pos, this.m_DstAddr, 0, this.m_DstAddr.length); pos += this.m_DstAddr.length;
			//this.m_DstAddr = this.global.getCTypeCast().BytesArrayLE2(this.m_DstAddr);
			System.arraycopy(this.m_Msg, pos, this.m_SrcAddr, 0, this.m_SrcAddr.length); pos += this.m_SrcAddr.length;
			//this.m_SrcAddr = this.global.getCTypeCast().BytesArrayLE2(this.m_SrcAddr);
			System.arraycopy(this.m_Msg, pos, this.m_TimeStamp, 0, this.m_TimeStamp.length); pos += this.m_TimeStamp.length;
			System.arraycopy(this.m_Msg, pos, this.m_Version, 0, this.m_Version.length); pos += this.m_Version.length;
			System.arraycopy(this.m_Msg, pos, this.m_Type, 0, this.m_Type.length); pos += this.m_Type.length;
			System.arraycopy(this.m_Msg, pos, this.m_Cmd, 0, this.m_Cmd.length); pos += this.m_Cmd.length;
			System.arraycopy(this.m_Msg, pos, this.m_Rsvd, 0, this.m_Rsvd.length); pos += this.m_Rsvd.length;
			System.arraycopy(this.m_Msg, pos, this.m_SeqNum, 0, this.m_SeqNum.length); pos += this.m_SeqNum.length;
			System.arraycopy(this.m_Msg, pos, this.m_Len, 0, this.m_Len.length); pos += this.m_Len.length;

			int p2pHdrLen = P2P_Header_field.getTotalValue() + P2P_Header_field.Len.getValue();
			
			int totLen = (p2pHdrLen + this.global.getCTypeCast().ByteArrayToShort(this.m_Len) + P2P_Define.CRC32Len.getValue());
			if (this.m_Msg.length > totLen)
			{
				int msgLen = this.m_Msg.length - totLen;
				this.m_MsgTmp = new byte[msgLen];

				System.arraycopy(this.m_Msg, totLen, this.m_MsgTmp, 0, msgLen);

				byte[] tMsg = new byte[totLen];
				System.arraycopy(this.m_Msg, 0, tMsg, 0, totLen);
				this.m_Msg = new byte[totLen];
				System.arraycopy(tMsg, 0, this.m_Msg, 0, totLen);
				
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Large than 1 packet length totLen " + totLen + " m_Msg Len " + this.m_Msg.length);
				tMsg = null;
			}
			else if (this.m_Msg.length < totLen)
			{
				this.m_MsgTmp = new byte[this.m_Msg.length];
				System.arraycopy(this.m_Msg, 0, this.m_MsgTmp, 0, this.m_Msg.length);
				//this.m_MsgTmp = this.m_Msg;
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Small than 1 packet length totLen " + totLen + " m_Msg Len " + this.m_Msg.length);

				return 0;
			}
			
			// For Compare CRC32 Value 
			int HeaderAndContentsLen = totLen - P2P_Define.CRC32Len.getValue();
			byte[] HeaderAndContents = new byte[HeaderAndContentsLen];
			System.arraycopy(this.m_Msg, 0, HeaderAndContents, 0, HeaderAndContentsLen);
			
if (P2P_Define.CheckCRC.getValue() != 0)
{
			//
			System.arraycopy(this.m_Msg, pos + this.global.getCTypeCast().ByteHexToDecimalInt(this.m_Len), this.m_CRC32, 0, this.m_CRC32.length);
			byte[] CalcCRC32 = new byte[P2P_Define.CRC32Len.getValue()];
			CalcCRC32 = this.global.getCCRC32().CalcCRC32(HeaderAndContents);
			
			// Compare CRC32 
			if(this.global.getCTypeCast().ByteHexToString(CalcCRC32).compareTo(this.global.getCTypeCast().ByteHexToString(this.m_CRC32)) != 0)
			{
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "The Two CRC32 Value is Different");
				//assert(false);
			}
			else 
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "The Two CRC32 Value is Same");
			
			CalcCRC32 = null;
}
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "m_Type " + this.m_Type[0] + " m_Cmd " + this.m_Cmd[0]);

			HeaderAndContents = null;

			return pos; // P2P header

		} 
		catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in Parse P2P Header cause " + e.getCause());
			return 0;
		}
	}

	synchronized public boolean ParseP2PJoinReq(ChannelHandlerContext ctx) {
		if(this.global.getCTypeCast().ByteHexToDecimalInt(this.m_Len) != P2P_Cmd_Join_Req_field.getTotalValue()) return false;
		try {
			//
			byte[] JoiningP2PAddr = new byte[P2P_Cmd_Join_Req_field.JoinP2PAddr.getValue()];
			byte[] NodeRule = new byte[P2P_Cmd_Join_Req_field.NodeRule.getValue()];

			//
			int pos = P2P_Header_field.getTotalValue() + P2P_Header_field.Len.getValue();

			String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Name of current method: " + nameofCurrMethod);
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, 
					this.m_Msg.length + " " + pos + " "  + JoiningP2PAddr.length + " "  + NodeRule.length);
						
			System.arraycopy(this.m_Msg, pos, JoiningP2PAddr, 0, JoiningP2PAddr.length); pos += JoiningP2PAddr.length;
			System.arraycopy(this.m_Msg, pos, NodeRule, 0, NodeRule.length); pos += NodeRule.length;

			this.global.getCP2P().P2PJoinReqPrint(JoiningP2PAddr, NodeRule);

			//
			// Set Peer Information
			this.global.getCP2P().setMyPeerP2P(JoiningP2PAddr, NodeRule[0], ctx);

			//
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send P2PJoinRsp to " + this.global.getCTypeCast().ByteHexToString(JoiningP2PAddr));
			p2p_tx.P2PJoinRsp(ctx, JoiningP2PAddr, JoiningP2PAddr, NodeRule);
			
			JoiningP2PAddr = null;
			NodeRule = null;
			nameofCurrMethod = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in Parse P2PJoinReq cause " + e.getCause());
			return false;
		}
	}

	synchronized public boolean ParseP2PJoinRsp(ChannelHandlerContext ctx) {
		if(this.global.getCTypeCast().ByteHexToDecimalInt(this.m_Len) != P2P_Cmd_Join_Rsp_field.getTotalValue()) return false;
		try {
			//
			byte[] JoiningP2PAddr = new byte[P2P_Cmd_Join_Rsp_field.JoinP2PAddr.getValue()];
			byte[] NodeRule = new byte[P2P_Cmd_Join_Rsp_field.NodeRule.getValue()];

			//
			int pos = P2P_Header_field.getTotalValue() + P2P_Header_field.Len.getValue();

			String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Name of current method: " + nameofCurrMethod);
			
			System.arraycopy(this.m_Msg, pos, JoiningP2PAddr, 0, JoiningP2PAddr.length); pos += JoiningP2PAddr.length;
			System.arraycopy(this.m_Msg, pos, NodeRule, 0, NodeRule.length); pos += NodeRule.length;

			this.global.getCP2P().P2PJoinRspPrint(JoiningP2PAddr, NodeRule);

			//
			// Set Peer Information
			this.global.getCP2P().setMyPeerP2P(this.m_SrcAddr, NodeRule[0], ctx);
			
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send P2PJoinRsp to " + this.global.getCTypeCast().ByteHexToString(this.m_SrcAddr));
			p2p_tx.P2PPubkeyNoti(ctx, this.m_SrcAddr);
			
			JoiningP2PAddr = null;
			NodeRule = null;
			nameofCurrMethod = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in ParseP2PJoinRsp cause " + e.getCause());
			return false;
		}
	}

	synchronized public boolean ParseP2PPubKeyNoti(ChannelHandlerContext ctx) {
		if(this.global.getCTypeCast().ByteHexToDecimalInt(this.m_Len) != P2P_Cmd_Pubkey_Noti_field.getTotalValue()) return false;
		try {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Parse P2PPubKeyNoti");
			//
			byte[] CompPubkey = new byte[P2P_Cmd_Pubkey_Noti_field.CompPubkey.getValue()];

			//
			int pos = P2P_Header_field.getTotalValue() + P2P_Header_field.Len.getValue();

			String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName(); 
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Name of current method: " + nameofCurrMethod);
		
			System.arraycopy(this.m_Msg, pos, CompPubkey, 0, CompPubkey.length); pos += CompPubkey.length;

			this.global.getCP2P().P2PPubkeyNotiPrint(CompPubkey);
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Source P2P Address : " + this.global.getCTypeCast().ByteHexToString(this.m_SrcAddr));

			//
			// Set Peer Public Key
			if (P2P_Define.MakePubkeyPath.getValue() != 0)
			{
				// Set PeerPublicKeyPath
				byte[] peerSubNetAddress = new byte[P2P_Cmd_Pubkey_Noti_field.PeerSubNetAddr.getValue()];
				System.arraycopy(this.m_SrcAddr, 4, peerSubNetAddress, 0, peerSubNetAddress.length);
				String peerKeyPath = "./key/" + this.global.getCTypeCast().ByteHexToString(peerSubNetAddress) + "/pubkey.pem";
				
				// Save Peer Public Key
				// Make Directory
				if(!this.global.getCFileIO().mkPeerKeydir(this.global.getCTypeCast().ByteHexToString(peerSubNetAddress))) {
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "Dir Aleardy exist or Exception occurred");
				}
				
				// Make Empty File
				if(!this.global.getCFileIO().mkPeerKeyfile(this.global.getCTypeCast().ByteHexToString(peerSubNetAddress), "pubkey.pem")) {
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "File Already exist or Exception occurred");
				}
				
				if(!this.global.getCKeyUtil().WritePeerPublicKeyPemFile(CompPubkey, peerKeyPath)) {
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "Fail to Write peer pubkey.pem");
				} else {
					this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_WARNING, "Success to Write peer pubkey.pem");
					this.global.getCP2P().getPeerPublicKeyPathMap().put(this.m_SrcAddr, peerKeyPath);
					this.global.getCP2P().setMyPeerPublicKeyPath(this.m_SrcAddr, peerKeyPath);	
				}
				peerSubNetAddress = null;
				peerKeyPath = null;
			}			
			byte nodeRule[] = this.global.getCP2P().getMyNodeRule();
			if (nodeRule[0] == (byte) P2P_Node_Rule.NN.ordinal())
			{
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "PubkeyNoti to " + this.global.getCTypeCast().ByteHexToString(this.m_SrcAddr));
				this.p2p_tx.P2PPubkeyNoti(ctx, this.m_SrcAddr);
			}
			else // CN or SCN
			{
				// 
			}
			CompPubkey = null;
			nodeRule = null;
			nameofCurrMethod = null;
			
			return true;
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in ParseP2PPubKeyNoti cause " + e.getCause());
			return false;
		}
	}

	synchronized public boolean PassToConesnsusRX(ChannelHandlerContext ctx, int pos) {
		int length = this.global.getCTypeCast().ByteHexToDecimalInt(this.m_Len);
		byte[] Contents = new byte[length];

		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "PassToConesnsusRX length " + length + " pos" + pos);
		System.arraycopy(this.m_Msg, pos, Contents, 0, length);
		reader.PassFromP2PRX(Contents, ctx);
		Contents = null;
		return true;
	}
}
