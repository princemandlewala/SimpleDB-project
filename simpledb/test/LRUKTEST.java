package simpledb.test;


import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;
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
	}

}
