/*     */ package live.thought.jtminer.data;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ByteArray
/*     */ {
/*     */   protected byte[] content;
/*     */   
/*     */   public ByteArray() {
/*  28 */     this.content = null;
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteArray(byte[] content) {
/*  33 */     if (null == content) {
/*     */       
/*  35 */       this.content = null;
/*     */     }
/*     */     else {
/*     */       
/*  39 */       this.content = new byte[content.length];
/*  40 */       System.arraycopy(content, 0, this.content, 0, content.length);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteArray(int size) {
/*  46 */     this.content = new byte[size];
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteArray append(byte b) {
/*  51 */     if (null != this.content) {
/*     */       
/*  53 */       int newLength = this.content.length + 1;
/*  54 */       byte[] newContent = new byte[newLength];
/*  55 */       System.arraycopy(this.content, 0, newContent, 0, this.content.length);
/*  56 */       newContent[newContent.length - 1] = b;
/*  57 */       this.content = newContent;
/*     */     }
/*     */     else {
/*     */       
/*  61 */       this.content = new byte[1];
/*  62 */       this.content[0] = b;
/*     */     } 
/*  64 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteArray append(byte[] b) {
/*  69 */     if (null != b)
/*     */     {
/*  71 */       if (null != this.content) {
/*     */         
/*  73 */         int oldLength = this.content.length;
/*  74 */         int newLength = oldLength + b.length;
/*  75 */         byte[] newContent = new byte[newLength];
/*  76 */         System.arraycopy(this.content, 0, newContent, 0, this.content.length);
/*  77 */         System.arraycopy(b, 0, newContent, oldLength, b.length);
/*  78 */         this.content = newContent;
/*     */       }
/*     */       else {
/*     */         
/*  82 */         this.content = new byte[b.length];
/*  83 */         System.arraycopy(b, 0, this.content, 0, b.length);
/*     */       } 
/*     */     }
/*  86 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteArray append(byte[] b, int index, int length) {
/*  91 */     if (null != b)
/*     */     {
/*  93 */       if (null != this.content) {
/*     */         
/*  95 */         int oldLength = this.content.length;
/*  96 */         int newLength = oldLength + length;
/*  97 */         byte[] newContent = new byte[newLength];
/*  98 */         System.arraycopy(this.content, 0, newContent, 0, this.content.length);
/*  99 */         System.arraycopy(b, index, newContent, oldLength, length);
/* 100 */         this.content = newContent;
/*     */       }
/*     */       else {
/*     */         
/* 104 */         this.content = new byte[length];
/* 105 */         System.arraycopy(b, index, this.content, 0, length);
/*     */       } 
/*     */     }
/* 108 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   public ByteArray set(int index, byte b) {
/* 113 */     if (null == this.content || index > this.content.length - 1)
/*     */     {
/* 115 */       throw new ArrayIndexOutOfBoundsException(index);
/*     */     }
/* 117 */     this.content[index] = b;
/* 118 */     return this;
/*     */   }
/*     */ 
/*     */   
/*     */   public byte[] get() {
/* 123 */     return this.content;
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\data\ByteArray.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */