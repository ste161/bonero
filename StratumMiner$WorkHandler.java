package live.thought.jtminer;

import live.thought.jtminer.stratum.StratumWork;

public interface WorkHandler {
  void handleNewWork(StratumWork paramStratumWork);
  
  void stop();
  
  void handleSubmitSuccess();
  
  void handleSubmitFailure(String paramString);
}


/* Location:              C:\Users\admin\Desktop\JT miner\jtminer-0.8-Stratum-jar-with-dependencies.jar!\live\thought\jtminer\StratumMiner$WorkHandler.class
 * Java compiler version: 17 (61.0)
 * JD-Core Version:       1.1.3
 */