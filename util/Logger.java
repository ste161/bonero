/*     */ package live.thought.jtminer.util;
/*     */ 
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.time.LocalDateTime;
/*     */ import java.time.format.DateTimeFormatter;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Logger
/*     */ {
/*     */   private static boolean loggingEnabled = false;
/*  23 */   private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
/*  24 */   private static final DateTimeFormatter LOG_TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
/*  25 */   private static final String INSTANCE_ID = String.format("%06X", new Object[] { Integer.valueOf((int)(Math.random() * 1.6777215E7D)) });
/*     */ 
/*     */ 
/*     */   
/*     */   private static final String LOG_FILE_PREFIX = "Log-";
/*     */ 
/*     */   
/*     */   private static final String LOG_FILE_EXTENSION = ".txt";
/*     */ 
/*     */ 
/*     */   
/*     */   public static void setLoggingEnabled(boolean enabled) {
/*  37 */     loggingEnabled = enabled;
/*  38 */     if (enabled)
/*     */     {
/*  40 */       log("Logging system enabled with instance ID: " + INSTANCE_ID);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static boolean isLoggingEnabled() {
/*  50 */     return loggingEnabled;
/*     */   }
/*     */   
/*     */   public static String getInstanceId() {
/*  54 */     return INSTANCE_ID;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static void log(String information) {
/*  68 */     if (!loggingEnabled) {
/*     */       return;
/*     */     }
/*     */     
/*  72 */     if (information == null) {
/*  73 */       information = "null";
/*     */     }
/*     */     
/*  76 */     LocalDateTime now = LocalDateTime.now();
/*  77 */     String fileName = generateLogFileName(now);
/*  78 */     String logEntry = formatLogEntry(now, information);
/*     */     
/*  80 */     try { BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true)); 
/*  81 */       try { writer.write(logEntry);
/*  82 */         writer.newLine();
/*  83 */         writer.flush();
/*  84 */         writer.close(); } catch (Throwable throwable) { try { writer.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }  } catch (IOException e)
/*     */     
/*     */     { 
/*  87 */       System.err.println("Erreur lors de l'écriture du log: " + e.getMessage());
/*  88 */       System.err.println("Message original: " + information); }
/*     */   
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static String generateLogFileName(LocalDateTime dateTime) {
/*  99 */     return "Log-" + dateTime.format(FILE_DATE_FORMAT) + "-" + INSTANCE_ID + ".txt";
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static String formatLogEntry(LocalDateTime dateTime, String information) {
/* 110 */     return dateTime.format(LOG_TIMESTAMP_FORMAT) + " - " + dateTime.format(LOG_TIMESTAMP_FORMAT);
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtmine\\util\Logger.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */