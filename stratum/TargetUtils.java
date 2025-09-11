/*     */ package live.thought.jtminer.stratum;
/*     */ 
/*     */ import java.math.BigInteger;
/*     */ import live.thought.jtminer.data.DataUtils;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class TargetUtils
/*     */ {
/*     */   private static final String MAX_TARGET_HEX = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";
/*  12 */   private static final BigInteger MAX_TARGET = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
/*  13 */   private static final BigInteger FACTOR = BigInteger.valueOf(10000L);
/*     */   
/*     */   private static final double MIN_DIFFICULTY = 1.0E-6D;
/*     */   
/*     */   private static final int TARGET_SIZE = 32;
/*     */   private static final int CACHE_SIZE = 100;
/*  19 */   private static final DifficultyCache difficultyCache = new DifficultyCache(100);
/*     */ 
/*     */   
/*     */   private TargetUtils() {
/*  23 */     throw new AssertionError("TargetUtils is a utility class and should not be instantiated");
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static byte[] diffToTarget(double diff) {
/*  34 */     validateDifficulty(diff);
/*     */ 
/*     */     
/*  37 */     byte[] cachedTarget = difficultyCache.get(diff);
/*  38 */     if (cachedTarget != null) {
/*  39 */       return (byte[])cachedTarget.clone();
/*     */     }
/*     */ 
/*     */     
/*  43 */     byte[] target = calculateTarget(Math.max(diff, 1.0E-6D));
/*     */ 
/*     */     
/*  46 */     difficultyCache.put(diff, target);
/*     */     
/*  48 */     return (byte[])target.clone();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static byte[] calculateTarget(double diff) {
/*  58 */     BigInteger diffInt = BigInteger.valueOf((long)(diff * 10000.0D));
/*  59 */     BigInteger currentTarget = MAX_TARGET.multiply(FACTOR).divide(diffInt);
/*     */     
/*  61 */     byte[] targetBytes = new byte[32];
/*  62 */     byte[] currentTargetBytes = currentTarget.toByteArray();
/*     */ 
/*     */     
/*  65 */     int startPos = Math.max(0, 32 - currentTargetBytes.length);
/*  66 */     System.arraycopy(currentTargetBytes, 0, targetBytes, startPos, 
/*     */ 
/*     */ 
/*     */ 
/*     */         
/*  71 */         Math.min(currentTargetBytes.length, 32));
/*     */ 
/*     */     
/*  74 */     return DataUtils.reverseBytes(targetBytes);
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
/*     */   public static boolean checkTarget(byte[] hash, byte[] target) {
/*  86 */     validateHashAndTarget(hash, target);
/*     */ 
/*     */     
/*  89 */     for (int i = 31; i >= 0; i--) {
/*  90 */       int h = hash[i] & 0xFF;
/*  91 */       int t = target[i] & 0xFF;
/*  92 */       if (h != t) {
/*  93 */         return (h < t);
/*     */       }
/*     */     } 
/*  96 */     return true;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static double targetToDiff(byte[] target) {
/* 107 */     validateTarget(target);
/*     */     
/* 109 */     BigInteger targetBigInt = new BigInteger(1, DataUtils.reverseBytes(target));
/* 110 */     if (targetBigInt.compareTo(BigInteger.ZERO) <= 0) {
/* 111 */       throw new IllegalArgumentException("Invalid target value");
/*     */     }
/*     */     
/* 114 */     BigInteger result = MAX_TARGET.multiply(FACTOR).divide(targetBigInt);
/* 115 */     return result.doubleValue() / 10000.0D;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void validateDifficulty(double diff) {
/* 122 */     if (Double.isNaN(diff) || Double.isInfinite(diff) || diff <= 0.0D) {
/* 123 */       throw new IllegalArgumentException("Invalid difficulty value: " + diff);
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void validateHashAndTarget(byte[] hash, byte[] target) {
/* 132 */     if (hash == null || target == null) {
/* 133 */       throw new IllegalArgumentException("Hash and target cannot be null");
/*     */     }
/* 135 */     if (hash.length != 32 || target.length != 32) {
/* 136 */       throw new IllegalArgumentException("Hash and target must be 32 bytes");
/*     */     }
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private static void validateTarget(byte[] target) {
/* 145 */     if (target == null || target.length != 32) {
/* 146 */       throw new IllegalArgumentException("Target must be 32 bytes");
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   private static class DifficultyCache
/*     */   {
/*     */     private final int capacity;
/*     */     
/*     */     private final CacheEntry[] entries;
/*     */     private int size;
/*     */     
/*     */     private static class CacheEntry
/*     */     {
/*     */       final double difficulty;
/*     */       final byte[] target;
/*     */       
/*     */       CacheEntry(double difficulty, byte[] target) {
/* 164 */         this.difficulty = difficulty;
/* 165 */         this.target = (byte[])target.clone();
/*     */       }
/*     */     }
/*     */     
/*     */     public DifficultyCache(int capacity) {
/* 170 */       this.capacity = capacity;
/* 171 */       this.entries = new CacheEntry[capacity];
/* 172 */       this.size = 0;
/*     */     }
/*     */     
/*     */     public synchronized byte[] get(double difficulty) {
/* 176 */       for (int i = 0; i < this.size; i++) {
/* 177 */         if ((this.entries[i]).difficulty == difficulty) {
/* 178 */           return (byte[])(this.entries[i]).target.clone();
/*     */         }
/*     */       } 
/* 181 */       return null;
/*     */     }
/*     */ 
/*     */     
/*     */     public synchronized void put(double difficulty, byte[] target) {
/* 186 */       for (int i = 0; i < this.size; i++) {
/* 187 */         if ((this.entries[i]).difficulty == difficulty) {
/*     */           return;
/*     */         }
/*     */       } 
/*     */ 
/*     */       
/* 193 */       if (this.size == this.capacity) {
/* 194 */         System.arraycopy(this.entries, 1, this.entries, 0, this.size - 1);
/* 195 */         this.size--;
/*     */       } 
/*     */ 
/*     */       
/* 199 */       this.entries[this.size++] = new CacheEntry(difficulty, target);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public static String targetToHex(byte[] target) {
/* 205 */     validateTarget(target);
/* 206 */     return DataUtils.byteArrayToHexString(DataUtils.reverseBytes(target));
/*     */   }
/*     */   
/*     */   private static class CacheEntry {
/*     */     final double difficulty;
/*     */     final byte[] target;
/*     */     
/*     */     CacheEntry(double difficulty, byte[] target) {
/*     */       this.difficulty = difficulty;
/*     */       this.target = (byte[])target.clone();
/*     */     }
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\stratum\TargetUtils.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */