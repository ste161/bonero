/*     */ package live.thought.jtminer.algo;
/*     */ 
/*     */ import live.thought.jtminer.util.Console;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class Cuckoo
/*     */ {
/*     */   public static final int EDGEBITS = 23;
/*     */   public static final int NEDGES = 8388608;
/*     */   public static final int NODEBITS = 24;
/*     */   public static final int NNODES = 16777216;
/*     */   public static final int EDGEMASK = 8388607;
/*     */   public static final int PROOFSIZE = 42;
/*  38 */   long[] k = new long[4];
/*  39 */   SHA256d hasher = new SHA256d(32);
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static long u8(byte b) {
/*  45 */     return b & 0xFFL;
/*     */   }
/*     */ 
/*     */   
/*     */   public static long u8to64(byte[] p, int i) {
/*  50 */     return u8(p[i]) | u8(p[i + 1]) << 8L | u8(p[i + 2]) << 16L | u8(p[i + 3]) << 24L | u8(p[i + 4]) << 32L | u8(p[i + 5]) << 40L | 
/*  51 */       u8(p[i + 6]) << 48L | u8(p[i + 7]) << 56L;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Cuckoo(byte[] header) {
/*  58 */     this.hasher.update(header);
/*  59 */     byte[] hdrkey = this.hasher.digest();
/*     */     
/*  61 */     this.k[0] = u8to64(hdrkey, 0);
/*  62 */     this.k[1] = u8to64(hdrkey, 8);
/*  63 */     this.k[2] = u8to64(hdrkey, 16);
/*  64 */     this.k[3] = u8to64(hdrkey, 24);
/*  65 */     Console.debug("k0: " + this.k[0], 2);
/*  66 */     Console.debug("k1: " + this.k[1], 2);
/*  67 */     Console.debug("k2: " + this.k[2], 2);
/*  68 */     Console.debug("k3: " + this.k[3], 2);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public long siphash242(int nonce) {
/*  75 */     long k0 = this.k[0];
/*  76 */     long k1 = this.k[1];
/*  77 */     long k2 = this.k[2];
/*  78 */     long k3 = this.k[3];
/*     */     
/*  80 */     long v0 = k0;
/*  81 */     long v1 = k1;
/*  82 */     long v2 = k2;
/*  83 */     long v3 = k3 ^ nonce;
/*     */     
/*     */     int r;
/*  86 */     for (r = 0; r < 2; r++) {
/*  87 */       v0 += v1; v2 += v3; v1 = v1 << 13L | v1 >>> 51L; v3 = v3 << 16L | v3 >>> 48L;
/*  88 */       v1 ^= v0; v3 ^= v2; v0 = v0 << 32L | v0 >>> 32L;
/*  89 */       v2 += v1; v0 += v3; v1 = v1 << 17L | v1 >>> 47L; v3 = v3 << 21L | v3 >>> 43L;
/*  90 */       v1 ^= v2; v3 ^= v0; v2 = v2 << 32L | v2 >>> 32L;
/*     */     } 
/*     */     
/*  93 */     v0 ^= nonce;
/*  94 */     v2 ^= 0xFFL;
/*     */ 
/*     */     
/*  97 */     for (r = 0; r < 4; r++) {
/*  98 */       v0 += v1; v2 += v3; v1 = v1 << 13L | v1 >>> 51L; v3 = v3 << 16L | v3 >>> 48L;
/*  99 */       v1 ^= v0; v3 ^= v2; v0 = v0 << 32L | v0 >>> 32L;
/* 100 */       v2 += v1; v0 += v3; v1 = v1 << 17L | v1 >>> 47L; v3 = v3 << 21L | v3 >>> 43L;
/* 101 */       v1 ^= v2; v3 ^= v0; v2 = v2 << 32L | v2 >>> 32L;
/*     */     } 
/*     */     
/* 104 */     return v0 ^ v1 ^ v2 ^ v3;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public long siphash24(int nonce) {
/* 110 */     long v0 = this.k[0];
/* 111 */     long v1 = this.k[1];
/* 112 */     long v2 = this.k[2];
/* 113 */     long v3 = this.k[3] ^ nonce;
/*     */     
/* 115 */     v0 += v1;
/* 116 */     v2 += v3;
/* 117 */     v1 = v1 << 13L | v1 >>> 51L;
/* 118 */     v3 = v3 << 16L | v3 >>> 48L;
/* 119 */     v1 ^= v0;
/* 120 */     v3 ^= v2;
/* 121 */     v0 = v0 << 32L | v0 >>> 32L;
/* 122 */     v2 += v1;
/* 123 */     v0 += v3;
/* 124 */     v1 = v1 << 17L | v1 >>> 47L;
/* 125 */     v3 = v3 << 21L | v3 >>> 43L;
/* 126 */     v1 ^= v2;
/* 127 */     v3 ^= v0;
/* 128 */     v2 = v2 << 32L | v2 >>> 32L;
/*     */     
/* 130 */     v0 += v1;
/* 131 */     v2 += v3;
/* 132 */     v1 = v1 << 13L | v1 >>> 51L;
/* 133 */     v3 = v3 << 16L | v3 >>> 48L;
/* 134 */     v1 ^= v0;
/* 135 */     v3 ^= v2;
/* 136 */     v0 = v0 << 32L | v0 >>> 32L;
/* 137 */     v2 += v1;
/* 138 */     v0 += v3;
/* 139 */     v1 = v1 << 17L | v1 >>> 47L;
/* 140 */     v3 = v3 << 21L | v3 >>> 43L;
/* 141 */     v1 ^= v2;
/* 142 */     v3 ^= v0;
/* 143 */     v2 = v2 << 32L | v2 >>> 32L;
/*     */     
/* 145 */     v0 ^= nonce;
/* 146 */     v2 ^= 0xFFL;
/*     */     
/* 148 */     v0 += v1;
/* 149 */     v2 += v3;
/* 150 */     v1 = v1 << 13L | v1 >>> 51L;
/* 151 */     v3 = v3 << 16L | v3 >>> 48L;
/* 152 */     v1 ^= v0;
/* 153 */     v3 ^= v2;
/* 154 */     v0 = v0 << 32L | v0 >>> 32L;
/* 155 */     v2 += v1;
/* 156 */     v0 += v3;
/* 157 */     v1 = v1 << 17L | v1 >>> 47L;
/* 158 */     v3 = v3 << 21L | v3 >>> 43L;
/* 159 */     v1 ^= v2;
/* 160 */     v3 ^= v0;
/* 161 */     v2 = v2 << 32L | v2 >>> 32L;
/*     */     
/* 163 */     v0 += v1;
/* 164 */     v2 += v3;
/* 165 */     v1 = v1 << 13L | v1 >>> 51L;
/* 166 */     v3 = v3 << 16L | v3 >>> 48L;
/* 167 */     v1 ^= v0;
/* 168 */     v3 ^= v2;
/* 169 */     v0 = v0 << 32L | v0 >>> 32L;
/* 170 */     v2 += v1;
/* 171 */     v0 += v3;
/* 172 */     v1 = v1 << 17L | v1 >>> 47L;
/* 173 */     v3 = v3 << 21L | v3 >>> 43L;
/* 174 */     v1 ^= v2;
/* 175 */     v3 ^= v0;
/* 176 */     v2 = v2 << 32L | v2 >>> 32L;
/*     */     
/* 178 */     v0 += v1;
/* 179 */     v2 += v3;
/* 180 */     v1 = v1 << 13L | v1 >>> 51L;
/* 181 */     v3 = v3 << 16L | v3 >>> 48L;
/* 182 */     v1 ^= v0;
/* 183 */     v3 ^= v2;
/* 184 */     v0 = v0 << 32L | v0 >>> 32L;
/* 185 */     v2 += v1;
/* 186 */     v0 += v3;
/* 187 */     v1 = v1 << 17L | v1 >>> 47L;
/* 188 */     v3 = v3 << 21L | v3 >>> 43L;
/* 189 */     v1 ^= v2;
/* 190 */     v3 ^= v0;
/* 191 */     v2 = v2 << 32L | v2 >>> 32L;
/*     */     
/* 193 */     v0 += v1;
/* 194 */     v2 += v3;
/* 195 */     v1 = v1 << 13L | v1 >>> 51L;
/* 196 */     v3 = v3 << 16L | v3 >>> 48L;
/* 197 */     v1 ^= v0;
/* 198 */     v3 ^= v2;
/* 199 */     v0 = v0 << 32L | v0 >>> 32L;
/* 200 */     v2 += v1;
/* 201 */     v0 += v3;
/* 202 */     v1 = v1 << 17L | v1 >>> 47L;
/* 203 */     v3 = v3 << 21L | v3 >>> 43L;
/* 204 */     v1 ^= v2;
/* 205 */     v3 ^= v0;
/* 206 */     v2 = v2 << 32L | v2 >>> 32L;
/*     */     
/* 208 */     return v0 ^ v1 ^ v2 ^ v3;
/*     */   }
/*     */ 
/*     */   
/*     */   public long siphash241(int nonce) {
/* 213 */     long k0 = this.k[0];
/* 214 */     long k1 = this.k[1];
/* 215 */     long k2 = this.k[2];
/* 216 */     long k3 = this.k[3];
/*     */ 
/*     */     
/* 219 */     long v0 = k0;
/* 220 */     long v1 = k1;
/* 221 */     long v2 = k2;
/* 222 */     long v3 = k3 ^ nonce & 0xFFFFFFFFL;
/*     */     
/*     */     int i;
/*     */     
/* 226 */     for (i = 0; i < 2; i++) {
/*     */       
/* 228 */       v0 = v0 + v1 & 0xFFFFFFFFFFFFFFFFL;
/* 229 */       v2 = v2 + v3 & 0xFFFFFFFFFFFFFFFFL;
/*     */ 
/*     */ 
/*     */       
/* 233 */       long v1Left = v1 << 13L;
/* 234 */       long v1Right = v1 >>> 51L;
/* 235 */       v1 = v1Left | v1Right;
/*     */ 
/*     */       
/* 238 */       long v3Left = v3 << 16L;
/* 239 */       long v3Right = v3 >>> 48L;
/* 240 */       v3 = v3Left | v3Right;
/*     */ 
/*     */       
/* 243 */       v1 ^= v0;
/* 244 */       v3 ^= v2;
/*     */ 
/*     */       
/* 247 */       v0 = (v0 << 32L | v0 >>> 32L) & 0xFFFFFFFFFFFFFFFFL;
/*     */ 
/*     */       
/* 250 */       v2 = v2 + v1 & 0xFFFFFFFFFFFFFFFFL;
/* 251 */       v0 = v0 + v3 & 0xFFFFFFFFFFFFFFFFL;
/*     */ 
/*     */ 
/*     */       
/* 255 */       v1Left = v1 << 17L;
/* 256 */       v1Right = v1 >>> 47L;
/* 257 */       v1 = v1Left | v1Right;
/*     */ 
/*     */       
/* 260 */       v3Left = v3 << 21L;
/* 261 */       v3Right = v3 >>> 43L;
/* 262 */       v3 = v3Left | v3Right;
/*     */ 
/*     */       
/* 265 */       v1 ^= v2;
/* 266 */       v3 ^= v0;
/*     */ 
/*     */       
/* 269 */       v2 = (v2 << 32L | v2 >>> 32L) & 0xFFFFFFFFFFFFFFFFL;
/*     */     } 
/*     */ 
/*     */     
/* 273 */     v0 ^= nonce & 0xFFFFFFFFL;
/* 274 */     v2 ^= 0xFFL;
/*     */ 
/*     */     
/* 277 */     for (i = 0; i < 4; i++) {
/*     */       
/* 279 */       v0 = v0 + v1 & 0xFFFFFFFFFFFFFFFFL;
/* 280 */       v2 = v2 + v3 & 0xFFFFFFFFFFFFFFFFL;
/*     */ 
/*     */       
/* 283 */       long v1Left = v1 << 13L;
/* 284 */       long v1Right = v1 >>> 51L;
/* 285 */       v1 = v1Left | v1Right;
/*     */       
/* 287 */       long v3Left = v3 << 16L;
/* 288 */       long v3Right = v3 >>> 48L;
/* 289 */       v3 = v3Left | v3Right;
/*     */ 
/*     */       
/* 292 */       v1 ^= v0;
/* 293 */       v3 ^= v2;
/*     */ 
/*     */       
/* 296 */       v0 = (v0 << 32L | v0 >>> 32L) & 0xFFFFFFFFFFFFFFFFL;
/*     */ 
/*     */       
/* 299 */       v2 = v2 + v1 & 0xFFFFFFFFFFFFFFFFL;
/* 300 */       v0 = v0 + v3 & 0xFFFFFFFFFFFFFFFFL;
/*     */ 
/*     */       
/* 303 */       v1Left = v1 << 17L;
/* 304 */       v1Right = v1 >>> 47L;
/* 305 */       v1 = v1Left | v1Right;
/*     */       
/* 307 */       v3Left = v3 << 21L;
/* 308 */       v3Right = v3 >>> 43L;
/* 309 */       v3 = v3Left | v3Right;
/*     */ 
/*     */       
/* 312 */       v1 ^= v2;
/* 313 */       v3 ^= v0;
/*     */ 
/*     */       
/* 316 */       v2 = (v2 << 32L | v2 >>> 32L) & 0xFFFFFFFFFFFFFFFFL;
/*     */     } 
/*     */ 
/*     */     
/* 320 */     return (v0 ^ v1 ^ v2 ^ v3) & 0xFFFFFFFFFFFFFFFFL;
/*     */   }
/*     */ 
/*     */   
/*     */   public int sipnode(int nonce, int uorv) {
/* 325 */     return (int)siphash24(nonce * 2 + uorv) & 0x7FFFFF;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public Edge sipedge(int nonce) {
/* 331 */     return new Edge(sipnode(nonce, 0), sipnode(nonce, 1));
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public Boolean verify(int[] nonces, int easiness) {
/* 393 */     int[] us = new int[42], vs = new int[42];
/* 394 */     int i = 0;
/* 395 */     int xor0 = 0;
/* 396 */     int xor1 = 0; int n;
/* 397 */     for (n = 0; n < 42; n++) {
/*     */       
/* 399 */       if (nonces[n] >= easiness || (n != 0 && nonces[n] <= nonces[n - 1]))
/* 400 */         return Boolean.valueOf(false); 
/* 401 */       us[n] = sipnode(nonces[n], 0);
/* 402 */       vs[n] = sipnode(nonces[n], 1);
/* 403 */       xor0 ^= us[n];
/* 404 */       xor1 ^= vs[n];
/*     */     } 
/* 406 */     if (xor0 > 0 || xor1 > 0) {
/* 407 */       return Boolean.valueOf(false);
/*     */     }
/*     */     while (true) {
/* 410 */       int j = i; int k;
/* 411 */       for (k = 0; k < 42; k++) {
/* 412 */         if (k != i && vs[k] == vs[i]) {
/*     */           
/* 414 */           if (j != i)
/* 415 */             return Boolean.valueOf(false); 
/* 416 */           j = k;
/*     */         } 
/* 418 */       }  if (j == i)
/* 419 */         return Boolean.valueOf(false); 
/* 420 */       i = j;
/* 421 */       for (k = 0; k < 42; k++) {
/* 422 */         if (k != j && us[k] == us[j]) {
/*     */           
/* 424 */           if (i != j)
/* 425 */             return Boolean.valueOf(false); 
/* 426 */           i = k;
/*     */         } 
/* 428 */       }  if (i == j)
/* 429 */         return Boolean.valueOf(false); 
/* 430 */       n -= 2;
/*     */       
/* 432 */       if (i == 0)
/* 433 */         return Boolean.valueOf((n == 0)); 
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\algo\Cuckoo.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */