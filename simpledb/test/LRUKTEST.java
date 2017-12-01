package simpledb.test;


import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;
import simpledb.buffer.BasicBufferMgr;
import simpledb.file.Block;
import simpledb.server.SimpleDB;

public class LRUKTEST {

	public static void main(String[] args) {
		
		SimpleDB.initFileLogAndBufferMgr("lruktest");
		int numOfBuffers = 4;
		BufferMgr bufferManager = new BufferMgr(numOfBuffers,2);
		Block a1 = new Block("A1", 1);
		Block a2 = new Block("A2", 2);
		Block a3 = new Block("A3", 3);
		Block a4 = new Block("A4", 4);
		Block a5 = new Block("A5", 5);
		Block a6 = new Block("A6", 6);
		Buffer ba = bufferManager.pin(a1,(long)2);
		bufferManager.unpin(ba);
		
		Buffer bb = bufferManager.pin(a2,(long)5);
		bufferManager.unpin(bb);
		
		ba = bufferManager.pin(a1,(long)8);
		bufferManager.unpin(ba);
		
		bb = bufferManager.pin(a2,(long)16);
		bufferManager.unpin(bb);
		
		Buffer bc = bufferManager.pin(a3,(long)20);
		bufferManager.unpin(bc);
		
		Buffer bd = bufferManager.pin(a4,(long)24);
		bufferManager.unpin(bd);
		
		Buffer be = bufferManager.pin(a5,(long)30);
		bufferManager.unpin(be);
		
		Buffer bff = bufferManager.pin(a6,(long)40);
		bufferManager.unpin(bff);
		
		ba = bufferManager.pin(a1,(long)45);
		bufferManager.unpin(ba);
		
		bc = bufferManager.pin(a3,(long)54);
		bufferManager.unpin(bc);
		
		bd = bufferManager.pin(a4,(long)70);
		bufferManager.unpin(bd);
		
		be = bufferManager.pin(a5,(long)80);
		bufferManager.unpin(be);
		
		bff = bufferManager.pin(a6,(long)100);
		bufferManager.unpin(bff);
		
		//System.out.println(BasicBufferMgr.hashBufferPool.size());
		System.out.print("buffer contents at the end of the test ->");
		BasicBufferMgr.printBufferPoolBlocks();
	}

}
