package simpledb.test;


import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;

import simpledb.file.Block;
import simpledb.server.SimpleDB;
public class LRUKTEST4 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleDB.BUFFER_SIZE=8;
		SimpleDB.LRU_K_VALUE=2;
		SimpleDB.LOG_FILE="lruktest-4.log";
		SimpleDB.initFileLogAndBufferMgr("lruktest4");
		BufferMgr BM = SimpleDB.bufferMgr();
		
		Block blk1 = new Block("test3", 1);
		Block blk2 = new Block("test3", 2);
		Block blk3 = new Block("test3", 3);
		Block blk4 = new Block("test3", 4);
		Block blk5 = new Block("test3", 5);
		Block blk6 = new Block("test4", 6);
		Block blk7 = new Block("test4", 7);
		Block blk8 = new Block("test4", 8);
		Block blk9 = new Block("test4", 9);
		Block blk10 = new Block("test4",10);
		
		Buffer block1pin = BM.pin(blk1,1);
		BM.pin(blk2,2);
		BM.pin(blk2,3);
		Buffer block1unpin = BM.pin(blk1,4);
		BM.pin(blk3,5);
		BM.pin(blk4,6);
		BM.pin(blk5,7);
		BM.pin(blk6,8);
		BM.pin(blk6,9);
		BM.pin(blk7,10);
		BM.pin(blk7,11);
		BM.pin(blk8,12);
		BM.pin(blk9,13);
		Buffer block10pin = BM.pin(blk10,14);
		Buffer block10unpin = BM.pin(blk10,15);
		BM.unpin(block10pin);
		BM.unpin(block10unpin);
		BM.unpin(block1pin);
		BM.unpin(block1unpin);
		Block blk11 = new Block("test1",16);
		BM.pin(blk11,17);
	}

}
