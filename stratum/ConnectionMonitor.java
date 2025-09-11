/*     */ package live.thought.jtminer.stratum;
/*     */ 
/*     */ import java.util.concurrent.Executors;
/*     */ import java.util.concurrent.RejectedExecutionException;
/*     */ import java.util.concurrent.ScheduledExecutorService;
/*     */ import java.util.concurrent.ScheduledFuture;
/*     */ import java.util.concurrent.TimeUnit;
/*     */ import java.util.concurrent.atomic.AtomicBoolean;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import java.util.concurrent.atomic.AtomicLong;
/*     */ import live.thought.jtminer.util.Console;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ConnectionMonitor
/*     */ {
/*     */   private static final long MAX_NOTIFICATION_GAP_SECONDS = 60L;
/*     */   private static final long CHECK_INTERVAL_SECONDS = 5L;
/*     */   private static final int MAX_TIMEOUT_ALERTS = 2;
/*     */   private final StratumClient client;
/*     */   private ScheduledExecutorService scheduler;
/*     */   private ScheduledFuture<?> monitorTask;
/*     */   private final AtomicLong lastNotificationTime;
/*     */   private final AtomicBoolean isRunning;
/*  26 */   private final AtomicInteger timeoutAlertCount = new AtomicInteger(0);
/*     */   
/*     */   public ConnectionMonitor(StratumClient client) {
/*  29 */     this.client = client;
/*  30 */     this.scheduler = createScheduler();
/*  31 */     this.lastNotificationTime = new AtomicLong(System.currentTimeMillis());
/*  32 */     this.isRunning = new AtomicBoolean(false);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private ScheduledExecutorService createScheduler() {
/*  39 */     return Executors.newSingleThreadScheduledExecutor(r -> {
/*     */           Thread t = new Thread(r, "Connection-Monitor");
/*     */           t.setDaemon(true);
/*     */           return t;
/*     */         });
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void ensureValidScheduler() {
/*  50 */     if (this.scheduler == null || this.scheduler.isShutdown() || this.scheduler.isTerminated()) {
/*  51 */       if (this.scheduler != null) {
/*     */         try {
/*  53 */           this.scheduler.shutdownNow();
/*  54 */         } catch (Exception exception) {}
/*     */       }
/*     */ 
/*     */ 
/*     */       
/*  59 */       this.scheduler = createScheduler();
/*  60 */       Console.debug("Created new scheduler for connection monitor", 2);
/*     */     } 
/*     */   }
/*     */   
/*     */   public void start() {
/*  65 */     if (this.isRunning.compareAndSet(false, true)) {
/*     */       
/*  67 */       this.timeoutAlertCount.set(0);
/*  68 */       this.lastNotificationTime.set(System.currentTimeMillis());
/*     */ 
/*     */       
/*  71 */       ensureValidScheduler();
/*     */ 
/*     */       
/*  74 */       synchronized (this) {
/*  75 */         if (this.monitorTask != null) {
/*  76 */           this.monitorTask.cancel(false);
/*  77 */           this.monitorTask = null;
/*     */         } 
/*     */         
/*     */         try {
/*  81 */           this.monitorTask = this.scheduler.scheduleAtFixedRate(this::checkConnection, 5L, 5L, TimeUnit.SECONDS);
/*     */           
/*  83 */           Console.debug("Connection monitor task scheduled successfully", 2);
/*  84 */         } catch (RejectedExecutionException e) {
/*  85 */           Console.output("Failed to schedule connection monitor: " + e.getMessage() + ". Recreating scheduler.");
/*  86 */           ensureValidScheduler();
/*     */           
/*     */           try {
/*  89 */             this.monitorTask = this.scheduler.scheduleAtFixedRate(this::checkConnection, 5L, 5L, TimeUnit.SECONDS);
/*     */             
/*  91 */             Console.debug("Connection monitor task scheduled after recreation", 2);
/*  92 */           } catch (Exception e2) {
/*  93 */             Console.output("Critical error scheduling connection monitor: " + e2.getMessage());
/*  94 */             this.isRunning.set(false);
/*     */             
/*     */             return;
/*     */           } 
/*     */         } 
/*     */       } 
/* 100 */       Console.debug("Connection monitor started", 2);
/*     */     }
/*     */     else {
/*     */       
/* 104 */       this.lastNotificationTime.set(System.currentTimeMillis());
/* 105 */       this.timeoutAlertCount.set(0);
/* 106 */       Console.debug("Connection monitor already running, timestamp reset", 2);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   private void checkConnection() {
/*     */     try {
/* 113 */       if (!this.client.isConnected()) {
/*     */         
/* 115 */         Console.debug("Connection already marked as disconnected", 2);
/*     */         
/*     */         return;
/*     */       } 
/* 119 */       long currentTime = System.currentTimeMillis();
/* 120 */       long lastNotif = this.lastNotificationTime.get();
/* 121 */       long timeSinceLastNotification = currentTime - lastNotif;
/*     */ 
/*     */       
/* 124 */       if (timeSinceLastNotification > TimeUnit.SECONDS.toMillis(60L)) {
/* 125 */         int alerts = this.timeoutAlertCount.incrementAndGet();
/* 126 */         Console.output("No mining notifications received in " + 
/* 127 */             String.format("%.1f", new Object[] { Double.valueOf(timeSinceLastNotification / 1000.0D / 60.0D) }) + " minutes (alert #" + alerts + ")");
/*     */ 
/*     */ 
/*     */         
/*     */         try {
/* 132 */           boolean pingResult = this.client.ping();
/* 133 */           if (!pingResult) {
/* 134 */             Console.output("Ping test failed, connection appears to be lost");
/*     */ 
/*     */             
/* 137 */             if (this.client.isConnected()) {
/* 138 */               Console.output("Client state inconsistent - forcing disconnection");
/* 139 */               this.client.setConnected(false);
/*     */             } 
/*     */             
/* 142 */             handleConnectionLost();
/*     */             return;
/*     */           } 
/* 145 */           Console.debug("Ping test successful but no mining notifications received", 2);
/*     */         }
/* 147 */         catch (Exception e) {
/* 148 */           Console.output("Ping test threw exception: " + e.getMessage());
/* 149 */           handleConnectionLost();
/*     */           
/*     */           return;
/*     */         } 
/*     */         
/* 154 */         if (timeSinceLastNotification > TimeUnit.SECONDS.toMillis(180L)) {
/* 155 */           Console.output("Long period without communication (" + 
/* 156 */               String.format("%.1f", new Object[] { Double.valueOf(timeSinceLastNotification / 1000.0D / 60.0D) }) + " minutes), forcing reconnection");
/*     */           
/* 158 */           handleConnectionLost();
/*     */           
/*     */           return;
/*     */         } 
/*     */         
/* 163 */         if (alerts >= 2) {
/* 164 */           Console.output("Connection appears stale after " + 
/* 165 */               String.format("%.1f", new Object[] { Double.valueOf((60L * alerts) / 60.0D) }) + " minutes with no activity. Forcing reconnection.");
/*     */           
/* 167 */           handleConnectionLost();
/*     */         }
/*     */       
/*     */       }
/* 171 */       else if (this.timeoutAlertCount.get() > 0) {
/* 172 */         this.timeoutAlertCount.set(0);
/* 173 */         Console.debug("Connection is active, alert count reset", 2);
/*     */       }
/*     */     
/* 176 */     } catch (Exception e) {
/* 177 */       Console.debug("Error in connection check: " + e.getMessage(), 2);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void handleConnectionLost() {
/* 182 */     this.client.handleConnectionTimeout();
/*     */     
/* 184 */     this.timeoutAlertCount.set(0);
/*     */   }
/*     */   
/*     */   public void notificationReceived() {
/* 188 */     this.lastNotificationTime.set(System.currentTimeMillis());
/*     */     
/* 190 */     this.timeoutAlertCount.set(0);
/* 191 */     Console.debug("Mining notification received, connection alive", 2);
/*     */   }
/*     */ 
/*     */   
/*     */   public void forceCheck() {
/* 196 */     Console.debug("Forcing connection check", 2);
/*     */ 
/*     */     
/* 199 */     if (this.isRunning.get()) {
/*     */       try {
/* 201 */         checkConnection();
/* 202 */       } catch (Exception e) {
/* 203 */         Console.debug("Error during forced connection check: " + e.getMessage(), 2);
/*     */       } 
/*     */     } else {
/* 206 */       Console.debug("Cannot force check - connection monitor not running", 2);
/*     */     } 
/*     */   }
/*     */   
/*     */   public void stop() {
/* 211 */     if (this.isRunning.compareAndSet(true, false)) {
/* 212 */       synchronized (this) {
/* 213 */         if (this.monitorTask != null) {
/* 214 */           this.monitorTask.cancel(true);
/* 215 */           this.monitorTask = null;
/*     */         } 
/*     */         
/* 218 */         Console.debug("Connection monitor task cancelled", 2);
/*     */       } 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 224 */       Console.debug("Connection monitor stopped", 2);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\stratum\ConnectionMonitor.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */