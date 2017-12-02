package simpledb.test;


import simpledb.buffer.Buffer;
import simpledb.buffer.BufferMgr;

import java.util.ArrayList;

import simpledb.file.Block;
import simpledb.server.SimpleDB;

public class LRUKTEST {

	public static void main(String[] args) {
		
		SimpleDB.BUFFER_SIZE=8;
		SimpleDB.LRU_K_VALUE=2;
		SimpleDB.LOG_FILE="lruktest-1.log";
		SimpleDB.initFileLogAndBufferMgr("lruktest");
		BufferMgr BM = SimpleDB.bufferMgr();
		
		ArrayList<Block> blocks=new ArrayList<Block>();
		ArrayList<Buffer> buffers=new ArrayList<Buffer>();
		
		for(int i=1;i<=8;++i) {
			blocks.add(new Block("test1",i));
		}
		
		buffers.add(BM.pin(blocks.get(0),1));
		buffers.add(BM.pin(blocks.get(1),2));
		buffers.add(BM.pin(blocks.get(2),3));
		buffers.add(BM.pin(blocks.get(3),4));
		buffers.add(BM.pin(blocks.get(4),5));
		buffers.add(BM.pin(blocks.get(5),6));
		buffers.add(BM.pin(blocks.get(6),7));
		buffers.add(BM.pin(blocks.get(7),8));
		BM.pin(blocks.get(3),9);
		BM.pin(blocks.get(1),10);
		BM.pin(blocks.get(6),11);
		BM.pin(blocks.get(0),12);
		BM.unpin(buffers.get(7));
		BM.unpin(buffers.get(6));
		BM.unpin(buffers.get(5));
		BM.unpin(buffers.get(4));
		BM.unpin(buffers.get(3));
		BM.unpin(buffers.get(0));
		BM.unpin(buffers.get(6));
		BM.unpin(buffers.get(3));
		BM.unpin(buffers.get(1));
		BM.unpin(buffers.get(1));
		BM.pin(new Block("test1",9),23);
	}

}
