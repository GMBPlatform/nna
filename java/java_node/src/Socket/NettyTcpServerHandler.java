package Socket;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import Main.Global;
import P2P.P2P_RX;
import Contract.Contract_RX;

@ChannelHandler.Sharable
public class NettyTcpServerHandler extends ChannelInboundHandlerAdapter {
	
	private Global global;
	private P2P_RX p2p_reader;
	private Contract_RX contract_reader;
	
	private final ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	public NettyTcpServerHandler(Global global) {
		this.global = global;
		this.contract_reader = new Contract_RX(global);
		this.p2p_reader = new P2P_RX(global);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// add new connected ctx info in global sock_ctxs Array
		try {
			this.global.getSockCtxSemaphore().acquire();
			InetSocketAddress remoteAddr = (InetSocketAddress)ctx.channel().remoteAddress();
			this.global.getCP2P().getSockCtxs().put(remoteAddr, ctx);	
			
			if(remoteAddr.getPort() == this.global.getCContract().getSCAport()) {
				// if remoteAddr == SCA then store SCA CTX info in Contract instance
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "SCA Connencted");
				this.global.getCContract().setSCASocketAddress(remoteAddr.getHostName(), remoteAddr.getPort());				
			}
			else if(remoteAddr.getPort() == this.global.getCISA().getISAport()) {
				// if remoteAddr == ISA then store ISA CTX info in ISA instance
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "ISA Connected");
				this.global.getCISA().setISASocketAddress(remoteAddr.getHostName(), remoteAddr.getPort());
			}
			remoteAddr = null;
		} catch (InterruptedException e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in TCPServerHandler cause " + e.getCause());
		}
		this.global.getSockCtxSemaphore().release();
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();

		for(Channel channel:channelGroup)
		{
			channel.write("[S-Socket]-" + incoming.remoteAddress() + " has joined!\n");
		}

		channelGroup.remove(incoming);
		incoming = null;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			Channel incoming = ctx.channel();
			
//(0)
{
			//
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, 
					"[S-Socket] Remote Addr : " + incoming.remoteAddress() + " ReadData : " + this.global.getCTypeCast().ByteBufToString((ByteBuf)msg));
			InetSocketAddress remoteAddr = (InetSocketAddress)incoming.remoteAddress();
			
			// if remoteAddr port == SCAPort then goto Contract_RX
			// else if remoteAddr port == ISAPort then goto ISA
			// else goto P2P_RX
			if(remoteAddr.getPort() == this.global.getCContract().getSCAport()) 
				this.contract_reader.ReadData((ByteBuf)msg, ctx);
			else if(remoteAddr.getPort() == this.global.getCISA().getISAport())
				this.global.getCISA().ReadData((ByteBuf)msg, ctx);
			else this.p2p_reader.ReadData((ByteBuf)msg, ctx);

			incoming = null;
			remoteAddr = null;
}	
		}
		finally {
			//((ByteBuf)msg).release();
			ReferenceCountUtil.release(msg);
		}
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "[S-Socket] channelReadComplete");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Channel incoming = ctx.channel();
		try {
			this.global.getSockCtxSemaphore().acquire();
			for(Channel channel:channelGroup)
			{
				channel.write("[S-Socket]-" + incoming.remoteAddress() + " has left!\n");
			}
			channelGroup.remove(incoming);
			this.global.getCP2P().getSockCtxs().remove(ctx.channel().remoteAddress());
			incoming = null;
			this.global.getSockCtxSemaphore().release();
		} catch (InterruptedException e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in TCPServerHandler channelInactive cause " + e.getCause());
		}
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in TCPServerHandler cause " + cause.getCause());
		ctx.close();
	}
}
