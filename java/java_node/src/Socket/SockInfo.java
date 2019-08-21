package Socket;

import java.util.HashMap;
import java.util.Map;

/////////////////////////////////////////////////////////////////////
// SockInfo Class is Entity class                                  //
// SockInfo class has socket info (e.g. peer_ip, local_ip...etc)   //
// SockInfo Instance Distinguish member variable that instance has //
/////////////////////////////////////////////////////////////////////

@SuppressWarnings("unchecked")
enum  Sock_Type{
	UDPServer(0), UDPClient(1), TCPServer(2), TCPClient(3), None(-1);

	private int value;
	@SuppressWarnings("rawtypes")
	private static Map map = new HashMap<>();
	
	private Sock_Type(int value) {
        this.value = value;
    }

    static {
        for (Sock_Type sockType : Sock_Type.values()) {
            map.put(sockType.value, sockType);
        }
    }

    public static Sock_Type valueOf(int sockType) {
        return (Sock_Type) map.get(sockType);
    }

    public int getValue() {
        return value;
    }
}

public class SockInfo {
	byte type; // 0 : udp_srv , 1 : udp_cli, 2 : tcp_srv, 3 : tcp_cli
	String mreq_ip;
	String ip;
	int port;
	
	int auto_join;
	int p2p_join;
	
	boolean peer;
	String peer_ip;
	int peer_port;
	
	boolean local;
	String local_ip;
	int local_port;
	
	String name;
	
	// parsing UDP server socket parameter
	public SockInfo(String mreq_ip, String ip, int port, String name) {
		this.type = (byte)Sock_Type.UDPServer.getValue();
		this.mreq_ip = mreq_ip;
		this.ip = ip;
		this.port = port;
		
		this.auto_join = Sock_Type.None.getValue();
		this.p2p_join = Sock_Type.None.getValue();
		
		this.peer = false;
		this.peer_ip = null;
		this.peer_port = Sock_Type.None.getValue();
		
		this.local = false;
		this.local_ip = null;
		this.local_port = Sock_Type.None.getValue();
		
		this.name = name;
	}

	// parsing UDP client socket parameter
	public SockInfo(String peer_ip, int peer_port, String local_ip, int local_port, String name) {
		this.type = (byte)Sock_Type.UDPClient.getValue();
		this.mreq_ip = null;
		this.ip = null;
		this.port = Sock_Type.None.getValue();
		
		this.auto_join = Sock_Type.None.getValue();
		this.p2p_join = Sock_Type.None.getValue();
		
		this.peer = true;
		this.peer_ip = peer_ip;
		this.peer_port = peer_port;
		
		this.local = true;
		this.local_ip = local_ip;
		this.local_port = local_port;
		
		this.name = name;
	}
	
	// parsing TCP server socket parameter
	public SockInfo(String ip, int port, String name) {
		this.type = (byte)Sock_Type.TCPServer.getValue();
		this.mreq_ip = null;
		this.ip = ip;
		this.port = port;
		
		this.auto_join = Sock_Type.None.getValue();
		this.p2p_join = Sock_Type.None.getValue();
		
		this.peer = false;
		this.peer_ip = null;
		this.peer_port = Sock_Type.None.getValue();
		
		this.local = false;
		this.local_ip = null;
		this.local_port = Sock_Type.None.getValue();	
		
		this.name = name;
	}
	
	// parsing TCP client socket parameter
	public SockInfo(int auto_join, int p2p_join, String peer_ip, int peer_port, String local_ip, int local_port, String name) {
		this.type = (byte)Sock_Type.TCPClient.getValue();
		this.mreq_ip = null;
		this.ip = null;
		this.port = Sock_Type.None.getValue();
		
		this.auto_join = auto_join;
		this.p2p_join = p2p_join;
		
		this.peer = true;
		this.peer_ip = peer_ip;
		this.peer_port = peer_port;
		
		this.local = true;
		this.local_ip = local_ip;
		this.local_port = local_port;
		
		this.name = name;
	}
	
	@Override
	public String toString() {
		if (this.type == Sock_Type.UDPServer.getValue()) {
			return "mreq_ip : " + this.mreq_ip + "\nip : " + this.ip + "\nport : " + Integer.toString(this.port);			
		} else if (this.type == Sock_Type.UDPClient.getValue()) {			
			return "peer_ip : " + this.peer_ip + "\npeer_port : " + Integer.toString(this.peer_port) + "\nlocal_ip : " 
					+ this.local_ip + "\nlocal_port : " + Integer.toString(this.local_port);
		} else if (this.type == Sock_Type.TCPServer.getValue()) {			
			return "ip : " + this.ip + "\nport : " + Integer.toString(this.port);
		} else if (this.type == Sock_Type.TCPClient.getValue()) {
			return "auto_join : " + Integer.toString(this.auto_join) + "\np2p_join : " + Integer.toString(this.p2p_join) +
					"\npeer_ip : " + peer_ip + "\npeer_port : " + Integer.toString(this.peer_port) +
					"\nlocal_ip : " + local_ip + "\nlocal_port : " + Integer.toString(this.local_port);			
		} else {
			return "Not Exist Socket Type\n";
		}
	}
	
	public byte getSockType() {
		return this.type;
	}
	
	public String getMreqIp() {
		return this.mreq_ip;
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public int getPort() {
		return this.port;
	}
	
	public int getAutoJoin() {
		return this.auto_join;
	}
	
	public int getP2PJoin() {
		return this.p2p_join;
	}
	
	public String getPeerIp() {
		return this.peer_ip;
	}
	
	public int getPeerPort() {
		return this.peer_port;
	}
	
	public String getLocalIp() {
		return this.local_ip;
	}
	
	public int getLocalPort() {
		return this.local_port;
	}
	
	public String getName() {
		return this.name;
	}
}
