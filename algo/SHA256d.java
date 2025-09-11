/*     */ package live.thought.jtminer.algo;
/*     */ 
/*     */ import java.nio.charset.Charset;
/*     */ import org.bouncycastle.crypto.digests.SHA256Digest;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class SHA256d
/*     */ {
/*     */   private SHA256Digest hasher;
/*     */   private byte[] digest;
/*     */   private int truncate_to;
/*     */   
/*     */   public SHA256d(int truncate_to) {
/*  57 */     this.hasher = new SHA256Digest();
/*  58 */     this.truncate_to = (truncate_to > this.hasher.getDigestSize()) ? this.hasher.getDigestSize() : truncate_to;
/*  59 */     this.digest = new byte[truncate_to];
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public SHA256d() {
/*  68 */     this.hasher = new SHA256Digest();
/*  69 */     this.digest = new byte[this.hasher.getDigestSize()];
/*     */     
/*  71 */     this.truncate_to = this.hasher.getDigestSize();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void update(String data) {
/*  82 */     update(data.getBytes(Charset.forName("UTF-8")));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void update(byte[] data) {
/*  93 */     this.hasher.update(data, 0, data.length);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public byte[] doubleDigest() {
/* 103 */     byte[] h1 = getDigest();
/* 104 */     update(h1);
/* 105 */     byte[] h2 = getDigest();
/* 106 */     System.arraycopy(h2, 0, this.digest, 0, this.truncate_to);
/* 107 */     return this.digest;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public byte[] digest() {
/* 117 */     byte[] h1 = getDigest();
/* 118 */     System.arraycopy(h1, 0, this.digest, 0, this.truncate_to);
/* 119 */     return this.digest;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private byte[] getDigest() {
/* 129 */     byte[] out = new byte[this.hasher.getDigestSize()];
/* 130 */     this.hasher.doFinal(out, 0);
/* 131 */     return out;
/*     */   }
/*     */   
/*     */   public void cleanup() {
/* 135 */     if (this.hasher != null) {
/* 136 */       this.hasher.reset();
/* 137 */       this.digest = null;
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\algo\SHA256d.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */