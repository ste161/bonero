/*     */ package live.thought.jtminer.stratum;
/*     */ 
/*     */ import live.thought.jtminer.StratumMiner;
/*     */ import live.thought.jtminer.algo.Cuckoo;
/*     */ import live.thought.jtminer.algo.CuckooSolve;
/*     */ import live.thought.jtminer.algo.SHA256d;
/*     */ import live.thought.jtminer.util.Console;
/*     */ 
/*     */ public class StratumSolver implements Runnable {
/*     */   private final int index;
/*     */   private CuckooSolve solve;
/*     */   private final StratumClient client;
/*     */   private volatile StratumWork currentWork;
/*     */   private final StratumMiner miner;
/*  15 */   private final SHA256d hasher = new SHA256d(32);
/*     */   
/*     */   private volatile boolean interrupted = false;
/*     */   
/*     */   private volatile boolean stopped = false;
/*     */   private Thread runningThread;
/*     */   private final int[] us;
/*     */   private final int[] vs;
/*     */   private final StringBuilder solutionBuilder;
/*     */   
/*     */   public StratumSolver(StratumClient client, StratumWork work, int index, CuckooSolve solve, StratumMiner miner) {
/*  26 */     this.solve = solve;
/*  27 */     this.index = index;
/*  28 */     this.currentWork = work;
/*  29 */     this.client = client;
/*  30 */     this.miner = miner;
/*     */ 
/*     */     
/*  33 */     this.us = new int[4096];
/*  34 */     this.vs = new int[4096];
/*  35 */     this.solutionBuilder = new StringBuilder(336);
/*     */   }
/*     */   
/*     */   public void stop() {
/*  39 */     this.interrupted = true;
/*  40 */     if (this.runningThread != null) {
/*  41 */       this.runningThread.interrupt();
/*     */     }
/*     */   }
/*     */   
/*     */   public void forceStop() {
/*  46 */     this.interrupted = true;
/*  47 */     if (this.runningThread != null) {
/*  48 */       this.runningThread.interrupt();
/*     */     }
/*     */   }
/*     */   
/*     */   public boolean isStopped() {
/*  53 */     return this.stopped;
/*     */   }
/*     */   
/*     */   private void cleanup() {
/*  57 */     if (this.solve != null) {
/*  58 */       this.solve.cleanup();
/*  59 */       this.solve = null;
/*     */     } 
/*  61 */     if (this.hasher != null) {
/*  62 */       this.hasher.cleanup();
/*     */     }
/*  64 */     this.runningThread = null;
/*     */   }
/*     */ 
/*     */   
/*     */   public void run() {
/*  69 */     this.runningThread = Thread.currentThread();
/*     */     
/*  71 */     int[] cuckoo = this.solve.getCuckoo();
/*     */ 
/*     */     
/*     */     try {
/*  75 */       int threadIndex = this.index;
/*  76 */       int threadCount = this.solve.getNthreads();
/*  77 */       int maxNonce = this.solve.getEasiness();
/*  78 */       Cuckoo graph = this.solve.getGraph();
/*     */       int nonce;
/*  80 */       for (nonce = threadIndex; !this.interrupted && nonce < maxNonce; nonce += threadCount) {
/*     */         
/*  82 */         if (Thread.currentThread().isInterrupted()) {
/*  83 */           this.interrupted = true;
/*     */           
/*     */           break;
/*     */         } 
/*     */         
/*  88 */         int u0 = graph.sipnode(nonce, 0);
/*  89 */         int v0 = graph.sipnode(nonce, 1) + 8388608;
/*     */         
/*  91 */         this.us[0] = u0;
/*  92 */         this.vs[0] = v0;
/*     */         
/*  94 */         int u = cuckoo[u0];
/*  95 */         int v = cuckoo[v0];
/*     */         
/*  97 */         if (u != v0 && v != u0) {
/*     */ 
/*     */           
/* 100 */           int nu = this.solve.path(u, this.us), nv = this.solve.path(v, this.vs);
/* 101 */           if (this.us[nu] == this.vs[nv]) {
/* 102 */             if (Thread.currentThread().isInterrupted()) {
/* 103 */               this.interrupted = true;
/*     */               break;
/*     */             } 
/* 106 */             int min = (nu < nv) ? nu : nv;
/* 107 */             for (nu -= min, nv -= min; this.us[nu] != this.vs[nv]; ) { nu++; nv++; }
/*     */             
/* 109 */             int len = nu + nv + 1;
/* 110 */             this.miner.cycleCount.incrementAndGet();
/* 111 */             if (len == 42) {
/* 112 */               int[] soln = this.solve.solution(this.us, nu, this.vs, nv);
/* 113 */               if (null != soln) {
/* 114 */                 this.miner.solutionCount.incrementAndGet();
/* 115 */                 if (graph.verify(soln, 16777216).booleanValue()) {
/*     */                   try {
/* 117 */                     if (this.currentWork.meetsTarget(soln)) {
/* 118 */                       Console.debug("Trying to submit solution", 2);
/*     */                       
/* 120 */                       this.solutionBuilder.setLength(0);
/* 121 */                       for (int n : soln) {
/* 122 */                         this.solutionBuilder.append(n).append(',');
/*     */                       }
/* 124 */                       if (this.solutionBuilder.length() > 0) {
/* 125 */                         this.solutionBuilder.setLength(this.solutionBuilder.length() - 1);
/*     */                       }
/*     */                       
/* 128 */                       this.client.submitWork(this.currentWork, this.solutionBuilder
/*     */                           
/* 130 */                           .toString());
/*     */                     }
/*     */                   
/* 133 */                   } catch (StratumException e) {
/* 134 */                     Console.debug("Error submitting solution: " + e.getMessage(), 2);
/*     */                     break;
/*     */                   } 
/*     */                 } else {
/* 138 */                   this.miner.errorCount.incrementAndGet();
/*     */                 }
/*     */               
/*     */               }
/*     */             
/*     */             } 
/*     */           } else {
/*     */             
/* 146 */             if (nu < nv) {
/* 147 */               while (nu-- != 0)
/* 148 */                 cuckoo[this.us[nu + 1]] = this.us[nu]; 
/* 149 */               cuckoo[this.us[0]] = this.vs[0];
/*     */             } else {
/* 151 */               while (nv-- != 0)
/* 152 */                 cuckoo[this.vs[nv + 1]] = this.vs[nv]; 
/* 153 */               cuckoo[this.vs[0]] = this.us[0];
/*     */             } 
/*     */ 
/*     */             
/* 157 */             if (nonce % 100 == 0 && Thread.currentThread().isInterrupted())
/* 158 */             { this.interrupted = true; break; } 
/*     */           } 
/*     */         } 
/*     */       } 
/* 162 */     } catch (RuntimeException re) {
/* 163 */       Console.debug("Illegal cycle detected", 2);
/*     */     } finally {
/* 165 */       cleanup();
/* 166 */       Console.debug("Solver " + this.index + " stopped", 2);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\stratum\StratumSolver.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */