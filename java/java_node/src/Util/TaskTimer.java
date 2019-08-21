package Util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import Consensus.Consensus_TX;

import java.util.TimerTask;

import Main.Global;

// When Using Setting Block Gen Timer

public class TaskTimer {
	private Global global;

	@SuppressWarnings({ "rawtypes", "unused" })
	private ScheduledFuture TxF;
	@SuppressWarnings("rawtypes")
	private ScheduledFuture bgtF;
	@SuppressWarnings("rawtypes")
	private ScheduledFuture commF;
	
	public TaskTimer(Global global) {
		this.global = global;
		this.TxF = null;
		this.bgtF = null;
		this.commF = null;
	}

	public void TxTimer(int delay, int period) {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		this.TxF = service.scheduleAtFixedRate(new TxTaskTimerRun(this.global), delay, period, TimeUnit.MILLISECONDS);
	}
	
	public void BlkGenTimer(int delay, int period, long StartTime) {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		// Periodically after delay execute BlkGenTimer Task
		// StartTime = BlkGenTime
		this.bgtF = service.scheduleAtFixedRate(new BlockGenTaskTimerRun(this.global, StartTime), delay, period, TimeUnit.MILLISECONDS);
	}

	public void CommonTaskTimer(int delay, int period) {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		// Periodically after delay execute CommonTask 
		// This method For Versatile Task 
		this.commF = service.scheduleAtFixedRate(new CommonTaskTimerRun(this.global), delay, period, TimeUnit.MILLISECONDS);
	}
	
	public void SpecificTaskTimer(TimerTask task, int delay, int period) throws InterruptedException {
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		// Periodically after delay execute Specific Task(argument task)
		// When Passing TimerTask argument
		service.scheduleAtFixedRate(task, delay, period, TimeUnit.MILLISECONDS);
	}

	class TxTaskTimerRun implements Runnable {
		private Global global;
		private Consensus_TX consensus_tx;

		private long DBKeyL;
		private byte[] DBKey;
		private byte[] Hash;
		
		public TxTaskTimerRun(Global global) {
			this.global = global;
			this.consensus_tx = new Consensus_TX(this.global);

			this.Hash = new byte[this.global.getCConsensus().getConsDefLen("Hash")];
			this.DBKeyL = 0;
		}
		@Override
		public void run() {
			
			long timeStamp = this.global.getCTimeStamp().getCurrentTimeStampL();
			
			this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "Tx TimeStmap : " + timeStamp);

			this.DBKey = this.global.getCTypeCast().LongToByteArray(this.DBKeyL++);
			this.Hash[0]++;
			// Send Transaction
			this.consensus_tx.Tx(this.DBKey, this.Hash);

		}
	}
	
	class BlockGenTaskTimerRun implements Runnable {
		private Global global;
		private Consensus_TX consensus_tx;
		
		private long startTime;
		private boolean exe;
		
		public BlockGenTaskTimerRun(Global global, long StartTime) {
			this.global = global;
			this.consensus_tx = new Consensus_TX(this.global);
			
			this.startTime = StartTime;
			this.exe = false;
		}
		@Override
		public void run() {
			long timeStamp = this.global.getCTimeStamp().getCurrentTimeStampL();
			
			if (this.startTime == 0)
			{
				this.exe = true;
			}
			else
			{
				if (this.startTime < timeStamp)
				{
					this.exe = true;
				}
			}

			if (this.exe == true)
			{
				bgtF.cancel(true);
				// Release Timer
				bgtF = null;
				this.global.getCLogger().OutConsole(this.global.getCLogger().LOG_DEBUG, "BlockGenTaskTimerRun TimeStmap : " + timeStamp);

				// Send Transaction Stop Req
				this.consensus_tx.TxStopReq();
			}
		}
	}
	
	class CommonTaskTimerRun implements Runnable {
		@SuppressWarnings("unused")
		private Global global;
		public CommonTaskTimerRun(Global global) {
			this.global = global;
		}
		@Override
		public void run() {
			commF.cancel(true);
			// do Something
		}
	}
	
	
}

