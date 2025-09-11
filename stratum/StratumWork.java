/*     */ package live.thought.jtminer.stratum;
/*     */ 
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.math.BigInteger;
/*     */ import java.security.SecureRandom;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.atomic.AtomicReference;
/*     */ import java.util.concurrent.locks.ReentrantLock;
/*     */ import live.thought.jtminer.algo.SHA256d;
/*     */ import live.thought.jtminer.data.DataUtils;
/*     */ import live.thought.jtminer.util.Console;
/*     */ import org.json.JSONArray;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public final class StratumWork
/*     */ {
/*     */   private final String jobId;
/*     */   private final String prevHash;
/*     */   private final String coinbase1;
/*     */   private final String coinbase2;
/*     */   private final List<String> merkleBranch;
/*     */   private final String version;
/*     */   public String ntime;
/*     */   private final String nbits;
/*     */   private final boolean cleanJobs;
/*  29 */   private final ReentrantLock stateLock = new ReentrantLock();
/*     */   
/*     */   private final AtomicReference<byte[]> xnonce2;
/*     */   
/*     */   private volatile byte[] target;
/*     */   private volatile byte[] data;
/*     */   private String extraNonce1;
/*     */   private int extraNonce2Size;
/*     */   public int nonce;
/*  38 */   private final ReentrantLock cacheLock = new ReentrantLock();
/*     */   
/*     */   private byte[] cachedMerkleRoot;
/*     */   
/*     */   private static final int HEADER_SIZE = 80;
/*     */   private static final int VERSION_OFFSET = 0;
/*     */   private static final int PREV_HASH_OFFSET = 4;
/*     */   private static final int MERKLE_ROOT_OFFSET = 36;
/*     */   private static final int TIME_OFFSET = 68;
/*     */   private static final int BITS_OFFSET = 72;
/*     */   private static final int NONCE_OFFSET = 76;
/*     */   private static final int SOLUTION_LENGTH = 42;
/*     */   
/*     */   private StratumWork(Builder builder) {
/*  52 */     this.jobId = builder.jobId;
/*  53 */     this.prevHash = builder.prevHash;
/*  54 */     this.coinbase1 = builder.coinbase1;
/*  55 */     this.coinbase2 = builder.coinbase2;
/*  56 */     this.merkleBranch = new ArrayList<>(builder.merkleBranch);
/*  57 */     this.version = builder.version;
/*  58 */     this.nbits = builder.nbits;
/*  59 */     this.ntime = builder.ntime;
/*  60 */     this.cleanJobs = builder.cleanJobs;
/*  61 */     this.xnonce2 = (AtomicReference)new AtomicReference<>();
/*  62 */     this.nonce = 0;
/*     */   }
/*     */   
/*     */   public static Builder builder() {
/*  66 */     return new Builder();
/*     */   }
/*     */   
/*     */   public static StratumWork fromNotification(JSONArray params) {
/*  70 */     if (params == null || params.length() < 9) {
/*  71 */       throw new IllegalArgumentException("Invalid stratum notification parameters");
/*     */     }
/*     */     
/*  74 */     return builder()
/*  75 */       .jobId(params.getString(0))
/*  76 */       .prevHash(params.getString(1))
/*  77 */       .coinbase1(params.getString(2))
/*  78 */       .coinbase2(params.getString(3))
/*  79 */       .merkleBranch(parseStringArray(params.getJSONArray(4)))
/*  80 */       .version(params.getString(5))
/*  81 */       .nbits(params.getString(6))
/*  82 */       .ntime(params.getString(7))
/*  83 */       .cleanJobs(params.getBoolean(8))
/*  84 */       .build();
/*     */   }
/*     */   
/*     */   public void prepareWork(String extraNonce1, int extraNonce2Size) {
/*  88 */     if (extraNonce1 == null || extraNonce2Size <= 0) {
/*  89 */       throw new IllegalArgumentException("Invalid extra nonce parameters");
/*     */     }
/*     */     
/*  92 */     this.stateLock.lock();
/*     */     try {
/*  94 */       this.extraNonce1 = extraNonce1;
/*  95 */       this.extraNonce2Size = extraNonce2Size;
/*  96 */       this.xnonce2.set(new byte[extraNonce2Size]);
/*  97 */       invalidateCache();
/*  98 */       updateBlockHeader();
/*     */     } finally {
/* 100 */       this.stateLock.unlock();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void updateBlockHeader() {
/* 105 */     byte[] merkleRoot = calculateMerkleRoot();
/* 106 */     byte[] newData = new byte[80];
/*     */ 
/*     */     
/* 109 */     byte[] versionBytes = DataUtils.hexStringToByteArray(this.version);
/* 110 */     byte[] reversedVersion = DataUtils.reverseBytes(versionBytes);
/* 111 */     System.arraycopy(reversedVersion, 0, newData, 0, 4);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 120 */     byte[] prevHashBytes = DataUtils.hexStringToByteArray(this.prevHash);
/* 121 */     byte[] reversedPrevHash = DataUtils.reverseBytes(prevHashBytes);
/* 122 */     System.arraycopy(reversedPrevHash, 0, newData, 4, 32);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 131 */     System.arraycopy(merkleRoot, 0, newData, 36, 32);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 140 */     byte[] ntimeBytes = DataUtils.hexStringToByteArray(this.ntime);
/* 141 */     byte[] reversedNtime = DataUtils.reverseBytes(ntimeBytes);
/* 142 */     System.arraycopy(reversedNtime, 0, newData, 68, 4);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 151 */     byte[] nbitsBytes = DataUtils.hexStringToByteArray(this.nbits);
/* 152 */     byte[] reversedNbits = DataUtils.reverseBytes(nbitsBytes);
/* 153 */     System.arraycopy(reversedNbits, 0, newData, 72, 4);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 161 */     this.data = newData;
/*     */   }
/*     */   
/*     */   private byte[] calculateMerkleRoot() {
/* 165 */     SHA256d hasher = new SHA256d(32);
/*     */     
/*     */     try {
/* 168 */       String coinbase = this.coinbase1 + this.coinbase1 + this.extraNonce1;
/*     */ 
/*     */       
/* 171 */       if (coinbase.length() + this.coinbase2.length() > 2147483646) {
/* 172 */         throw new IllegalStateException("Coinbase transaction too large");
/*     */       }
/*     */ 
/*     */       
/* 176 */       coinbase = coinbase + coinbase;
/*     */       
/* 178 */       Console.debug("Coinbase : " + coinbase, 2);
/*     */       
/* 180 */       hasher.update(DataUtils.hexStringToByteArray(coinbase));
/* 181 */       byte[] merkleRoot = new byte[64];
/* 182 */       System.arraycopy(hasher.doubleDigest(), 0, merkleRoot, 0, 32);
/*     */       
/* 184 */       for (String h : this.merkleBranch) {
/* 185 */         System.arraycopy(DataUtils.hexStringToByteArray(h), 0, merkleRoot, 32, 32);
/* 186 */         hasher.update(merkleRoot);
/* 187 */         System.arraycopy(hasher.doubleDigest(), 0, merkleRoot, 0, 32);
/*     */       } 
/*     */       
/* 190 */       byte[] finalMerkleRoot = new byte[32];
/* 191 */       System.arraycopy(merkleRoot, 0, finalMerkleRoot, 0, 32);
/* 192 */       Console.debug("FinalMerkle : " + DataUtils.byteArrayToHexString(finalMerkleRoot), 2);
/* 193 */       return finalMerkleRoot;
/*     */     } finally {
/* 195 */       hasher.cleanup();
/*     */     } 
/*     */   }
/*     */   
/*     */   public byte[] incrementExtraNonce2() {
/* 200 */     this.stateLock.lock();
/*     */     try {
/* 202 */       byte[] current = this.xnonce2.get();
/* 203 */       byte[] next = (byte[])current.clone();
/*     */       
/* 205 */       for (int i = 0; i < next.length; i++) {
/* 206 */         next[i] = (byte)(next[i] + 1);
/* 207 */         if (next[i] != 0)
/*     */           break; 
/*     */       } 
/* 210 */       this.xnonce2.set(next);
/* 211 */       invalidateCache();
/* 212 */       updateBlockHeader();
/* 213 */       return (byte[])next.clone();
/*     */     } finally {
/* 215 */       this.stateLock.unlock();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public byte[] randomizeExtraNonce2() {
/* 221 */     this.stateLock.lock();
/*     */     try {
/* 223 */       byte[] randomBytes = new byte[((byte[])this.xnonce2.get()).length];
/* 224 */       SecureRandom secureRandom = new SecureRandom();
/* 225 */       secureRandom.nextBytes(randomBytes);
/*     */       
/* 227 */       this.xnonce2.set(randomBytes);
/* 228 */       invalidateCache();
/* 229 */       updateBlockHeader();
/* 230 */       return (byte[])randomBytes.clone();
/*     */     } finally {
/* 232 */       this.stateLock.unlock();
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void updateNTime() {
/* 238 */     long currentTime = Long.parseLong(this.ntime, 16) + 1L;
/*     */ 
/*     */     
/* 241 */     String hexTime = String.format("%08x", new Object[] { Long.valueOf(currentTime) });
/* 242 */     this.ntime = hexTime;
/* 243 */     invalidateCache();
/* 244 */     updateBlockHeader();
/*     */   }
/*     */   private void invalidateCache() {
/* 247 */     this.cacheLock.lock();
/*     */     try {
/* 249 */       this.cachedMerkleRoot = null;
/*     */     } finally {
/* 251 */       this.cacheLock.unlock();
/*     */     } 
/*     */   }
/*     */   
/*     */   public byte[] getHeader() {
/* 256 */     byte[] header = (byte[])this.data.clone();
/* 257 */     byte[] nonce = DataUtils.hexStringToByteArray(String.format("%08x", new Object[] { Integer.valueOf(this.nonce) }));
/* 258 */     System.arraycopy(nonce, 0, header, 76, 4);
/* 259 */     Console.debug("Header data inside getheader " + DataUtils.byteArrayToHexString(header), 2);
/* 260 */     return header;
/*     */   }
/*     */   
/*     */   public boolean meetsTarget(int[] solution) {
/*     */     try {
/* 265 */       if (solution == null || solution.length != 42) {
/* 266 */         Console.debug("Arrêt Solution", 2);
/* 267 */         return false;
/*     */       } 
/*     */       
/* 270 */       byte[] targetCopy = this.target;
/* 271 */       if (targetCopy == null) {
/* 272 */         Console.debug("Arrêt Target", 2);
/* 273 */         return false;
/*     */       } 
/*     */ 
/*     */       
/* 277 */       ByteArrayOutputStream baos = new ByteArrayOutputStream();
/* 278 */       for (int n : solution) {
/* 279 */         byte[] bytes = new byte[4];
/* 280 */         bytes[0] = (byte)(n & 0xFF);
/* 281 */         bytes[1] = (byte)(n >> 8 & 0xFF);
/* 282 */         bytes[2] = (byte)(n >> 16 & 0xFF);
/* 283 */         bytes[3] = (byte)(n >> 24 & 0xFF);
/* 284 */         baos.write(bytes);
/*     */       } 
/*     */       
/* 287 */       SHA256d hasher = new SHA256d(32);
/* 288 */       hasher.update(baos.toByteArray());
/* 289 */       byte[] hash = hasher.doubleDigest();
/* 290 */       BigInteger hashValue = new BigInteger(1, DataUtils.reverseBytes(hash));
/* 291 */       if (hashValue.compareTo(BigInteger.ZERO) < 0) {
/* 292 */         return false;
/*     */       }
/*     */       
/* 295 */       BigInteger targetValue = new BigInteger(1, DataUtils.reverseBytes(targetCopy));
/*     */       
/* 297 */       BigInteger truediffone = new BigInteger("452312848583266388373324160190187140051835877600158453279131187530910662656");
/* 298 */       double diff = truediffone.doubleValue() / hashValue.doubleValue();
/* 299 */       Console.debug("Share Difficulty : " + diff, 2);
/* 300 */       return (hashValue.compareTo(targetValue) <= 0);
/*     */     }
/* 302 */     catch (Exception e) {
/* 303 */       Console.debug("Error checking target: " + e.getMessage(), 2);
/* 304 */       return false;
/*     */     } 
/*     */   }
/*     */   
/*     */   public void setTarget(byte[] newTarget) {
/* 309 */     if (newTarget == null || newTarget.length != 32) {
/* 310 */       throw new IllegalArgumentException("Invalid target");
/*     */     }
/* 312 */     this.target = (byte[])newTarget.clone();
/* 313 */     Console.debug("Target : " + DataUtils.byteArrayToHexString(this.target), 2);
/*     */   }
/*     */   
/*     */   private static List<String> parseStringArray(JSONArray array) {
/* 317 */     if (array == null) {
/* 318 */       throw new IllegalArgumentException("Array cannot be null");
/*     */     }
/*     */     
/* 321 */     List<String> result = new ArrayList<>();
/* 322 */     for (int i = 0; i < array.length(); i++) {
/* 323 */       result.add(array.getString(i));
/*     */     }
/* 325 */     return result;
/*     */   }
/*     */   
/*     */   public String getJobId() {
/* 329 */     return this.jobId;
/* 330 */   } public byte[] getData() { return (byte[])this.data.clone(); }
/* 331 */   public byte[] getTarget() { return (this.target != null) ? (byte[])this.target.clone() : null; }
/* 332 */   public byte[] getXnonce2() { return (byte[])((byte[])this.xnonce2.get()).clone(); }
/* 333 */   public String getNtime() { return this.ntime; } public boolean isCleanJobs() {
/* 334 */     return this.cleanJobs;
/*     */   }
/*     */ 
/*     */   
/*     */   public static class Builder
/*     */   {
/*     */     private String jobId;
/*     */     private String prevHash;
/*     */     private String coinbase1;
/*     */     private String coinbase2;
/*     */     private List<String> merkleBranch;
/*     */     private String version;
/*     */     private String nbits;
/*     */     private String ntime;
/*     */     private boolean cleanJobs;
/*     */     
/*     */     public Builder jobId(String jobId) {
/* 351 */       this.jobId = jobId;
/* 352 */       return this;
/*     */     }
/*     */     
/*     */     public Builder prevHash(String prevHash) {
/* 356 */       this.prevHash = prevHash;
/* 357 */       return this;
/*     */     }
/*     */     
/*     */     public Builder coinbase1(String coinbase1) {
/* 361 */       this.coinbase1 = coinbase1;
/* 362 */       return this;
/*     */     }
/*     */     
/*     */     public Builder coinbase2(String coinbase2) {
/* 366 */       this.coinbase2 = coinbase2;
/* 367 */       return this;
/*     */     }
/*     */     
/*     */     public Builder merkleBranch(List<String> merkleBranch) {
/* 371 */       this.merkleBranch = merkleBranch;
/* 372 */       return this;
/*     */     }
/*     */     
/*     */     public Builder version(String version) {
/* 376 */       this.version = version;
/* 377 */       return this;
/*     */     }
/*     */     
/*     */     public Builder nbits(String nbits) {
/* 381 */       this.nbits = nbits;
/* 382 */       return this;
/*     */     }
/*     */     
/*     */     public Builder ntime(String ntime) {
/* 386 */       this.ntime = ntime;
/* 387 */       return this;
/*     */     }
/*     */     
/*     */     public Builder cleanJobs(boolean cleanJobs) {
/* 391 */       this.cleanJobs = cleanJobs;
/* 392 */       return this;
/*     */     }
/*     */     
/*     */     public StratumWork build() {
/* 396 */       validate();
/* 397 */       return new StratumWork(this);
/*     */     }
/*     */     
/*     */     private void validate() {
/* 401 */       if (this.jobId == null || this.prevHash == null || this.coinbase1 == null || this.coinbase2 == null || this.merkleBranch == null || this.version == null || this.nbits == null || this.ntime == null)
/*     */       {
/*     */         
/* 404 */         throw new IllegalStateException("All fields must be non-null");
/*     */       }
/*     */     }
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\stratum\StratumWork.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */