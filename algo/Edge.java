/*    */ package live.thought.jtminer.algo;
/*    */ 
/*    */ 
/*    */ class Edge
/*    */ {
/*    */   final int u;
/*    */   final int v;
/*    */   
/*    */   public Edge(int x, int y) {
/* 10 */     this.u = x;
/* 11 */     this.v = y;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public int hashCode() {
/* 17 */     return this.u * 31 + this.v;
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean equals(Object o) {
/* 22 */     if (this == o) return true; 
/* 23 */     if (!(o instanceof Edge)) return false; 
/* 24 */     Edge f = (Edge)o;
/* 25 */     return (this.u == f.u && this.v == f.v);
/*    */   }
/*    */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\algo\Edge.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */