/*     */ package live.thought.jtminer.stratum;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.PrintWriter;
/*     */ import java.net.Socket;
/*     */ import java.util.Observable;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import java.util.concurrent.locks.ReentrantLock;
/*     */ import live.thought.jtminer.StratumMiner;
/*     */ import live.thought.jtminer.data.DataUtils;
/*     */ import live.thought.jtminer.util.Console;
/*     */ import live.thought.jtminer.util.Logger;
/*     */ import org.json.JSONArray;
/*     */ import org.json.JSONException;
/*     */ import org.json.JSONObject;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class StratumClient
/*     */   extends Observable
/*     */ {
/*     */   private final String url;
/*     */   private final int port;
/*     */   private final String username;
/*     */   private final String password;
/*     */   private final ReconnectionHandler reconnectionHandler;
/*     */   private final ConnectionMonitor connectionMonitor;
/*     */   private Socket socket;
/*     */   private BufferedReader reader;
/*     */   private PrintWriter writer;
/*     */   private volatile boolean connected;
/*     */   private volatile StratumWork currentWork;
/*  37 */   private final ReentrantLock workLock = new ReentrantLock();
/*     */   
/*     */   private volatile String extraNonce1;
/*     */   private volatile int extraNonce2Size;
/*     */   private volatile double diff;
/*  42 */   private final AtomicInteger nextId = new AtomicInteger(3);
/*  43 */   private final Object outputLock = new Object();
/*     */   
/*  45 */   private String VERSION = "v0.8.0";
/*     */   
/*     */   private StratumMiner.WorkHandler workHandler;
/*     */   
/*     */   public StratumClient(String url, int port, String username, String password) {
/*  50 */     if (url == null || username == null || password == null) {
/*  51 */       throw new IllegalArgumentException("URL, username and password cannot be null");
/*     */     }
/*  53 */     this.url = url;
/*  54 */     this.port = port;
/*  55 */     this.username = username;
/*  56 */     this.password = password;
/*  57 */     this.reconnectionHandler = new ReconnectionHandler(this);
/*  58 */     this.connectionMonitor = new ConnectionMonitor(this);
/*     */   }
/*     */   
/*     */   public void setWorkHandler(StratumMiner.WorkHandler handler) {
/*  62 */     this.workHandler = handler;
/*  63 */     if (this.reconnectionHandler != null) {
/*  64 */       this.reconnectionHandler.setWorkHandler(handler);
/*     */     }
/*     */   }
/*     */   
/*     */   public ReconnectionHandler getReconnectionHandler() {
/*  69 */     return this.reconnectionHandler;
/*     */   }
/*     */   
/*     */   private void resetMiningState() {
/*  73 */     this.workLock.lock();
/*     */     try {
/*  75 */       this.currentWork = null;
/*  76 */       this.extraNonce1 = null;
/*  77 */       this.extraNonce2Size = 0;
/*  78 */       this.diff = 0.0D;
/*     */     } finally {
/*  80 */       this.workLock.unlock();
/*     */     } 
/*     */   }
/*     */   
/*     */   public void connect() throws StratumException {
/*  85 */     resetMiningState();
/*     */     
/*     */     try {
/*  88 */       this.socket = new Socket(this.url, this.port);
/*  89 */       this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
/*  90 */       this.writer = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
/*  91 */       this.connected = true;
/*     */       
/*  93 */       Thread responseThread = new Thread(this::readResponses, "Stratum-Reader");
/*  94 */       responseThread.setDaemon(true);
/*  95 */       responseThread.start();
/*     */       
/*  97 */       subscribe();
/*  98 */       authorize();
/*     */ 
/*     */       
/* 101 */       this.connectionMonitor.start();
/*     */     }
/* 103 */     catch (IOException e) {
/* 104 */       cleanup();
/* 105 */       throw new StratumException(StratumException.Type.CONNECTION_ERROR, "Failed to connect: " + e.getMessage());
/*     */     } 
/*     */   }
/*     */   
/*     */   private void readResponses() {
/*     */     try {
/*     */       String line;
/* 112 */       while (this.connected && (line = this.reader.readLine()) != null) {
/*     */         try {
/* 114 */           Console.debug(line, 2);
/* 115 */           JSONObject response = new JSONObject(line);
/* 116 */           handleResponse(response);
/* 117 */         } catch (JSONException e) {
/* 118 */           Console.debug("Invalid JSON response: " + line, 2);
/*     */         } 
/*     */       } 
/* 121 */     } catch (IOException e) {
/* 122 */       if (this.connected) {
/* 123 */         Console.output("Connection lost: " + e.getMessage());
/*     */ 
/*     */ 
/*     */         
/* 127 */         this.connected = false;
/*     */ 
/*     */         
/* 130 */         closeNetworkResources();
/*     */ 
/*     */         
/* 133 */         notifyError(new StratumException(StratumException.Type.CONNECTION_ERROR, "Connection lost: " + e
/* 134 */               .getMessage()));
/*     */ 
/*     */         
/* 137 */         if (this.reconnectionHandler != null) {
/* 138 */           this.reconnectionHandler.startReconnection();
/*     */         } else {
/* 140 */           Console.output("ERROR: ReconnectionHandler is null, cannot reconnect");
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   private void closeNetworkResources() {
/*     */     try {
/* 148 */       if (this.socket != null) {
/*     */         try {
/* 150 */           this.socket.close();
/* 151 */         } catch (IOException e) {
/* 152 */           Console.debug("Error closing socket: " + e.getMessage(), 2);
/*     */         } 
/*     */       }
/*     */       
/* 156 */       if (this.reader != null) {
/*     */         try {
/* 158 */           this.reader.close();
/* 159 */         } catch (IOException e) {
/* 160 */           Console.debug("Error closing reader: " + e.getMessage(), 2);
/*     */         } 
/*     */       }
/*     */       
/* 164 */       if (this.writer != null) {
/* 165 */         this.writer.close();
/*     */       }
/*     */       
/* 168 */       this.socket = null;
/* 169 */       this.reader = null;
/* 170 */       this.writer = null;
/* 171 */     } catch (Exception e) {
/* 172 */       Console.debug("Error in closeNetworkResources: " + e.getMessage(), 2);
/*     */     } 
/*     */   }
/*     */   
/*     */   private void notifyReconnectionSuccess() {
/* 177 */     setChanged();
/* 178 */     notifyObservers(new StratumNotification(StratumNotification.Type.RECONNECTION_SUCCESS));
/* 179 */     Logger.log("Reconnection success notification sent");
/*     */   }
/*     */   
/*     */   private void handleResponse(JSONObject response) {
/*     */     try {
/* 184 */       if (response.has("method")) {
/* 185 */         handleNotification(response);
/* 186 */       } else if (response.has("id")) {
/* 187 */         int idint = response.getInt("id");
/* 188 */         String id = response.get("id").toString();
/* 189 */         if (idint == 0) {
/* 190 */           JSONObject errorObj = response.getJSONObject("error");
/* 191 */           int errorCode = errorObj.getInt("code");
/* 192 */           String errorMessage = errorObj.getString("message");
/* 193 */           Console.output("Error: " + errorMessage + " (code: " + errorCode + ")");
/*     */           
/* 195 */           Thread.sleep(30000L);
/*     */         
/*     */         }
/* 198 */         else if (response.has("result") && idint == 1) {
/*     */           
/* 200 */           JSONArray result = response.getJSONArray("result");
/* 201 */           if (result.length() >= 3) {
/* 202 */             String newExtraNonce1 = result.getString(1);
/* 203 */             int newExtraNonce2Size = result.getInt(2);
/*     */             
/* 205 */             this.workLock.lock();
/*     */             try {
/* 207 */               this.extraNonce1 = newExtraNonce1;
/* 208 */               this.extraNonce2Size = newExtraNonce2Size;
/* 209 */               Console.debug("Got extranonce1: " + this.extraNonce1 + ", extranonce2_size: " + this.extraNonce2Size, 2);
/*     */             } finally {
/* 211 */               this.workLock.unlock();
/*     */             } 
/*     */ 
/*     */             
/* 215 */             notifyReconnectionSuccess();
/*     */           }
/*     */         
/* 218 */         } else if (idint == 2) {
/*     */           
/* 220 */           String result = response.get("result").toString();
/* 221 */           if (result.equals("true")) {
/* 222 */             Console.debug("Worker authorized", 2);
/*     */             
/* 224 */             notifyReconnectionSuccess();
/*     */           } else {
/*     */             
/* 227 */             Console.debug("Worker not authorized", 2);
/*     */             
/* 229 */             disconnect();
/* 230 */             this.reconnectionHandler.startReconnection();
/*     */           }
/*     */         
/* 233 */         } else if (idint > 0) {
/* 234 */           this.connectionMonitor.notificationReceived();
/* 235 */           String result = response.get("result").toString();
/* 236 */           if (result.equals("true")) {
/* 237 */             this.workHandler.handleSubmitSuccess();
/*     */           } else {
/*     */             
/* 240 */             String error = response.get("error").toString();
/* 241 */             this.workHandler.handleSubmitFailure(error);
/*     */           } 
/*     */         } 
/*     */       } 
/* 245 */     } catch (Exception e) {
/* 246 */       Console.debug("Error handling response: " + e.getMessage(), 2);
/*     */     } 
/*     */   }
/*     */   private void handleNotification(JSONObject notification) {
/*     */     double newDiff;
/* 251 */     String method = notification.getString("method");
/* 252 */     JSONArray params = notification.getJSONArray("params");
/*     */     
/* 254 */     switch (method) {
/*     */       case "mining.notify":
/* 256 */         this.connectionMonitor.notificationReceived();
/* 257 */         if (this.workHandler != null) {
/* 258 */           StratumWork newWork = StratumWork.fromNotification(params);
/* 259 */           if (this.currentWork == null || !newWork.getJobId().equals(this.currentWork.getJobId())) {
/* 260 */             Logger.log("-----New job received from pool - Job ID: " + newWork.getJobId() + " (replacing job: " + ((this.currentWork != null) ? this.currentWork.getJobId() : "none") + ")");
/* 261 */             this.workHandler.stop();
/* 262 */             updateWork(newWork);
/* 263 */             this.currentWork.setTarget(TargetUtils.diffToTarget(this.diff));
/* 264 */             this.currentWork.nonce = 0;
/* 265 */             this.workHandler.handleNewWork(this.currentWork);
/*     */           } 
/*     */         } 
/*     */         break;
/*     */       
/*     */       case "mining.set_difficulty":
/* 271 */         this.connectionMonitor.notificationReceived();
/* 272 */         newDiff = params.getDouble(0);
/* 273 */         this.diff = newDiff;
/* 274 */         this.workLock.lock();
/*     */         try {
/* 276 */           if (this.currentWork != null) {
/* 277 */             this.currentWork.setTarget(TargetUtils.diffToTarget(this.diff));
/*     */           }
/*     */         } finally {
/* 280 */           this.workLock.unlock();
/*     */         } 
/* 282 */         Console.output("New diff: " + this.diff);
/*     */         break;
/*     */     } 
/*     */   }
/*     */   
/*     */   private void updateWork(StratumWork newWork) {
/* 288 */     this.workLock.lock();
/*     */     try {
/* 290 */       this.currentWork = null;
/* 291 */       this.currentWork = newWork;
/* 292 */       this.currentWork.prepareWork(this.extraNonce1, this.extraNonce2Size);
/* 293 */       Console.debug("Job : " + this.currentWork.getJobId(), 2);
/*     */     } finally {
/* 295 */       this.workLock.unlock();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void subscribe() throws StratumException {
/* 303 */     JSONObject request = (new JSONObject()).put("id", "1").put("method", "mining.subscribe").put("params", (new JSONArray()).put("jtminer/" + this.VERSION));
/* 304 */     sendRequest(request);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private void authorize() throws StratumException {
/* 311 */     JSONObject request = (new JSONObject()).put("id", "2").put("method", "mining.authorize").put("params", (new JSONArray()).put(this.username).put(this.password));
/* 312 */     sendRequest(request);
/*     */   }
/*     */   
/*     */   public void submitWork(StratumWork work, String solution) throws StratumException {
/* 316 */     if (!this.connected) {
/* 317 */       throw new StratumException(StratumException.Type.CONNECTION_ERROR, "Not connected to server");
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 322 */     JSONObject request = (new JSONObject()).put("id", getNextMessageId()).put("method", "mining.submit").put("params", (new JSONArray())
/* 323 */         .put(this.username)
/* 324 */         .put(work.getJobId())
/* 325 */         .put(DataUtils.byteArrayToHexString(work.getXnonce2()))
/* 326 */         .put(work.getNtime())
/* 327 */         .put(String.format("%08x", new Object[] { Integer.valueOf(Integer.reverseBytes(work.nonce))
/* 328 */             })).put(solution));
/* 329 */     sendRequest(request);
/*     */   }
/*     */   
/*     */   public void sendRequest(JSONObject request) {
/* 333 */     if (this.connected && this.writer != null) {
/* 334 */       this.writer.println(request.toString());
/*     */     }
/*     */   }
/*     */   
/*     */   private String getNextMessageId() {
/* 339 */     return String.valueOf(this.nextId.getAndIncrement());
/*     */   }
/*     */   
/*     */   private void notifyError(StratumException e) {
/* 343 */     setChanged();
/* 344 */     notifyObservers(new StratumNotification(StratumNotification.Type.ERROR, e.getMessage()));
/*     */   }
/*     */   
/*     */   public void disconnect() {
/* 348 */     if (!this.connected)
/* 349 */       return;  this.connected = false;
/*     */     
/* 351 */     resetMiningState();
/* 352 */     closeNetworkResources();
/*     */     
/* 354 */     if (this.workHandler != null) {
/* 355 */       this.workHandler.stop();
/*     */     }
/*     */   }
/*     */   
/*     */   public boolean isReconnecting() {
/* 360 */     return (this.reconnectionHandler != null && this.reconnectionHandler.isReconnecting());
/*     */   }
/*     */   
/*     */   public void cleanup() {
/* 364 */     deleteObservers();
/*     */     
/* 366 */     if (this.connectionMonitor != null) {
/* 367 */       this.connectionMonitor.stop();
/*     */     }
/*     */     
/* 370 */     disconnect();
/*     */ 
/*     */ 
/*     */     
/* 374 */     if (this.reconnectionHandler != null && !this.reconnectionHandler.isReconnecting())
/* 375 */       this.reconnectionHandler.stop(); 
/*     */   }
/*     */   
/*     */   public boolean isConnected() {
/* 379 */     return this.connected;
/* 380 */   } public String getExtraNonce1() { return this.extraNonce1; }
/* 381 */   public int getExtraNonce2Size() { return this.extraNonce2Size; } public void setConnected(boolean connected) {
/* 382 */     this.connected = connected;
/*     */   }
/*     */   public StratumWork getCurrentWork() {
/* 385 */     this.workLock.lock();
/*     */     try {
/* 387 */       return this.currentWork;
/*     */     } finally {
/* 389 */       this.workLock.unlock();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public boolean ping() {
/* 398 */     if (!isConnected()) {
/* 399 */       return false;
/*     */     }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*     */     try {
/* 409 */       JSONObject request = new JSONObject();
/* 410 */       request.put("id", this.nextId.getAndIncrement());
/* 411 */       request.put("method", "mining.subscribe");
/* 412 */       request.put("params", new JSONArray());
/*     */ 
/*     */ 
/*     */       
/* 416 */       synchronized (this.outputLock) {
/* 417 */         if (this.writer != null) {
/* 418 */           this.writer.write(request.toString());
/* 419 */           this.writer.write("\n");
/* 420 */           this.writer.flush();
/* 421 */           return true;
/*     */         } 
/*     */       } 
/* 424 */       return false;
/* 425 */     } catch (Exception e) {
/* 426 */       Console.debug("Ping failed: " + e.getMessage(), 2);
/* 427 */       return false;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public ConnectionMonitor getConnectionMonitor() {
/* 436 */     return this.connectionMonitor;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void handleConnectionTimeout() {
/* 444 */     setConnected(false);
/*     */     
/* 446 */     Console.output("Connection timeout detected. Initiating reconnection process.");
/*     */ 
/*     */     
/*     */     try {
/* 450 */       closeNetworkResources();
/* 451 */     } catch (Exception e) {
/* 452 */       Console.debug("Error closing connection: " + e.getMessage(), 2);
/*     */     } 
/*     */ 
/*     */     
/* 456 */     if (this.reconnectionHandler != null) {
/* 457 */       this.reconnectionHandler.startReconnection();
/*     */     } else {
/* 459 */       Console.output("Error: Reconnection handler is null!");
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void restartConnectionMonitor() {
/* 468 */     if (this.connectionMonitor != null) {
/*     */       
/* 470 */       this.connectionMonitor.stop();
/*     */ 
/*     */       
/*     */       try {
/* 474 */         Thread.sleep(200L);
/* 475 */       } catch (InterruptedException e) {
/* 476 */         Thread.currentThread().interrupt();
/*     */       } 
/*     */ 
/*     */       
/* 480 */       this.connectionMonitor.start();
/* 481 */       Console.debug("Connection monitor explicitly restarted after reconnection", 2);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\stratum\StratumClient.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */