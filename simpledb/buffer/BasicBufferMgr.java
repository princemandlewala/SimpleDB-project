package simpledb.buffer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import simpledb.file.*;
import simpledb.log.BasicLogRecord;
import simpledb.server.SimpleDB;

/**
 * Manages the pinning and unpinning of buffers to blocks.
 * 
 * @author Edward Sciore
 *
 */
public class BasicBufferMgr {
	/**
	 * Added history, last accessed and HashMap of bufferpool
	 * 
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
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
	 * Creates a buffer manager having the specified number of buffer slots. This
	 * constructor depends on both the {@link FileMgr} and
	 * {@link simpledb.log.LogMgr LogMgr} objects that it gets from the class
	 * {@link simpledb.server.SimpleDB}. Those objects are created during system
	 * initialization. Thus this constructor cannot be called until
	 * {@link simpledb.server.SimpleDB#initFileAndLogMgr(String)} or is called
	 * first.
	 * 
	 * @param numbuffs
	 *            the number of buffer slots to allocate
	 */

	/**
	 * All the variables used for the Buffer Management are initialized here.
	 * 
	 * @param numbuffs
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	BasicBufferMgr(int numbuffs) {
		numAvailable = numbuffs;
		bufferPoolSize = numbuffs;
		bufferPoolMap = new LinkedHashMap<Block, Buffer>(numbuffs);
		history = new HashMap<Block, Long[]>();
		lastAssigned = new HashMap<Block, Long>();
	}

	/**
	 * this constructor takes the value of number of buffers and k value for LRU
	 * algorithm to apply for choosing an unpinned buffer
	 * 
	 * @param numbuffs
	 * @param lru_K_value
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */

	public BasicBufferMgr(int numbuffs, int lru_K_value) {
		this(numbuffs);
		LRU_K_VALUE = lru_K_value;
	}

	/**
	 * Flushes the dirty buffers modified by the specified transaction.
	 * 
	 * @param txnum
	 *            the transaction's id number
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	synchronized void flushAll(int txnum) {
		Iterator<Entry<Block, Buffer>> iterator = bufferPoolMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Block, Buffer> newEntry = iterator.next();
			Buffer buffer = newEntry.getValue();
			if (buffer.isModifiedBy(txnum)) {
				buffer.flush();
			}
		}
	}

	/**
	 * @param blk
	 * @param timeWhenBlockPinned
	 * @return
	 * 
	 * 		the method takes the time at which the buffer is being pinned as an
	 *         input parameter If the input parameter value is not being sent by the
	 *         user, the method will take the default value as the current time of
	 *         the system in milliseconds
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	synchronized Buffer pin(Block blk, long timeWhenBlockPinned) {
		LogPrint("Pin Buffer : " + blk + " at time " + timeWhenBlockPinned);
		Buffer buffer = findExistingBufferUsingHashMap(blk);
		if (buffer == null) {
			LogPrint("\tNo buffer present.");
			buffer = chooseUnpinnedBuffer(timeWhenBlockPinned);
			if (buffer == null) {
				return null;
			}

			if (buffer.block() != null) {
				LogPrint("\t" + buffer.block() + " will be replaced by the block " + blk);
			}
			bufferPoolMap.remove(buffer.block());

			buffer.assignToBlock(blk);
		}

		if (!buffer.isPinned())
			numAvailable = numAvailable - 1;
		buffer.pin();

		bufferPoolMap.put(blk, buffer);

		long currentTime = timeWhenBlockPinned;
		if (lastAssigned.containsKey(blk)) {
			long lastTime = lastAssigned.get(blk);
			if (currentTime - lastTime > correlatedTimeSpan) {
				Long[] historyCreated = history.get(blk);
				shiftValuesByOneIndex(historyCreated, timeWhenBlockPinned);
				history.put(blk, historyCreated);
			}
		} else {
			Long[] historyCreated = initializeNewHistory(timeWhenBlockPinned);
			history.put(blk, historyCreated);
		}
		lastAssigned.put(blk, currentTime);
		return buffer;
	}

	/**
	 * 
	 * @param block
	 * @return the constructor is used to get the current time of the system in
	 *         milliseconds if the time to pin the buffer is not sent by the user.
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	synchronized Buffer pin(Block block) {
		return pin(block, System.currentTimeMillis());
	}

	/**
	 * Allocates a new block in the specified file, and pins a buffer to it. Returns
	 * null (without allocating the block) if there are no available buffers.
	 * 
	 * @param filename
	 *            the name of the file
	 * @param fmtr
	 *            a pageformatter object, used to format the new block
	 * @return the pinned buffer
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
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
	 * 
	 * @param buff
	 *            the buffer to be unpinned
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	synchronized void unpin(Buffer buff) {
		LogPrint("Unpin Buffer : " + buff.block());
		buff.unpin();
		if (!buff.isPinned())
			numAvailable++;
	}

	/**
	 * Returns the number of available (i.e. unpinned) buffers.
	 * 
	 * @return the number of available buffers
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	int available() {
		return numAvailable;
	}

	/**
	 * 
	 * @param blk
	 * @return existing buffer
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	private Buffer findExistingBufferUsingHashMap(Block blk) {
		return bufferPoolMap.get(blk);
	}

	/**
	 * Chooses an unpinned buffer based on the LRU-K policy
	 * @param timeWhenBlockPinned
	 * @return
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */

	private Buffer chooseUnpinnedBuffer(long timeWhenBlockPinned) {
		LogPrint("\tSelecting an unpinned buffer");
		if (bufferPoolMap.size() < bufferPoolSize) {
			LogPrint("\tPinning to a new buffer in Pool.");
			return new Buffer();
		} else {
			long currentTime = timeWhenBlockPinned;
			long minimum = timeWhenBlockPinned;

			Iterator<Entry<Block, Buffer>> iterator = bufferPoolMap.entrySet().iterator();
			Buffer bufferToReturn = null;
			while (iterator.hasNext()) {
				Entry<Block, Buffer> entry = iterator.next();
				Buffer buffer = entry.getValue();

				long lastTimeAccessed = lastAssigned.get(buffer.block());
				long historyLast = getLastAccessedBuffer(buffer.block());

				if (!buffer.isPinned() && historyLast < minimum
						&& (currentTime - lastTimeAccessed) > correlatedTimeSpan) {
					minimum = historyLast;
					bufferToReturn = buffer;
				}
			}
			if (bufferToReturn != null) {
				LogPrint("\tReplacing buffer: " + bufferToReturn.block());
			} else {
				LogPrint("\tNo unpinned buffer left");
			}
			return bufferToReturn;
		}
	}

	/**
	 * Printing the log
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	public void printLogBuffer() {
		Iterator<BasicLogRecord> logs = SimpleDB.logMgr().iterator();
		while (logs.hasNext()) {
			System.out.println(logs.next().nextString());
		}
	}

	/**
	 * For appending the log message in the LogMgr
	 * @param message
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	private void LogPrint(String message) {
		SimpleDB.logMgr().append(new Object[] { message });
	}

	/**
	 * For testcases developed simpledb.test package
	* @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	*/
	public void printBufferPoolBlocks() {
		Iterator<Entry<Block, Buffer>> iterator = bufferPoolMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Block, Buffer> entry = iterator.next();
			Long[] historyCreated = history.get(entry.getKey());
			String historyValues = "" + historyCreated[LRU_K_VALUE - 1];
			for (int q = LRU_K_VALUE - 2; q >= 0; q--) {
				historyValues += "||" + historyCreated[q];
			}
			LogPrint(entry.getKey() + " : History -> " + historyValues + " , Last Accessed ->  "
					+ lastAssigned.get(entry.getKey()));
		}
	}

	/**
	 * history of the LRU-K algorithm needs to be initilaized based
	 *         on the K value. Most recent entry of the buffer goes to 0th index and
	 *         the most least used entry is retrieved from the K-1 index.
	 * @param timeWhenblockPinned
	 * @return
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */

	private Long[] initializeNewHistory(long timeWhenblockPinned) {
		Long[] historyCreated = new Long[LRU_K_VALUE];
		for (int k = 1; k < LRU_K_VALUE; k++) {
			historyCreated[k] = (long) -1;
		}
		historyCreated[0] = timeWhenblockPinned;
		return historyCreated;
	}

	/**
	 * method to shift history values by one index
	 * @param historyCreated
	 * @param timeWhenBlockPinned
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */

	void shiftValuesByOneIndex(Long[] historyCreated, long timeWhenBlockPinned) {
		long previousTime = historyCreated[0];
		historyCreated[0] = timeWhenBlockPinned;
		for (int j = 0; j < LRU_K_VALUE; j++) {
			long temporary = previousTime;
			previousTime = historyCreated[j];
			historyCreated[j] = temporary;
		}
	}

	/**
	 * return the time of last accessed buffer
	 * @param block
	 * @return
	 * @author abhardw3, achauha3, kdpandya, nkapadi, pjmandle
	 */
	public long getLastAccessedBuffer(Block block) {
		Long[] historyCreated = history.get(block);
		String historyValues = "" + historyCreated[LRU_K_VALUE - 1];
		for (int q = LRU_K_VALUE - 2; q >= 0; q--) {
			historyValues += "||" + historyCreated[q];
		}
		LogPrint("\t\t" + block + " : History -> " + historyValues + " , Last Accessed ->  " + lastAssigned.get(block));
		return historyCreated[LRU_K_VALUE - 1];
	}
}
