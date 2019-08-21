package Main;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import Consensus.Consensus_TX;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

//////////////////////////////////////////////////////////////
// ISA class is for suspend connection to ISA               //
// And receive command from ISA                             //
// They communicate using TCP Protocol                      //
// Update RR List and Start Block Gen Req only receive NN   //
//////////////////////////////////////////////////////////////

@SuppressWarnings("unchecked")
enum  ISA_Cmd{
	Update_RR_List(0), Start_Blk_Gen(33);

	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private ISA_Cmd(int value) {
        this.value = value;
    }

    static {
        for (ISA_Cmd isaCmd : ISA_Cmd.values()) {
            map.put(isaCmd.value, isaCmd);
        }
    }

    public static ISA_Cmd valueOf(int isaCmd) {
        return (ISA_Cmd) map.get(isaCmd);
    }

    public int getValue() {
        return value;
    }
}

public class ISA {
	private Global global;
	// Store ISA Socket CTX data
	private InetSocketAddress ISA_SockAddr;
	private int ISAport;
	private Consensus_TX consensus_tx;
	
	public ISA(Global global) {
		this.global = global;
		this.consensus_tx = new Consensus_TX(global);
	}
	
	public void setISAport(int ISAport) {
		this.ISAport = ISAport;
	}
	
	public int getISAport() {
		return this.ISAport;
	}
	
	public void setISASocketAddress(String ip, int port) {
		this.ISA_SockAddr = new InetSocketAddress(ip, port);
	}
	
	public InetSocketAddress getISASocketAddress() {
		return this.ISA_SockAddr;
	}
	
	public void ReadData(ByteBuf msg, ChannelHandlerContext ctx) {
		byte cmd[] = this.global.getCTypeCast().ByteBufToByteArr(msg);
		int cmd_I = (byte)cmd[0];
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Received from ISA Cmd : " + this.global.getCTypeCast().IntToString(cmd_I));
		
		if(cmd_I == ISA_Cmd.Update_RR_List.getValue()) {
			// Update RR List Routine
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Received Udpate RR List Req from ISA");
			this.global.ParseRRNetJson();
			this.global.ParseRRSubNetJson();
			
			this.global.getCConsensus().rrNetInit();
			this.global.getCConsensus().rrSubNetInit();
			
			// Send Update RR List Resp To ISA
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send Update RR List Resp to ISA");
			WriteDatas(ctx, cmd, null);
		} else if(cmd_I == ISA_Cmd.Start_Blk_Gen.getValue()) {
			// Start Blk Gen Routine
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Received StartBlockGen Cmd from ISA");
			this.consensus_tx.BlkGenInfo();
			
			// Send StartBlockGen Resp To ISA
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "Send StartBlockGen Resp to ISA");
			WriteDatas(ctx, cmd, null);
		}
		
		cmd = null;
		
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
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "ISA WriteData Error cause " + e.getCause());
			cf = null;
			return false;
		}
		cf = null;
		return true;
	}
}
