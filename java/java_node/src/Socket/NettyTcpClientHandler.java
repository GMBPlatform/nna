package Socket;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import Main.Global;
import P2P.P2P_RX;
import P2P.P2P_TX;

public class NettyTcpClientHandler extends SimpleChannelInboundHandler<ByteBuf>{
	private Global global;
	private P2P_RX reader;
	private P2P_TX writer;
	
	public NettyTcpClientHandler(Global global) {
		this.global = global;
		this.reader = new P2P_RX(global);
		this.writer = new P2P_TX(global);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Write Buffer code
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, "TCP Client Registered " + ctx.channel().id());
		
		try {
			this.global.getSockCtxSemaphore().acquire();
			this.global.getCP2P().getSockCtxs().put((InetSocketAddress)ctx.channel().remoteAddress(), ctx);
			this.global.getSockCtxSemaphore().release();
		} catch (InterruptedException e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in TCP Server Channel Active cause " + e.getCause());
		}
		this.writer.P2PJoinReq(ctx, this.global.getCP2P().getMyClusterRoot());
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		try {
			Channel incoming = ctx.channel();
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, 
					"[C-Socket] Remote Addr : " + incoming.remoteAddress() + " ReadData : " + this.global.getCTypeCast().ByteBufToString((ByteBuf)msg));
			incoming = null;
			reader.ReadData((ByteBuf)msg, ctx);
		}
		finally {
			//((ByteBuf)msg).release();
			//ReferenceCountUtil.release(msg);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Error in TCPClientHandler cause " + cause.getCause());
		ctx.close();
	}
}
