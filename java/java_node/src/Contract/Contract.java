package Contract;

import java.net.InetSocketAddress;

/////////////////////////////////////////////////////////////////////////////
// Contract class instance Communicate with SCA                            //
// between SCA and NNA communicate using Contract_TX, Contract_RX methods  //
/////////////////////////////////////////////////////////////////////////////

public class Contract {
	// For Communicate with SCA, Contract instance has SCA's SocketAddress
	private InetSocketAddress SCA_SockAddr;
	private int SCAport;
	
	public void setSCAport(int SCAport) {
		this.SCAport = SCAport;
	}
	
	public int getSCAport() {
		return this.SCAport;
	}
	
	public void setSCASocketAddress(String ip, int port) {
		this.SCA_SockAddr = new InetSocketAddress(ip, port);
	}
	
	public InetSocketAddress getSCASocketAddress() {
		return this.SCA_SockAddr;
	}
}
