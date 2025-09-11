/*     */ package live.thought.jtminer.algo;
/*     */ 
/*     */ import java.util.HashSet;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class CuckooSolve
/*     */ {
/*     */   public static final int MAXPATHLEN = 4096;
/*     */   Cuckoo graph;
/*     */   int easiness;
/*     */   int[] cuckoo;
/*     */   int nthreads;
/*     */   private HashSet<Edge> solutionCycle;
/*     */   private int[] solutionArray;
/*     */   private static final int MAXPATH_CHECK = 4092;
/*     */   
/*     */   public CuckooSolve(byte[] hdr, int en, int nt) {
/*  21 */     this.graph = new Cuckoo(hdr);
/*  22 */     this.easiness = en;
/*  23 */     this.cuckoo = new int[16777217];
/*  24 */     assert this.cuckoo != null;
/*  25 */     this.nthreads = nt;
/*     */   }
/*     */ 
/*     */   
/*     */   public int path(int u, int[] us) {
/*  30 */     int nu = 0;
/*     */     
/*  32 */     while (u != 0) {
/*  33 */       us[++nu] = u;
/*  34 */       u = this.cuckoo[u];
/*     */       
/*  36 */       if (nu >= 4092) {
/*  37 */         int i = nu;
/*  38 */         while (i-- != 0 && us[i] != u);
/*  39 */         throw new RuntimeException((i < 0) ? "maximum path length exceeded" : ("illegal " + nu - i + "-cycle"));
/*     */       } 
/*     */     } 
/*     */     
/*  43 */     return nu;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized int[] solution(int[] us, int nu, int[] vs, int nv) {
/*  83 */     if (this.solutionCycle == null) {
/*  84 */       this.solutionCycle = new HashSet<>(84);
/*  85 */       this.solutionArray = new int[42];
/*     */     } else {
/*  87 */       this.solutionCycle.clear();
/*     */     } 
/*     */ 
/*     */     
/*  91 */     this.solutionCycle.add(new Edge(us[0], vs[0] - 8388608));
/*     */     
/*  93 */     int nuOriginal = nu;
/*  94 */     while (nu-- != 0) {
/*  95 */       this.solutionCycle.add(new Edge(us[nu + 1 & 0xFFFFFFFE], us[nu | 0x1] - 8388608));
/*     */     }
/*  97 */     int nvOriginal = nv;
/*  98 */     while (nv-- != 0) {
/*  99 */       this.solutionCycle.add(new Edge(vs[nv | 0x1], vs[nv + 1 & 0xFFFFFFFE] - 8388608));
/*     */     }
/*     */     
/* 102 */     Edge e = null;
/* 103 */     int n = 0;
/*     */     
/* 105 */     for (int nonce = 0; nonce < this.easiness && n < 42; nonce++) {
/* 106 */       e = this.graph.sipedge(nonce);
/* 107 */       if (this.solutionCycle.contains(e)) {
/* 108 */         this.solutionArray[n++] = nonce;
/* 109 */         this.solutionCycle.remove(e);
/*     */       } 
/*     */     } 
/*     */     
/* 113 */     return (n == 42) ? this.solutionArray : null;
/*     */   }
/*     */ 
/*     */   
/*     */   public int getEasiness() {
/* 118 */     return this.easiness;
/*     */   }
/*     */ 
/*     */   
/*     */   public int[] getCuckoo() {
/* 123 */     return this.cuckoo;
/*     */   }
/*     */ 
/*     */   
/*     */   public int getNthreads() {
/* 128 */     return this.nthreads;
/*     */   }
/*     */ 
/*     */   
/*     */   public Cuckoo getGraph() {
/* 133 */     return this.graph;
/*     */   }
/*     */ 
/*     */   
/*     */   public void cleanup() {
/* 138 */     this.graph = null;
/* 139 */     this.cuckoo = null;
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\algo\CuckooSolve.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */