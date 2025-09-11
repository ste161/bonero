/*    */ package live.thought.jtminer.util;
/*    */ 
/*    */ import java.text.SimpleDateFormat;
/*    */ import java.util.Date;
/*    */ import org.fusesource.jansi.Ansi;
/*    */ import org.fusesource.jansi.AnsiConsole;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public abstract class Console
/*    */ {
/*    */   protected static int debugLevel;
/* 32 */   protected static final SimpleDateFormat sdf = new SimpleDateFormat("[YYYY-MM-dd HH:mm:ss] ");
/*    */   
/*    */   static {
/* 35 */     AnsiConsole.systemInstall();
/*    */   }
/*    */ 
/*    */   
/*    */   public static int getLevel() {
/* 40 */     return debugLevel;
/*    */   }
/*    */ 
/*    */   
/*    */   public static void setLevel(int level) {
/* 45 */     debugLevel = level;
/*    */   }
/*    */ 
/*    */   
/*    */   public static void print(Object content) {
/* 50 */     System.out.print(Ansi.ansi().render(content.toString()));
/*    */   }
/*    */ 
/*    */   
/*    */   public static void println(Object content) {
/* 55 */     System.out.println(Ansi.ansi().render(content.toString()));
/*    */   }
/*    */ 
/*    */   
/*    */   public static void output(Object content) {
/* 60 */     println(sdf.format(new Date()) + sdf.format(new Date()));
/*    */   }
/*    */ 
/*    */   
/*    */   public static void debug(Object content, int level) {
/* 65 */     if (level <= debugLevel)
/*    */     {
/* 67 */       output("@|faint,white " + content.toString() + "|@");
/*    */     }
/*    */   }
/*    */ 
/*    */   
/*    */   public static void end() {
/* 73 */     AnsiConsole.systemUninstall();
/*    */   }
/*    */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtmine\\util\Console.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */