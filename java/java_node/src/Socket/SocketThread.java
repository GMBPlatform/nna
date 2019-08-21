package Socket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.SystemUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.stream.ChunkedWriteHandler;

import Main.Global;
import Socket.SockInfo;

//import Socket.Sock_Type;

// Each Socket open
public class SocketThread {
	private Global global;
	private ArrayList<SockInfo> udp_server_infos;
	private ArrayList<SockInfo> udp_client_infos;
	private ArrayList<SockInfo> tcp_server_infos;
	private ArrayList<SockInfo> tcp_client_infos;
	
//	private Bootstrap udpClientBootstrap;
	private Bootstrap tcpClientBootstrap;
//	private ServerBootstrap udpServerBootstrap;
	private ServerBootstrap tcpServerBootstrap;
	
	private EventLoopGroup tcpGroup;
	private EventLoopGroup tcpBossGroup;
	private EventLoopGroup tcpWorkerGroup;

	private Collection<Channel> tcpServerChannels;
	private Collection<ChannelFuture> tcpClientChannelFutures;
	
	public void Sock_Thread(Global global, ArrayList<SockInfo> sock_param_infos) {
		this.global = global;
		this.tcp_server_infos = new ArrayList<SockInfo>();
		this.tcp_client_infos = new ArrayList<SockInfo>();

		for(int i = 0; i < sock_param_infos.size(); i++) {
			if(sock_param_infos.get(i).getSockType() == Sock_Type.UDPServer.getValue()) {
//				this.udp_server_infos.add(sock_param_infos.get(i));
			} 
			else if(sock_param_infos.get(i).getSockType() == Sock_Type.UDPClient.getValue()) {
//				this.udp_client_infos.add(sock_param_infos.get(i));
			} 
			else if(sock_param_infos.get(i).getSockType() == Sock_Type.TCPServer.getValue()) {
				this.tcp_server_infos.add(sock_param_infos.get(i));
			} 
			else if(sock_param_infos.get(i).getSockType() == Sock_Type.TCPClient.getValue()) {
				this.tcp_client_infos.add(sock_param_infos.get(i));
			}
		}
		
		setTCPServerBootstrap(this.global);
		setTCPClientBootstrap(this.global);
		AllSocketOpen();
	}
	
	public void setUDPServerBootstrap() {
		// TODO UDP Server
	}
	
	public void setUDPClientBootstrap() {
		// TODO UDP Client
	}
	
	public void setTCPServerBootstrap(Global global) {
		
		if(SystemUtils.IS_OS_LINUX == true) {
			this.tcpBossGroup = new EpollEventLoopGroup(1);
			this.tcpWorkerGroup = new EpollEventLoopGroup(3);
		} else {
			this.tcpBossGroup = new NioEventLoopGroup(1);
			this.tcpWorkerGroup = new NioEventLoopGroup(3);
		}
		
		try {
			this.tcpServerBootstrap = new ServerBootstrap();
			
			this.tcpServerBootstrap.option(ChannelOption.SO_REUSEADDR, true);
			this.tcpServerBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
			this.tcpServerBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			this.tcpServerBootstrap.childOption(ChannelOption.SO_LINGER, 0);
			this.tcpServerBootstrap.childOption(ChannelOption.SO_RCVBUF, 60000);
			this.tcpServerBootstrap.childOption(ChannelOption.SO_SNDBUF, 60000);
			
			this.tcpServerBootstrap.group(this.tcpBossGroup, this.tcpWorkerGroup);
			if(SystemUtils.IS_OS_LINUX == true) {
				this.tcpServerBootstrap.channel(EpollServerSocketChannel.class);
			} else {
				this.tcpServerBootstrap.channel(NioServerSocketChannel.class);
			}
			
			this.tcpServerBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) {
					ch.pipeline().addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
					ch.pipeline().addLast(new NettyTcpServerHandler(global));
				}
			});
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "TCP Server Bootstrap init Error");
		}
	}
	
	public void setTCPClientBootstrap(Global global) {
		
		if(SystemUtils.IS_OS_LINUX == true) {
			this.tcpGroup = new EpollEventLoopGroup(1);
		} else {
			this.tcpGroup = new NioEventLoopGroup(1);
		}
		
		try {
			this.tcpClientBootstrap = new Bootstrap();
			
			this.tcpClientBootstrap.option(ChannelOption.SO_REUSEADDR, true);
			this.tcpClientBootstrap.option(ChannelOption.TCP_NODELAY, true);
			this.tcpClientBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			this.tcpClientBootstrap.option(ChannelOption.SO_LINGER, 0);
			this.tcpClientBootstrap.option(ChannelOption.SO_RCVBUF, 60000);
			this.tcpClientBootstrap.option(ChannelOption.SO_SNDBUF, 60000);
			
			this.tcpClientBootstrap.group(this.tcpGroup);
			if(SystemUtils.IS_OS_LINUX == true) {
				this.tcpClientBootstrap.channel(EpollSocketChannel.class);
			} else {
				this.tcpClientBootstrap.channel(NioSocketChannel.class);
			}
			
			this.tcpClientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) {
					ch.pipeline().addLast("ChunkedWriteHandler", new ChunkedWriteHandler());
					ch.pipeline().addLast(new NettyTcpClientHandler(global));
				}
			});
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "TCP Client Bootstrap init Error");
		}
	}
	
	public void AllSocketOpen() {
		this.tcpServerChannels = new ArrayList<>(this.tcp_server_infos.size());
		Channel tcpServerChannel;
		
		this.tcpClientChannelFutures = new ArrayList<>(this.tcp_client_infos.size());
		ChannelFuture tcpClientFuture;
		
		try {
			// Bind each TCP Server Socket
			for(int idx = 0; idx < this.tcp_server_infos.size(); idx++) {
				tcpServerChannel = this.tcpServerBootstrap.bind(this.tcp_server_infos.get(idx).getIp(), this.tcp_server_infos.get(idx).getPort()).sync().channel();
				tcpServerChannels.add(tcpServerChannel);
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, 
						"TCP Server Bind [ " + this.tcp_server_infos.get(idx).getIp() + " , " + this.tcp_server_infos.get(idx).getPort() + "]");
			}
			
			// Connect TCP Client Socket
			for(int idx = 0; idx < this.tcp_client_infos.size(); idx++) {
				tcpClientFuture = this.tcpClientBootstrap.connect(new InetSocketAddress(this.tcp_client_infos.get(idx).getPeerIp(), this.tcp_client_infos.get(idx).getPeerPort()),
						new InetSocketAddress(this.tcp_client_infos.get(idx).getLocalIp(), this.tcp_client_infos.get(idx).getLocalPort()));
				tcpClientChannelFutures.add(tcpClientFuture);
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_INFO, 
						"TCP Client Connect to [" + this.tcp_client_infos.get(idx).getPeerIp() + " , " + this.tcp_client_infos.get(idx).getPeerPort() + "]");
			}
			
			// Waiting For Connection Close (Similarly Waiting Thread join)
			for(Channel ch : this.tcpServerChannels) {
				ch.closeFuture().sync();
			}
			
			for(ChannelFuture cf : this.tcpClientChannelFutures) {
				cf.channel().closeFuture().sync();
			}
		} catch (Exception e) {
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "AllSocket Open Error");
		} finally {
			try {
				// Release Each Bootstrap Variable
				this.tcpBossGroup.shutdownGracefully().sync();
				this.tcpWorkerGroup.shutdownGracefully().sync();
				this.tcpGroup.shutdownGracefully().sync();
			} catch (InterruptedException e) {
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_ERROR, "Netty Group shutdown Error");
			}
		}
	}
	
}
