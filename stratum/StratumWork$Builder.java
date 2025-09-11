/*     */ package live.thought.jtminer.stratum;
/*     */ 
/*     */ import java.util.List;
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
/*     */ public class Builder
/*     */ {
/*     */   private String jobId;
/*     */   private String prevHash;
/*     */   private String coinbase1;
/*     */   private String coinbase2;
/*     */   private List<String> merkleBranch;
/*     */   private String version;
/*     */   private String nbits;
/*     */   private String ntime;
/*     */   private boolean cleanJobs;
/*     */   
/*     */   public Builder jobId(String jobId) {
/* 351 */     this.jobId = jobId;
/* 352 */     return this;
/*     */   }
/*     */   
/*     */   public Builder prevHash(String prevHash) {
/* 356 */     this.prevHash = prevHash;
/* 357 */     return this;
/*     */   }
/*     */   
/*     */   public Builder coinbase1(String coinbase1) {
/* 361 */     this.coinbase1 = coinbase1;
/* 362 */     return this;
/*     */   }
/*     */   
/*     */   public Builder coinbase2(String coinbase2) {
/* 366 */     this.coinbase2 = coinbase2;
/* 367 */     return this;
/*     */   }
/*     */   
/*     */   public Builder merkleBranch(List<String> merkleBranch) {
/* 371 */     this.merkleBranch = merkleBranch;
/* 372 */     return this;
/*     */   }
/*     */   
/*     */   public Builder version(String version) {
/* 376 */     this.version = version;
/* 377 */     return this;
/*     */   }
/*     */   
/*     */   public Builder nbits(String nbits) {
/* 381 */     this.nbits = nbits;
/* 382 */     return this;
/*     */   }
/*     */   
/*     */   public Builder ntime(String ntime) {
/* 386 */     this.ntime = ntime;
/* 387 */     return this;
/*     */   }
/*     */   
/*     */   public Builder cleanJobs(boolean cleanJobs) {
/* 391 */     this.cleanJobs = cleanJobs;
/* 392 */     return this;
/*     */   }
/*     */   
/*     */   public StratumWork build() {
/* 396 */     validate();
/* 397 */     return new StratumWork(this);
/*     */   }
/*     */   
/*     */   private void validate() {
/* 401 */     if (this.jobId == null || this.prevHash == null || this.coinbase1 == null || this.coinbase2 == null || this.merkleBranch == null || this.version == null || this.nbits == null || this.ntime == null)
/*     */     {
/*     */       
/* 404 */       throw new IllegalStateException("All fields must be non-null");
/*     */     }
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\stratum\StratumWork$Builder.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */