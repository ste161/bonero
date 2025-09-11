/*     */ package live.thought.jtminer.stratum;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ class DifficultyCache
/*     */ {
/*     */   private final int capacity;
/*     */   private final CacheEntry[] entries;
/*     */   private int size;
/*     */   
/*     */   private static class CacheEntry
/*     */   {
/*     */     final double difficulty;
/*     */     final byte[] target;
/*     */     
/*     */     CacheEntry(double difficulty, byte[] target) {
/* 164 */       this.difficulty = difficulty;
/* 165 */       this.target = (byte[])target.clone();
/*     */     }
/*     */   }
/*     */   
/*     */   public DifficultyCache(int capacity) {
/* 170 */     this.capacity = capacity;
/* 171 */     this.entries = new CacheEntry[capacity];
/* 172 */     this.size = 0;
/*     */   }
/*     */   
/*     */   public synchronized byte[] get(double difficulty) {
/* 176 */     for (int i = 0; i < this.size; i++) {
/* 177 */       if ((this.entries[i]).difficulty == difficulty) {
/* 178 */         return (byte[])(this.entries[i]).target.clone();
/*     */       }
/*     */     } 
/* 181 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public synchronized void put(double difficulty, byte[] target) {
/* 186 */     for (int i = 0; i < this.size; i++) {
/* 187 */       if ((this.entries[i]).difficulty == difficulty) {
/*     */         return;
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 193 */     if (this.size == this.capacity) {
/* 194 */       System.arraycopy(this.entries, 1, this.entries, 0, this.size - 1);
/* 195 */       this.size--;
/*     */     } 
/*     */ 
/*     */     
/* 199 */     this.entries[this.size++] = new CacheEntry(difficulty, target);
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\stratum\TargetUtils$DifficultyCache.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */