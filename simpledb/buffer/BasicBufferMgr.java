package simpledb.buffer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import simpledb.file.*;
import simpledb.server.SimpleDB;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * @author Edward Sciore
 *
 */
public class BasicBufferMgr {
   /**
    * @author priyance
    * replaced the existing bufferpool which was an array with a HashMap
    */
   private static HashMap<Block, Buffer> bufferPoolMap;
   private int bufferPoolSize;
   private HashMap<Block, Long[]> history;
   private HashMap<Block, Long> lastAssigned;
   private int LRU_K_VALUE = SimpleDB.LRU_K_VALUE;
   // default doesn't have any LRU policy
   private int numAvailable;
   
   private int correlatedTimeSpan = 10;
   /**
    * Creates a buffer manager having the specified number 
    * of buffer slots.
    * This constructor depends on both the {@link FileMgr} and
    * {@link simpledb.log.LogMgr LogMgr} objects 
    * that it gets from the class
    * {@link simpledb.server.SimpleDB}.
    * Those objects are created during system initialization.
    * Thus this constructor cannot be called until 
    * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or
    * is called first.
    * @param numbuffs the number of buffer slots to allocate
    */
   
   /**
    * 
    * @param numbuffs
    * @author priyance
    * All the variables used for the Buffer Management are initialized here.
    * News 
    */
   BasicBufferMgr(int numbuffs) {
      numAvailable = numbuffs;
      bufferPoolSize = numbuffs;
      bufferPoolMap = new LinkedHashMap<Block,Buffer>(numbuffs);
      history = new HashMap<Block, Long[]>();
      lastAssigned = new HashMap<Block, Long>();  
   }
   
   /**
    * @author priyance
    * @param numbuffs
    * @param lru_K_value
    * this constructor takes the value of number of buffers and k value for LRU algorithm to apply for choosing an unpinned buffer
    */
   
   public BasicBufferMgr(int numbuffs, int lru_K_value) {
	   this(numbuffs);
	   LRU_K_VALUE = lru_K_value;
   }
   
   /**
    * @author priyance
    * Flushes the dirty buffers modified by the specified transaction.
    * @param txnum the transaction's id number
    */
   synchronized void flushAll(int txnum) {
	   Iterator<Entry<Block, Buffer>> iterator = bufferPoolMap.entrySet().iterator();
	   while(iterator.hasNext()) {
		   Entry<Block, Buffer> newEntry = iterator.next();
		   Buffer buffer = newEntry.getValue();
		   if(buffer.isModifiedBy(txnum)) {
			   buffer.flush();
		   }
	   }
   }
   
   /**
    * @author priyance
    * @param blk
    * @param timeWhenBlockPinned
    * @return
    * 
    * the method takes the time at which the buffer is being pinned as an input parameter
    * If the input parameter value is not being sent by the user, the method will take the default value as the current time of the system in milliseconds
    */
   synchronized Buffer pin(Block blk, long timeWhenBlockPinned) {
      System.out.println("Required to pin the buffer no: "+blk+" at time "+ timeWhenBlockPinned);
      Buffer buffer = findExistingBufferUsingHashMap(blk);
      if(buffer == null) {
    	  System.out.println("There is no existing buffer present. Process to choose and unpinned buffer started.");
    	  buffer = chooseUnpinnedBuffer(timeWhenBlockPinned);
    	  if(buffer==null) {
    		  return null;
    	  }
    	  
    	  System.out.println("Replacement policy will replace "+buffer.block() + " by the block " + blk);
    	  bufferPoolMap.remove(buffer.block());
    	  
    	  buffer.assignToBlock(blk);
      }
      
      if(!buffer.isPinned())
    	  numAvailable = numAvailable-1;
      buffer.pin();
      
      bufferPoolMap.put(blk, buffer);
      
      long currentTime = timeWhenBlockPinned;
      if(lastAssigned.containsKey(blk)) {
    	  long lastTime = lastAssigned.get(blk);
    	  if(currentTime-lastTime > correlatedTimeSpan) {
    		  Long[] historyCreated = history.get(blk);
    		  shiftValuesByOneIndex(historyCreated, timeWhenBlockPinned);
    		  history.put(blk, historyCreated);
    	  }
      }
      else {
    	  Long[] historyCreated = initializeNewHistory(timeWhenBlockPinned);
    	  history.put(blk, historyCreated);
      }
      lastAssigned.put(blk, currentTime);
      return buffer;
   }
   
   
   /**
    *@author priyance
    *@param block
    *@return
    * the constructor is used to get the current time of the system in milliseconds if the time to pin the buffer is not sent by the user.
    */
   synchronized Buffer pin(Block block) {
	   return pin(block, System.currentTimeMillis());
   }
   /**
    * Allocates a new block in the specified file, and
    * pins a buffer to it. 
    * Returns null (without allocating the block) if 
    * there are no available buffers.
    * @param filename the name of the file
    * @param fmtr a pageformatter object, used to format the new block
    * @return the pinned buffer
    */
   synchronized Buffer pinNew(String filename, PageFormatter fmtr) {
      Buffer buff = chooseUnpinnedBuffer(System.currentTimeMillis());
      if (buff == null)
         return null;
      buff.assignToNew(filename, fmtr);
      numAvailable--;
      buff.pin();
      return buff;
   }
   
   /**
    * Unpins the specified buffer.
    * @param buff the buffer to be unpinned
    */
   synchronized void unpin(Buffer buff) {
      buff.unpin();
      if (!buff.isPinned())
         numAvailable++;
   }
   
   /**
    * Returns the number of available (i.e. unpinned) buffers.
    * @return the number of available buffers
    */
   int available() {
      return numAvailable;
   }
   
   private Buffer findExistingBufferUsingHashMap(Block blk) {
      return bufferPoolMap.get(blk);
   }
   
   /**
    * @author priyance
    * Chooses an unpinned buffer based on the LRU-K policy
    * @param timeWhenBlockPinned
    * @return
    */
   
   private Buffer chooseUnpinnedBuffer(long timeWhenBlockPinned) {
	   System.out.println("Choosing an unpinned buffer to pin");
	   if(bufferPoolMap.size() < bufferPoolSize) {
		   System.out.println("Miracle!! There are new buffers to pin as there is space left in the buffer pool.");
		   return new Buffer();
	   }
	   else {
		   long currentTime = timeWhenBlockPinned;
		   long minimum = timeWhenBlockPinned;
		   
		   Iterator<Entry<Block,Buffer>> iterator = bufferPoolMap.entrySet().iterator();
		   Buffer bufferToReturn = null;
		   while(iterator.hasNext()) {
			   Entry<Block,Buffer> entry = iterator.next();
			   Buffer buffer = entry.getValue();
			   
			   long lastTimeAccessed = lastAssigned.get(buffer.block());
			   long historyLast = getLastAccessedBuffer(buffer.block());
			   
			   if(!buffer.isPinned() && historyLast < minimum && (currentTime - lastTimeAccessed) > correlatedTimeSpan){
				   minimum = historyLast;
				   bufferToReturn = buffer;
			   }
	   		}
	   if(bufferToReturn != null) {
		   System.out.println("Victimized buffer: "+ bufferToReturn.block().fileName());
	   }
	   else {
		   System.out.println("No unpinned buffer left");
	   }
	   return bufferToReturn;
	   }
   }
  
   /**
    * 
    */
   public static void printBufferPoolBlocks() {
	   Iterator<Entry<Block,Buffer>> iterator = bufferPoolMap.entrySet().iterator();
	   while(iterator.hasNext()) {
		   Entry<Block,Buffer> entry = iterator.next();
		   System.out.println(entry.getKey().fileName()+" ");
	   }
	   System.out.println();
   }
   
   /**
    * @author priyance
    * history of the LRU-K algorithm needs to be initilaized based on the K value. Most recent entry of the buffer goes to 0th index and 
    * the most least used entry is retrieved from the K-1 index.
    * @param timeWhenblockPinned
    * @return
    */
   
   private Long[] initializeNewHistory(long timeWhenblockPinned) {
	   Long[] historyCreated = new Long[LRU_K_VALUE];
	   for(int k=1; k<LRU_K_VALUE;k++) {
		   historyCreated[k] = (long) - 1;
	   }
	   historyCreated[0] = timeWhenblockPinned;
	   return historyCreated;
   }
   
   /**
    * @author priyance
    * method to shift history values by one index
    * @param historyCreated
    * @param timeWhenBlockPinned
    */
   
   void shiftValuesByOneIndex(Long[] historyCreated, long timeWhenBlockPinned) {
	   long previousTime = historyCreated[0];
	   historyCreated[0] = previousTime;
	   for(int j=0;j<LRU_K_VALUE;j++) {
		   long temporary = previousTime;
		   previousTime = historyCreated[j];
		   historyCreated[j] = temporary;
	   }
   }
   
   private long getLastAccessedBuffer(Block block) {
	   Long[] historyCreated = history.get(block);
	   String historyValues = "";
	   for(int q = LRU_K_VALUE-1;q>=0;q--) {
		   historyValues+="-"+historyCreated[q];
	   }
	   System.out.println("History value created for "+block.fileName()+": [ "+historyValues+" ] , last value for the block is : "+ lastAssigned.get(block));
	   return historyCreated[LRU_K_VALUE-1];
   }
}
