/*     */ package live.thought.jtminer.stratum;
/*     */ 
/*     */ import java.util.concurrent.CountDownLatch;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.RejectedExecutionException;
/*     */ import java.util.concurrent.ScheduledExecutorService;
/*     */ import java.util.concurrent.ScheduledFuture;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.concurrent.atomic.AtomicBoolean;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import live.thought.jtminer.StratumMiner;
/*     */ import live.thought.jtminer.util.Console;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ReconnectionHandler
/*     */ {
/*     */   private static final int INITIAL_DELAY_SECONDS = 1;
/*     */   private static final int MAX_DELAY_SECONDS = 300;
/*     */   private static final double BACKOFF_MULTIPLIER = 1.5D;
/*     */   private static final long STOP_TIMEOUT_MS = 5000L;
/*     */   private static final int MAX_CONSECUTIVE_FAILURES = 100;
/*     */   private static final long MAX_RECONNECTION_TIME_MS = 600000L;
/*  24 */   private long reconnectionStartTime = 0L;
/*     */   
/*     */   private final StratumClient client;
/*     */   private ScheduledExecutorService executor;
/*     */   private ScheduledFuture<?> reconnectTask;
/*  29 */   private final AtomicBoolean isReconnecting = new AtomicBoolean(false);
/*  30 */   private final AtomicInteger attemptCount = new AtomicInteger(0);
/*  31 */   private final AtomicInteger failureCount = new AtomicInteger(0);
/*  32 */   private int currentDelay = 1;
/*     */   private StratumMiner.WorkHandler workHandler;
/*  34 */   private final Object executorLock = new Object();
/*  35 */   private final Object reconnectLock = new Object();
/*     */   
/*     */   public ReconnectionHandler(StratumClient client) {
/*  38 */     this.client = client;
/*  39 */     createExecutor();
/*     */   }
/*     */   
/*     */   private void createExecutor() {
/*  43 */     synchronized (this.executorLock) {
/*     */       
/*  45 */       cleanupExecutor();
/*     */ 
/*     */       
/*  48 */       this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
/*     */             Thread t = new Thread(r, "Reconnection-Handler");
/*     */             t.setDaemon(true);
/*     */             return t;
/*     */           });
/*  53 */       Console.debug("Created new executor for reconnection handler", 2);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void cleanupExecutor() {
/*  58 */     synchronized (this.executorLock) {
/*     */       
/*  60 */       if (this.reconnectTask != null && !this.reconnectTask.isDone()) {
/*  61 */         this.reconnectTask.cancel(true);
/*  62 */         this.reconnectTask = null;
/*  63 */         Console.debug("Cancelled active reconnection task", 2);
/*     */       } 
/*     */ 
/*     */       
/*  67 */       if (this.executor != null && !this.executor.isShutdown()) {
/*     */         try {
/*  69 */           Console.debug("Shutting down executor", 2);
/*  70 */           this.executor.shutdownNow();
/*  71 */           if (!this.executor.awaitTermination(2L, TimeUnit.SECONDS)) {
/*  72 */             Console.debug("Executor did not terminate within timeout - resources may leak", 2);
/*     */           } else {
/*  74 */             Console.debug("Executor terminated successfully", 2);
/*     */           } 
/*  76 */         } catch (InterruptedException e) {
/*  77 */           Thread.currentThread().interrupt();
/*  78 */           Console.debug("Interrupted while waiting for executor shutdown", 2);
/*  79 */         } catch (Exception e) {
/*  80 */           Console.debug("Error shutting down executor: " + e.getMessage(), 2);
/*     */         } 
/*     */       }
/*     */     } 
/*     */   }
/*     */   
/*     */   public void setWorkHandler(StratumMiner.WorkHandler handler) {
/*  87 */     this.workHandler = handler;
/*     */   }
/*     */ 
/*     */   
/*     */   public void startReconnection() {
/*  92 */     synchronized (this.reconnectLock) {
/*  93 */       boolean wasAlreadyReconnecting = this.isReconnecting.getAndSet(true);
/*     */       
/*  95 */       if (wasAlreadyReconnecting) {
/*     */         
/*  97 */         long elapsedTime = System.currentTimeMillis() - this.reconnectionStartTime;
/*  98 */         if (elapsedTime > 600000L) {
/*  99 */           Console.output("Reconnection timeout exceeded (" + elapsedTime / 1000L + "s), restarting process");
/*     */           
/* 101 */           cleanupExecutor();
/* 102 */           this.attemptCount.set(0);
/* 103 */           this.failureCount.set(0);
/* 104 */           this.currentDelay = 1;
/* 105 */           this.reconnectionStartTime = System.currentTimeMillis();
/*     */         } 
/*     */         
/* 108 */         Console.output("New disconnection detected while already reconnecting. Restarting reconnection process.");
/*     */ 
/*     */         
/* 111 */         cleanupExecutor();
/*     */ 
/*     */         
/* 114 */         this.failureCount.incrementAndGet();
/*     */       } else {
/*     */         
/* 117 */         this.attemptCount.set(0);
/* 118 */         this.failureCount.set(0);
/* 119 */         this.currentDelay = 1;
/* 120 */         this.reconnectionStartTime = System.currentTimeMillis();
/* 121 */         Console.output("Starting reconnection process...");
/*     */       } 
/*     */       
/*     */       try {
/* 125 */         if (this.workHandler != null) {
/* 126 */           Console.output("Stopping all mining operations for reconnection...");
/* 127 */           stopMiningOperations();
/*     */         } 
/*     */ 
/*     */         
/* 131 */         createExecutor();
/*     */ 
/*     */         
/* 134 */         scheduleReconnection(0);
/* 135 */       } catch (Exception e) {
/* 136 */         Console.output("Error during reconnection setup: " + e.getMessage());
/*     */         
/* 138 */         createExecutor();
/* 139 */         scheduleReconnection(1);
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void scheduleReconnection(int delaySeconds) {
/* 145 */     synchronized (this.executorLock) {
/*     */       
/* 147 */       if (!this.isReconnecting.get()) {
/* 148 */         Console.debug("Reconnection process stopped, cancelling scheduling", 2);
/*     */         
/*     */         return;
/*     */       } 
/*     */       
/* 153 */       int delay = (delaySeconds >= 0) ? delaySeconds : this.currentDelay;
/*     */ 
/*     */       
/* 156 */       delay = Math.min(delay, 300);
/*     */       
/* 158 */       int currentAttempt = this.attemptCount.get() + 1;
/* 159 */       Console.output("Scheduling reconnection attempt in " + delay + " seconds (attempt #" + currentAttempt + ")");
/*     */ 
/*     */       
/* 162 */       if (this.executor == null || this.executor.isShutdown()) {
/* 163 */         Console.debug("Executor invalid, creating new one", 2);
/* 164 */         createExecutor();
/*     */       } 
/*     */ 
/*     */       
/*     */       try {
/* 169 */         if (this.reconnectTask != null && !this.reconnectTask.isDone()) {
/* 170 */           this.reconnectTask.cancel(false);
/*     */         }
/*     */         
/* 173 */         this.reconnectTask = this.executor.schedule(this::reconnect, delay, TimeUnit.SECONDS);
/* 174 */         Console.debug("Reconnection scheduled successfully", 3);
/* 175 */       } catch (RejectedExecutionException e) {
/* 176 */         Console.output("Failed to schedule reconnection: " + e.getMessage());
/*     */         
/* 178 */         createExecutor();
/*     */         try {
/* 180 */           this.reconnectTask = this.executor.schedule(this::reconnect, delay, TimeUnit.SECONDS);
/* 181 */           Console.debug("Reconnection scheduled after executor recreation", 2);
/* 182 */         } catch (Exception e2) {
/* 183 */           Console.output("Critical error scheduling reconnection: " + e2.getMessage());
/*     */ 
/*     */           
/* 186 */           int finalDelay = delay;
/* 187 */           Thread emergencyThread = new Thread(() -> {
/*     */                 try {
/*     */                   Thread.sleep(TimeUnit.SECONDS.toMillis(finalDelay));
/*     */                   reconnect();
/* 191 */                 } catch (InterruptedException ie) {
/*     */                   Thread.currentThread().interrupt();
/*     */                 } 
/*     */               }"Emergency-Reconnect");
/* 195 */           emergencyThread.setDaemon(true);
/* 196 */           emergencyThread.start();
/* 197 */           Console.output("Using emergency reconnection thread due to executor failures");
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void reconnect() {
/* 205 */     if (!this.isReconnecting.get()) {
/* 206 */       Console.debug("Reconnection cancelled before execution", 2);
/*     */       
/*     */       return;
/*     */     } 
/*     */     
/* 211 */     int currentAttempt = this.attemptCount.incrementAndGet();
/*     */ 
/*     */     
/* 214 */     Console.debug("[" + System.currentTimeMillis() + "] Reconnection attempt #" + currentAttempt + " starting", 2);
/*     */ 
/*     */     
/* 217 */     if (currentAttempt % 500 == 0) {
/* 218 */       Console.output("Still attempting to reconnect after " + currentAttempt + " tries. Performing memory cleanup.");
/* 219 */       System.gc();
/*     */     } 
/*     */     
/*     */     try {
/* 223 */       Console.output("Attempting to reconnect to mining pool... (attempt #" + currentAttempt + ")");
/* 224 */       this.client.connect();
/*     */ 
/*     */       
/* 227 */       if (this.client.isConnected()) {
/* 228 */         Console.output("Successfully reconnected to mining pool after " + currentAttempt + " attempts");
/*     */ 
/*     */ 
/*     */         
/*     */         try {
/* 233 */           ConnectionMonitor monitor = this.client.getConnectionMonitor();
/* 234 */           if (monitor != null) {
/* 235 */             monitor.stop();
/* 236 */             Thread.sleep(100L);
/* 237 */             monitor.start();
/* 238 */             Console.output("Connection monitor explicitly restarted after reconnection");
/*     */           } 
/* 240 */         } catch (Exception e) {
/* 241 */           Console.output("Warning: Error restarting connection monitor: " + e.getMessage());
/*     */         } 
/*     */ 
/*     */         
/*     */         try {
/* 246 */           if (this.workHandler != null) {
/* 247 */             Console.output("Attempting to restart mining operations after reconnection...");
/*     */ 
/*     */             
/* 250 */             Thread.sleep(500L);
/*     */ 
/*     */             
/* 253 */             StratumWork currentWork = this.client.getCurrentWork();
/* 254 */             if (currentWork != null) {
/* 255 */               Console.output("Restarting mining with existing work: " + currentWork.getJobId());
/* 256 */               this.workHandler.handleNewWork(currentWork);
/*     */             } else {
/* 258 */               Console.output("No current work available, mining will restart when new job arrives");
/*     */             } 
/*     */           } 
/* 261 */         } catch (Exception e) {
/* 262 */           Console.output("Warning: Error restarting mining operations: " + e.getMessage());
/*     */         } 
/*     */ 
/*     */ 
/*     */         
/* 267 */         synchronized (this.reconnectLock) {
/* 268 */           this.isReconnecting.set(false);
/* 269 */           this.attemptCount.set(0);
/* 270 */           this.failureCount.set(0);
/* 271 */           this.currentDelay = 1;
/*     */         } 
/*     */         
/* 274 */         Console.output("Reconnection process completed successfully");
/*     */         return;
/*     */       } 
/* 277 */       this.failureCount.incrementAndGet();
/* 278 */       Console.output("Connection attempt #" + currentAttempt + " failed without exception");
/*     */     }
/* 280 */     catch (StratumException e) {
/* 281 */       this.failureCount.incrementAndGet();
/* 282 */       Console.output("Reconnection attempt #" + currentAttempt + " failed: " + e.getMessage());
/* 283 */     } catch (Exception e) {
/* 284 */       this.failureCount.incrementAndGet();
/* 285 */       Console.output("Unexpected error during reconnection attempt #" + currentAttempt + ": " + e.getMessage());
/*     */     } 
/*     */ 
/*     */     
/* 289 */     if (this.failureCount.get() > 100) {
/* 290 */       Console.output("Exceeded maximum reconnection failures (100). Will continue but may indicate a serious problem.");
/* 291 */       this.failureCount.set(50);
/*     */     } 
/*     */ 
/*     */     
/* 295 */     synchronized (this.reconnectLock) {
/* 296 */       if (this.isReconnecting.get()) {
/*     */ 
/*     */         
/* 299 */         int actualMaxDelay = (this.failureCount.get() > 10) ? Math.min(300, 60) : 300;
/*     */         
/* 301 */         this.currentDelay = (int)Math.min(this.currentDelay * 1.5D, actualMaxDelay);
/*     */ 
/*     */ 
/*     */         
/* 305 */         int jitter = (int)(this.currentDelay * 0.1D);
/* 306 */         if (jitter > 0) {
/* 307 */           this.currentDelay = Math.max(1, this.currentDelay - jitter + (int)(Math.random() * jitter * 2.0D));
/*     */         }
/*     */         
/* 310 */         Console.debug("Next reconnection attempt in " + this.currentDelay + " seconds", 2);
/* 311 */         scheduleReconnection(-1);
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void stopMiningOperations() {
/* 317 */     if (this.workHandler == null)
/*     */       return; 
/* 319 */     CountDownLatch stopLatch = new CountDownLatch(1);
/* 320 */     Thread stopThread = new Thread(() -> {
/*     */           try {
/*     */             this.workHandler.stop();
/* 323 */           } catch (Exception e) {
/*     */             Console.debug("Error stopping mining operations: " + e.getMessage(), 2);
/*     */           } finally {
/*     */             stopLatch.countDown();
/*     */           } 
/*     */         }"Mining-Stop-Thread");
/*     */     
/* 330 */     stopThread.setDaemon(true);
/* 331 */     stopThread.start();
/*     */     
/*     */     try {
/* 334 */       if (!stopLatch.await(5000L, TimeUnit.MILLISECONDS)) {
/* 335 */         Console.output("Warning: Mining operations stop timeout - forcing shutdown");
/* 336 */         stopThread.interrupt();
/*     */       } 
/* 338 */     } catch (InterruptedException e) {
/* 339 */       Thread.currentThread().interrupt();
/* 340 */       Console.debug("Interrupted while stopping mining operations", 2);
/*     */     } 
/*     */   }
/*     */   
/*     */   public void stop() {
/* 345 */     synchronized (this.reconnectLock) {
/* 346 */       this.isReconnecting.set(false);
/*     */     } 
/*     */     
/* 349 */     cleanupExecutor();
/*     */   }
/*     */   
/*     */   public boolean isReconnecting() {
/* 353 */     return this.isReconnecting.get();
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\stratum\ReconnectionHandler.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */