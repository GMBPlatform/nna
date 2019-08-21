package P2P;

import java.util.concurrent.ScheduledExecutorService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import Main.Global;

/////////////////////////////////////////////////////////////////////////////////////
// P2P has three types of cmd (P2PJoinReq, P2PJoinRsp, P2PPublicKeyNoti)           //
// P2P protocol the type of data is pass from Consensus                            //
/////////////////////////////////////////////////////////////////////////////////////

public class P2P_TX {
	private Global global;

	public P2P_TX(Global global) {
		this.global = global;
	}

	public boolean WriteDatas(ChannelHandlerContext ctx, byte[] msg, ScheduledExecutorService tm) {
		ByteBuf tx_msg = Unpooled.copiedBuffer(msg);
		ChannelFuture cf = ctx.writeAndFlush(tx_msg);
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
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in P2P WriteData cause " + e.getCause());
			tx_msg = null;
			cf = null;
			return false;
		}
		tx_msg = null;
		cf = null;
		return true;
	}

	public void P2PJoinReq(ChannelHandlerContext ctx, byte[] dstAddr) {
		// P2P Header
		byte[] P2PHeader = MakeP2PHeader(dstAddr, this.global.getCP2P().getMyP2PAddr(), (byte)P2P_Type.CMD.ordinal(), (byte)P2P_Cmd.P2P_JOIN_REQ.ordinal());
		byte[] Len = new byte[P2P_Header_field.Len.getValue()];

		// Join Request Header
		byte[] JoiningP2PAddr = new byte[P2P_Cmd_Join_Req_field.JoinP2PAddr.ordinal()];
		byte[] NodeRule = new byte[P2P_Cmd_Join_Req_field.NodeRule.ordinal()];
		byte[] CRC32 = new byte[P2P_Define.CRC32Len.getValue()];

		//
		int msgLen = P2P_Header_field.getTotalValue() + Len.length + P2P_Cmd_Join_Req_field.getTotalValue() + CRC32.length;
		byte[] before_crc32 = new byte[msgLen - CRC32.length];
		byte[] m_Msg = new byte[msgLen];
		
		//
		int pos = 0;

		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Name of current method : " + nameofCurrMethod);

		Len = this.global.getCTypeCast().shortToByteArray((short)P2P_Cmd_Join_Req_field.getTotalValue());

		//
		JoiningP2PAddr = this.global.getCP2P().getMyP2PAddr();
		NodeRule = this.global.getCP2P().getMyNodeRule();

		//
		System.arraycopy(P2PHeader, 0, before_crc32, pos, P2PHeader.length); pos += P2PHeader.length;
		System.arraycopy(Len, 0, before_crc32, pos, Len.length); pos += Len.length;
		System.arraycopy(JoiningP2PAddr, 0, before_crc32, pos, JoiningP2PAddr.length); pos += JoiningP2PAddr.length;
		System.arraycopy(NodeRule, 0, before_crc32, pos, NodeRule.length); pos += NodeRule.length;
		
		//
		CRC32 = this.global.getCCRC32().CalcCRC32(before_crc32);
		System.arraycopy(before_crc32, 0, m_Msg, 0, msgLen - CRC32.length);
		System.arraycopy(CRC32, 0, m_Msg, pos, CRC32.length); pos += CRC32.length;

		P2PHeader = null;
		Len = null;
		JoiningP2PAddr = null;
		NodeRule = null;
		CRC32 = null;
		before_crc32 = null;
		nameofCurrMethod = null;
		
		WriteDatas(ctx, m_Msg, null);
		m_Msg = null;
	}

	public void P2PJoinRsp(ChannelHandlerContext ctx, byte[] dstAddr, byte[] JoiningP2PAddr, byte[] NodeRule) {
		// P2P Header
		byte[] P2PHeader = MakeP2PHeader(dstAddr, this.global.getCP2P().getMyP2PAddr(), (byte)P2P_Type.CMD.ordinal(), (byte)P2P_Cmd.P2P_JOIN_RESP.ordinal());
		byte[] Len = new byte[P2P_Header_field.Len.getValue()];

		// Joining Response Header
		byte[] CRC32 = new byte[P2P_Define.CRC32Len.getValue()];

		//
		int msgLen = P2P_Header_field.getTotalValue() + Len.length + P2P_Cmd_Join_Rsp_field.getTotalValue() + CRC32.length;
		byte[] before_crc32 = new byte[msgLen - CRC32.length];
		byte[] m_Msg = new byte[msgLen];
		
		//
		int pos = 0;

		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Name of current method : " + nameofCurrMethod);

		Len = this.global.getCTypeCast().shortToByteArray((short)P2P_Cmd_Join_Rsp_field.getTotalValue());

		//
		System.arraycopy(P2PHeader, 0, before_crc32, pos, P2PHeader.length); pos += P2PHeader.length;
		System.arraycopy(Len, 0, before_crc32, pos, Len.length); pos += Len.length;
		System.arraycopy(JoiningP2PAddr, 0, before_crc32, pos, JoiningP2PAddr.length); pos += JoiningP2PAddr.length;
		System.arraycopy(NodeRule, 0, before_crc32, pos, NodeRule.length); pos += NodeRule.length;

		//
		CRC32 = this.global.getCCRC32().CalcCRC32(before_crc32);
		System.arraycopy(before_crc32, 0, m_Msg, 0, msgLen - CRC32.length);
		System.arraycopy(CRC32, 0, m_Msg, pos, CRC32.length); pos += CRC32.length;

		P2PHeader = null;
		Len = null;
		CRC32 = null;
		before_crc32 = null;
		nameofCurrMethod = null;
		
		WriteDatas(ctx, m_Msg, null);
		m_Msg = null;		
	}

	public void P2PPubkeyNoti(ChannelHandlerContext ctx, byte[] dstAddr) {
		// P2P Header
		byte[] P2PHeader = MakeP2PHeader(dstAddr, this.global.getCP2P().getMyP2PAddr(), (byte)P2P_Type.CMD.ordinal(), (byte)P2P_Cmd.P2P_PUBKEY_NOTI.ordinal());
		byte[] Len = new byte[P2P_Header_field.Len.getValue()];

		// Public Key Notification
		byte[] CompPubKey = new byte[P2P_Cmd_Pubkey_Noti_field.CompPubkey.ordinal()];
		byte[] CRC32 = new byte[P2P_Define.CRC32Len.getValue()];

		//
		int msgLen = P2P_Header_field.getTotalValue() + Len.length + P2P_Cmd_Pubkey_Noti_field.getTotalValue() + CRC32.length;
		byte[] before_crc32 = new byte[msgLen - CRC32.length];
		byte[] m_Msg = new byte[msgLen];
		
		//
		int pos = 0;

		String nameofCurrMethod = new Throwable().getStackTrace()[0].getMethodName();
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Name of current method : " + nameofCurrMethod);

		Len = this.global.getCTypeCast().shortToByteArray((short)P2P_Cmd_Pubkey_Noti_field.getTotalValue());

		//
		CompPubKey = this.global.getCKeyUtil().getCMyKey().getPublicKeyStr();

		//
		System.arraycopy(P2PHeader, 0, before_crc32, pos, P2PHeader.length); pos += P2PHeader.length;
		System.arraycopy(Len, 0, before_crc32, pos, Len.length); pos += Len.length;
		System.arraycopy(CompPubKey, 0, before_crc32, pos, CompPubKey.length); pos += CompPubKey.length;

		//
		CRC32 = this.global.getCCRC32().CalcCRC32(before_crc32);
		System.arraycopy(before_crc32, 0, m_Msg, 0, msgLen - CRC32.length);
		System.arraycopy(CRC32, 0, m_Msg, pos, CRC32.length); pos += CRC32.length;

		P2PHeader = null;
		Len = null;
		CompPubKey = null;
		CRC32 = null;
		before_crc32 = null;
		nameofCurrMethod = null;
		
		WriteDatas(ctx, m_Msg, null);
		m_Msg = null;
	}

	public byte[] MakeP2PHeader(byte[] dstAddr, byte[] srcAddr, byte type, byte cmd) {
		// P2P Header
		byte[] DstAddr = new byte[P2P_Header_field.DstAddr.getValue()];
		byte[] SrcAddr = new byte[P2P_Header_field.SrcAddr.getValue()];
		byte[] TimeStamp = new byte[P2P_Header_field.TimeStamp.getValue()];
		byte[] Version = new byte[P2P_Header_field.Version.getValue()];
		byte[] Type = new byte[P2P_Header_field.Type.getValue()];
		byte[] Cmd = new byte[P2P_Header_field.Cmd.getValue()];
		byte[] Rsvd = new byte[P2P_Header_field.Rsvd.getValue()];
		byte[] SeqNum = new byte[P2P_Header_field.SeqNum.getValue()];
		
		//
		byte[] P2PHeader = new byte[P2P_Header_field.getTotalValue()];
		int pos = 0;

		DstAddr = dstAddr;
		//DstAddr = this.global.getCTypeCast().BytesArrayLE2(DstAddr);
		SrcAddr = srcAddr;
 		//SrcAddr = this.global.getCTypeCast().BytesArrayLE2(DstAddr);

 		TimeStamp = this.global.getCTimeStamp().getCurrentTimeStampBA();
		Version[0] = this.global.getCP2P().getMyVersion();
		Type[0] = type;
		Cmd[0] = cmd;
		Rsvd[0] = (byte)0x00;

		if (type == P2P_Type.CMD.ordinal())
		{
			SeqNum = this.global.getCTypeCast().shortToByteArray(this.global.getCP2P().getMyP2PCmdSN());
			this.global.getCP2P().incMyP2PCmdSN();
		}
		else if (type == P2P_Type.DATA.ordinal())
		{
			SeqNum = this.global.getCTypeCast().shortToByteArray(this.global.getCP2P().getMyP2PDataSN());
			this.global.getCP2P().incMyP2PDataSN();
		}
		else
		{
			SeqNum = this.global.getCTypeCast().shortToByteArray((short)0x00);
		}
		
		System.arraycopy(DstAddr, 0, P2PHeader, pos, DstAddr.length); pos += DstAddr.length;
		System.arraycopy(SrcAddr, 0, P2PHeader, pos, SrcAddr.length); pos += SrcAddr.length;
		System.arraycopy(TimeStamp, 0, P2PHeader, pos, TimeStamp.length); pos += TimeStamp.length;
		System.arraycopy(Version, 0, P2PHeader, pos, Version.length); pos += Version.length;
		System.arraycopy(Type, 0, P2PHeader, pos, 1); pos += 1;
		System.arraycopy(Cmd, 0, P2PHeader, pos, Type.length); pos += Type.length;
		System.arraycopy(Rsvd, 0, P2PHeader, pos, Rsvd.length); pos += Rsvd.length;
		System.arraycopy(SeqNum, 0, P2PHeader, pos, SeqNum.length); pos += SeqNum.length;

		DstAddr = null;
		SrcAddr = null;
		TimeStamp = null;
		Version = null;
		Type = null;
		Cmd = null;
		Rsvd = null;
		SeqNum = null;
		
		return P2PHeader;
	}

	public boolean PassFromConsensusTX(ChannelHandlerContext ctx, byte[] dstAddr, byte[] Contents) {
		byte[] P2PHeader = MakeP2PHeader(dstAddr, this.global.getCP2P().getMyP2PAddr(), (byte)P2P_Type.DATA.ordinal(), (byte)0x00);
		byte[] p2PLen = new byte[P2P_Header_field.Len.getValue()];
		byte[] CRC32 = new byte[P2P_Define.CRC32Len.getValue()];

		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "PassFromConsensusTX " + P2PHeader.length + " "  + p2PLen.length + " " + Contents.length);
		int totalLen = P2PHeader.length + p2PLen.length + Contents.length + CRC32.length;
		byte[] before_crc32 = new byte[totalLen - CRC32.length];
		byte[] m_Msg = new byte[totalLen];
		int pos = 0;

		p2PLen = this.global.getCTypeCast().shortToByteArray((short)Contents.length);

		System.arraycopy(P2PHeader, 0, before_crc32, pos, P2PHeader.length); pos += P2PHeader.length;
		System.arraycopy(p2PLen, 0, before_crc32, pos, p2PLen.length); pos += p2PLen.length;
		System.arraycopy(Contents, 0, before_crc32, pos, Contents.length); pos += Contents.length;

		// Calculate CRC32
		CRC32 = this.global.getCCRC32().CalcCRC32(before_crc32);
		System.arraycopy(before_crc32, 0, m_Msg, 0, totalLen - CRC32.length);
		System.arraycopy(CRC32, 0, m_Msg, pos, CRC32.length); pos += CRC32.length;


		P2PHeader = null;
		p2PLen = null;
		CRC32 = null;
		before_crc32 = null;

		return (WriteDatas(ctx, m_Msg, null));
	}

}
