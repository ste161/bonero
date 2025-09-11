/*     */ package live.thought.jtminer.stratum;
/*     */ 
/*     */ import java.util.concurrent.ConcurrentLinkedQueue;
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.ScheduledExecutorService;
/*     */ import java.util.concurrent.ScheduledFuture;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.concurrent.atomic.AtomicBoolean;
/*     */ import live.thought.jtminer.util.Console;
/*     */ import live.thought.jtminer.util.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class RejectionMonitor
/*     */ {
/*     */   private static final int MAX_REJECTIONS_THRESHOLD = 5;
/*     */   private static final long TIME_WINDOW_MINUTES = 5L;
/*     */   private static final long CHECK_INTERVAL_SECONDS = 30L;
/*     */   private static final long RESET_AFTER_SUCCESS_MINUTES = 2L;
/*     */   private final ConcurrentLinkedQueue<Long> rejectionTimestamps;
/*     */   private final ScheduledExecutorService scheduler;
/*     */   private ScheduledFuture<?> monitorTask;
/*     */   private final AtomicBoolean isRunning;
/*  28 */   private volatile long lastSuccessfulShare = 0L;
/*     */ 
/*     */ 
/*     */   
/*     */   private final RestartCallback restartCallback;
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public RejectionMonitor(RestartCallback callback) {
/*  38 */     this.rejectionTimestamps = new ConcurrentLinkedQueue<>();
/*  39 */     this.restartCallback = callback;
/*  40 */     this.isRunning = new AtomicBoolean(false);
/*  41 */     this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
/*     */           Thread t = new Thread(r, "Rejection-Monitor");
/*     */           t.setDaemon(true);
/*     */           return t;
/*     */         });
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void start() {
/*  52 */     if (this.isRunning.compareAndSet(false, true)) {
/*  53 */       Logger.log("Rejection monitor started - will trigger restart if > 5 rejections in 5 minutes");
/*     */ 
/*     */       
/*  56 */       this.monitorTask = this.scheduler.scheduleAtFixedRate(this::checkRejectionRate, 30L, 30L, TimeUnit.SECONDS);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  63 */       Console.debug("Rejection monitor scheduled successfully", 2);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void stop() {
/*  71 */     if (this.isRunning.compareAndSet(true, false)) {
/*  72 */       if (this.monitorTask != null) {
/*  73 */         this.monitorTask.cancel(true);
/*  74 */         this.monitorTask = null;
/*     */       } 
/*     */       
/*  77 */       Logger.log("Rejection monitor stopped");
/*  78 */       Console.debug("Rejection monitor stopped", 2);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void cleanup() {
/*  86 */     stop();
/*  87 */     this.rejectionTimestamps.clear();
/*  88 */     if (!this.scheduler.isShutdown()) {
/*  89 */       this.scheduler.shutdownNow();
/*     */       try {
/*  91 */         if (!this.scheduler.awaitTermination(2L, TimeUnit.SECONDS)) {
/*  92 */           Console.debug("Rejection monitor scheduler did not terminate within timeout", 2);
/*     */         }
/*  94 */       } catch (InterruptedException e) {
/*  95 */         Thread.currentThread().interrupt();
/*  96 */         Console.debug("Interrupted while waiting for rejection monitor shutdown", 2);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void recordRejection() {
/* 106 */     long currentTime = System.currentTimeMillis();
/* 107 */     this.rejectionTimestamps.offer(Long.valueOf(currentTime));
/*     */     
/* 109 */     Logger.log("Share rejection recorded at " + currentTime + " (total in queue: " + this.rejectionTimestamps
/* 110 */         .size() + ")");
/*     */ 
/*     */     
/* 113 */     cleanOldTimestamps(currentTime);
/*     */ 
/*     */     
/* 116 */     if (this.isRunning.get()) {
/* 117 */       checkRejectionRate();
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void recordSuccessfulShare() {
/* 126 */     this.lastSuccessfulShare = System.currentTimeMillis();
/* 127 */     Logger.log("Successful share recorded at " + this.lastSuccessfulShare);
/*     */ 
/*     */ 
/*     */     
/* 131 */     checkForStabilityReset();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void checkForStabilityReset() {
/* 139 */     if (this.lastSuccessfulShare == 0L)
/*     */       return; 
/* 141 */     long currentTime = System.currentTimeMillis();
/* 142 */     long timeSinceLastSuccess = currentTime - this.lastSuccessfulShare;
/*     */ 
/*     */ 
/*     */     
/* 146 */     if (timeSinceLastSuccess < TimeUnit.MINUTES.toMillis(2L)) {
/* 147 */       int currentRejections = getCurrentRejectionCount();
/*     */ 
/*     */ 
/*     */       
/* 151 */       if (currentRejections < 3 && currentRejections > 0) {
/* 152 */         Logger.log("Connection appears stable (successful shares + low rejections), considering partial reset");
/*     */         
/* 154 */         partialReset();
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void partialReset() {
/* 164 */     long currentTime = System.currentTimeMillis();
/* 165 */     long shorterCutoff = currentTime - TimeUnit.MINUTES.toMillis(2L);
/*     */     
/* 167 */     int removed = 0;
/* 168 */     while (!this.rejectionTimestamps.isEmpty() && ((Long)this.rejectionTimestamps.peek()).longValue() < shorterCutoff) {
/* 169 */       this.rejectionTimestamps.poll();
/* 170 */       removed++;
/*     */     } 
/*     */     
/* 173 */     if (removed > 0) {
/* 174 */       Logger.log("Partial reset performed: removed " + removed + " old rejections due to stable connection");
/* 175 */       Console.debug("Connection stabilized - partial rejection reset applied", 2);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void resetAfterReconnection() {
/* 183 */     this.rejectionTimestamps.clear();
/* 184 */     this.lastSuccessfulShare = 0L;
/* 185 */     Logger.log("Complete rejection monitor reset after successful reconnection");
/* 186 */     Console.output("@|green Rejection monitor reset - starting fresh after reconnection|@");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void checkRejectionRate() {
/* 193 */     if (!this.isRunning.get()) {
/*     */       return;
/*     */     }
/*     */     
/* 197 */     long currentTime = System.currentTimeMillis();
/*     */ 
/*     */     
/* 200 */     cleanOldTimestamps(currentTime);
/*     */     
/* 202 */     int recentRejections = this.rejectionTimestamps.size();
/*     */     
/* 204 */     Logger.log("Rejection check: " + recentRejections + " rejections in last 5 minutes (threshold: 5)");
/*     */ 
/*     */     
/* 207 */     if (recentRejections >= 5) {
/* 208 */       String reason = "Too many rejections: " + recentRejections + " rejections in 5 minutes (threshold: 5)";
/*     */ 
/*     */       
/* 211 */       Logger.log("REJECTION THRESHOLD EXCEEDED: " + reason);
/* 212 */       Console.output("@|red WARNING: " + reason + "|@");
/* 213 */       Console.output("@|yellow Triggering automatic restart to recover connection quality...|@");
/*     */ 
/*     */       
/* 216 */       stop();
/*     */ 
/*     */       
/* 219 */       if (this.restartCallback != null) {
/*     */         try {
/* 221 */           this.restartCallback.triggerRestart(reason);
/* 222 */         } catch (Exception e) {
/* 223 */           Logger.log("Error during restart callback: " + e.getMessage());
/* 224 */           Console.output("@|red Error triggering restart: " + e.getMessage() + "|@");
/*     */         } 
/*     */       }
/* 227 */     } else if (recentRejections > 0) {
/*     */       
/* 229 */       Console.debug("Rejection rate acceptable: " + recentRejections + "/5 in 5 minutes", 2);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void cleanOldTimestamps(long currentTime) {
/* 238 */     long cutoffTime = currentTime - TimeUnit.MINUTES.toMillis(5L);
/*     */ 
/*     */     
/* 241 */     while (!this.rejectionTimestamps.isEmpty() && ((Long)this.rejectionTimestamps.peek()).longValue() < cutoffTime) {
/* 242 */       Long removed = this.rejectionTimestamps.poll();
/* 243 */       Logger.log("Removed old rejection timestamp: " + removed + " (older than 5 minutes)");
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public int getCurrentRejectionCount() {
/* 252 */     cleanOldTimestamps(System.currentTimeMillis());
/* 253 */     return this.rejectionTimestamps.size();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean isRunning() {
/* 260 */     return this.isRunning.get();
/*     */   }
/*     */   
/*     */   public static interface RestartCallback {
/*     */     void triggerRestart(String param1String);
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\stratum\RejectionMonitor.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */