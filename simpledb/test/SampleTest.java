package simpledb.test;

import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;
import simpledb.file.Block;
import simpledb.server.SimpleDB;

public class SampleTest {

	public static void main(String[] args) {
		
		SimpleDB.initFileLogAndBufferMgr("bufferSearchTest");
		int numBuffs = 300;
		BufferMgr bufferManager = new BufferMgr(numBuffs);
		Block[] blocks = new Block[numBuffs];
		Buffer[] buffers = new Buffer[numBuffs];
		
		for(int k=0;k<numBuffs;k++) {
			blocks[k] = new Block("bufferSearch_"+k,(k+1));
			buffers[k] = bufferManager.pin(blocks[k]);
		}
		
		for(int l=0;l<numBuffs;l++) {
			bufferManager.pin(blocks[l]);
		}
		
		Block block = new Block("bufferSearch_"+numBuffs, (numBuffs+1));
		bufferManager.unpin(buffers[0]);
		bufferManager.unpin(buffers[0]);
		
		bufferManager.pin(block); 
	}

}
