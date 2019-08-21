package Util;

import Main.Global;

// Is currently only use Example Test

public class Runnable_Thread {
	private Global global;
	
	public Runnable_Thread(Global global) {
		this.global = global;
	}
	
	public void StartNode() {
		Runnable R_ConsensusThreadRun = new ConsensusThreadRun(this.global);
		Runnable R_ExampleThreadRun = new ExampleThreadRun(this.global);
		
		Thread m_ConsensusThread = new Thread(R_ConsensusThreadRun);
		Thread m_ExampleThread = new Thread(R_ExampleThreadRun);
		
		m_ConsensusThread.start();
		m_ExampleThread.start();
		
		try {
			m_ConsensusThread.join();
			m_ExampleThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	class ConsensusThreadRun implements Runnable {
		private Global global;
		
		public ConsensusThreadRun(Global global) {
			this.global = global;
		}
		@Override
		public void run() {
			this.global.getCConsensus().sock_init(this.global.getNodeJson());
		}
	}
	
	class ExampleThreadRun implements Runnable {
		private Global global;
		
		public ExampleThreadRun(Global global) {
			this.global = global;
		}
		
		@Override
		public void run() {
			this.global.getCExample().startExample();
		}
	}
}
