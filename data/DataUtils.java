/*     */ package live.thought.jtminer.data;
/*     */ 
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.math.BigInteger;
/*     */ import java.util.Arrays;
/*     */ import live.thought.jtminer.algo.SHA256d;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class DataUtils
/*     */ {
/*  13 */   private static SHA256d hasher = new SHA256d();
/*     */ 
/*     */   
/*     */   public static String byteArrayToHexString(byte[] b) {
/*  17 */     StringBuilder sb = new StringBuilder(80);
/*  18 */     for (int i = 0; i < b.length; i++)
/*  19 */       sb.append(Integer.toString((b[i] & 0xFF) + 256, 16).substring(1)); 
/*  20 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] hexStringToByteArray(String s) {
/*  25 */     int len = s.length();
/*  26 */     String source = null;
/*  27 */     if (len % 2 != 0) {
/*     */       
/*  29 */       source = "0" + s;
/*  30 */       len++;
/*     */     }
/*     */     else {
/*     */       
/*  34 */       source = s;
/*     */     } 
/*  36 */     byte[] data = new byte[len / 2];
/*  37 */     for (int i = 0; i < len; i += 2)
/*     */     {
/*  39 */       data[i / 2] = (byte)((Character.digit(source.charAt(i), 16) << 4) + Character.digit(source.charAt(i + 1), 16));
/*     */     }
/*  41 */     return data;
/*     */   }
/*     */   
/*  44 */   private static final char[] BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
/*  45 */   private static int[] toInt = new int[128];
/*     */   public static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
/*     */   
/*     */   static {
/*  49 */     for (int i = 0; i < BASE64_ALPHABET.length; i++)
/*     */     {
/*  51 */       toInt[BASE64_ALPHABET[i]] = i;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public static String stringToBase64(String str) {
/*  57 */     byte[] buf = str.getBytes();
/*  58 */     int size = buf.length;
/*  59 */     char[] ar = new char[(size + 2) / 3 * 4];
/*  60 */     int a = 0;
/*  61 */     int i = 0;
/*  62 */     while (i < size) {
/*     */       
/*  64 */       byte b0 = buf[i++];
/*  65 */       byte b1 = (i < size) ? buf[i++] : 0;
/*  66 */       byte b2 = (i < size) ? buf[i++] : 0;
/*  67 */       ar[a++] = BASE64_ALPHABET[b0 >> 2 & 0x3F];
/*  68 */       ar[a++] = BASE64_ALPHABET[(b0 << 4 | (b1 & 0xFF) >> 4) & 0x3F];
/*  69 */       ar[a++] = BASE64_ALPHABET[(b1 << 2 | (b2 & 0xFF) >> 6) & 0x3F];
/*  70 */       ar[a++] = BASE64_ALPHABET[b2 & 0x3F];
/*     */     } 
/*  72 */     switch (size % 3) {
/*     */       
/*     */       case 1:
/*  75 */         ar[--a] = '=';
/*     */       case 2:
/*  77 */         ar[--a] = '='; break;
/*     */     } 
/*  79 */     return new String(ar);
/*     */   }
/*     */ 
/*     */   
/*     */   public static void uint32ToByteArrayBE(long val, byte[] out, int offset) {
/*  84 */     out[offset + 0] = (byte)(int)(0xFFL & val >> 24L);
/*  85 */     out[offset + 1] = (byte)(int)(0xFFL & val >> 16L);
/*  86 */     out[offset + 2] = (byte)(int)(0xFFL & val >> 8L);
/*  87 */     out[offset + 3] = (byte)(int)(0xFFL & val >> 0L);
/*     */   }
/*     */ 
/*     */   
/*     */   public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
/*  92 */     out[offset + 0] = (byte)(int)(0xFFL & val >> 0L);
/*  93 */     out[offset + 1] = (byte)(int)(0xFFL & val >> 8L);
/*  94 */     out[offset + 2] = (byte)(int)(0xFFL & val >> 16L);
/*  95 */     out[offset + 3] = (byte)(int)(0xFFL & val >> 24L);
/*     */   }
/*     */   
/*     */   public static byte reverseBitsByte(byte x) {
/*  99 */     int intSize = 8;
/* 100 */     byte y = 0;
/* 101 */     for (int position = intSize - 1; position > 0; position--) {
/* 102 */       y = (byte)(y + ((x & 0x1) << position));
/* 103 */       x = (byte)(x >> 1);
/*     */     } 
/* 105 */     return y;
/*     */   }
/*     */ 
/*     */   
/*     */   public static long readUint32(byte[] bytes, int offset) {
/* 110 */     return (bytes[offset++] & 0xFFL) << 0L | (bytes[offset++] & 0xFFL) << 8L | (bytes[offset++] & 0xFFL) << 16L | (bytes[offset] & 0xFFL) << 24L;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public static long readUint32BE(byte[] bytes, int offset) {
/* 116 */     return (bytes[offset + 0] & 0xFFL) << 24L | (bytes[offset + 1] & 0xFFL) << 16L | (bytes[offset + 2] & 0xFFL) << 8L | (bytes[offset + 3] & 0xFFL) << 0L;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static BigInteger decodeMPI(byte[] mpi) {
/* 127 */     int length = (int)readUint32BE(mpi, 0);
/* 128 */     byte[] buf = new byte[length];
/* 129 */     System.arraycopy(mpi, 4, buf, 0, length);
/* 130 */     return new BigInteger(buf);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static BigInteger decodeCompactBits(long compact) {
/* 138 */     int size = (int)(compact >> 24L) & 0xFF;
/* 139 */     byte[] bytes = new byte[4 + size];
/* 140 */     bytes[3] = (byte)size;
/* 141 */     if (size >= 1)
/* 142 */       bytes[4] = (byte)(int)(compact >> 16L & 0xFFL); 
/* 143 */     if (size >= 2)
/* 144 */       bytes[5] = (byte)(int)(compact >> 8L & 0xFFL); 
/* 145 */     if (size >= 3)
/* 146 */       bytes[6] = (byte)(int)(compact >> 0L & 0xFFL); 
/* 147 */     return decodeMPI(bytes);
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] reverseBytes(byte[] bytes) {
/* 152 */     byte[] buf = new byte[bytes.length];
/* 153 */     for (int i = 0; i < bytes.length; i++)
/* 154 */       buf[i] = bytes[bytes.length - 1 - i]; 
/* 155 */     return buf;
/*     */   }
/*     */ 
/*     */   
/*     */   public static byte[] encodeCompact(long height) {
/* 160 */     byte[] retval = null;
/* 161 */     if (height <= 252L) {
/*     */       
/* 163 */       retval = new byte[1];
/* 164 */       retval[0] = (byte)(int)(height & 0xFFL);
/*     */     }
/* 166 */     else if (height < 65535L) {
/*     */       
/* 168 */       retval = new byte[3];
/* 169 */       retval[0] = -3;
/* 170 */       retval[1] = (byte)(int)(height & 0xFFL);
/* 171 */       retval[2] = (byte)(int)(height >> 8L & 0xFFL);
/*     */     }
/* 173 */     else if (height < -1L) {
/*     */       
/* 175 */       retval = new byte[5];
/* 176 */       retval[0] = -13;
/* 177 */       retval[1] = (byte)(int)(height & 0xFFL);
/* 178 */       retval[2] = (byte)(int)(height >> 8L & 0xFFL);
/* 179 */       retval[3] = (byte)(int)(height >> 16L & 0xFFL);
/* 180 */       retval[4] = (byte)(int)(height >> 24L & 0xFFL);
/*     */     } 
/* 182 */     return retval;
/*     */   }
/*     */ 
/*     */   
/*     */   public static String bytesToBase58(byte[] data) {
/* 187 */     return rawBytesToBase58(addCheckHash(data));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   static String rawBytesToBase58(byte[] data) {
/* 194 */     StringBuilder sb = new StringBuilder();
/* 195 */     BigInteger num = new BigInteger(1, data);
/* 196 */     while (num.signum() != 0) {
/* 197 */       BigInteger[] quotrem = num.divideAndRemainder(ALPHABET_SIZE);
/* 198 */       sb.append("123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".charAt(quotrem[1].intValue()));
/* 199 */       num = quotrem[0];
/*     */     } 
/*     */ 
/*     */     
/* 203 */     for (int i = 0; i < data.length && data[i] == 0; i++)
/* 204 */       sb.append("123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".charAt(0)); 
/* 205 */     return sb.reverse().toString();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   static byte[] addCheckHash(byte[] data) {
/*     */     try {
/* 212 */       hasher.update(data);
/* 213 */       byte[] hash = Arrays.copyOf(hasher.doubleDigest(), 4);
/* 214 */       ByteArrayOutputStream buf = new ByteArrayOutputStream();
/* 215 */       buf.write(data);
/* 216 */       buf.write(hash);
/* 217 */       return buf.toByteArray();
/* 218 */     } catch (IOException e) {
/* 219 */       throw new AssertionError(e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public static byte[] base58ToBytes(String s) {
/* 227 */     byte[] concat = base58ToRawBytes(s);
/* 228 */     byte[] data = Arrays.copyOf(concat, concat.length - 4);
/* 229 */     byte[] hash = Arrays.copyOfRange(concat, concat.length - 4, concat.length);
/* 230 */     hasher.update(data);
/* 231 */     byte[] rehash = Arrays.copyOf(hasher.doubleDigest(), 4);
/* 232 */     if (!Arrays.equals(rehash, hash))
/* 233 */       throw new IllegalArgumentException("Checksum mismatch"); 
/* 234 */     return data;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected static byte[] base58ToRawBytes(String s) {
/* 241 */     BigInteger num = BigInteger.ZERO;
/* 242 */     for (int i = 0; i < s.length(); i++) {
/* 243 */       num = num.multiply(ALPHABET_SIZE);
/* 244 */       int digit = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".indexOf(s.charAt(i));
/* 245 */       if (digit == -1)
/* 246 */         throw new IllegalArgumentException("Invalid character for Base58Check"); 
/* 247 */       num = num.add(BigInteger.valueOf(digit));
/*     */     } 
/*     */ 
/*     */     
/* 251 */     byte[] b = num.toByteArray();
/* 252 */     if (b[0] == 0) {
/* 253 */       b = Arrays.copyOfRange(b, 1, b.length);
/*     */     }
/*     */     
/*     */     try {
/* 257 */       ByteArrayOutputStream buf = new ByteArrayOutputStream();
/* 258 */       for (int j = 0; j < s.length() && s.charAt(j) == "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".charAt(0); j++)
/* 259 */         buf.write(0); 
/* 260 */       buf.write(b);
/* 261 */       return buf.toByteArray();
/* 262 */     } catch (IOException e) {
/* 263 */       throw new AssertionError(e);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/* 268 */   private static final BigInteger ALPHABET_SIZE = BigInteger.valueOf("123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".length());
/*     */ 
/*     */   
/*     */   public static byte[] addressToScript(String addr) {
/* 272 */     byte[] retval = null;
/*     */     
/* 274 */     byte[] addrbin = base58ToBytes(addr);
/* 275 */     byte addrver = addrbin[0];
/*     */     
/* 277 */     switch (addrver) {
/*     */       case -63:
/*     */       case 9:
/* 280 */         retval = new byte[23];
/* 281 */         retval[0] = -87;
/* 282 */         retval[1] = 20;
/* 283 */         System.arraycopy(addrbin, 1, retval, 2, 20);
/* 284 */         retval[22] = -121; break;
/*     */     } 
/* 286 */     retval = new byte[25];
/* 287 */     retval[0] = 118;
/* 288 */     retval[1] = -87;
/* 289 */     retval[2] = 20;
/* 290 */     System.arraycopy(addrbin, 1, retval, 3, 20);
/* 291 */     retval[23] = -120;
/* 292 */     retval[24] = -84;
/*     */     
/* 294 */     return retval;
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\data\DataUtils.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */