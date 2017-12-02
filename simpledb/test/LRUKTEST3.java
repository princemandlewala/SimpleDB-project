package simpledb.test;


import simpledb.buffer.BufferMgr;

import simpledb.file.Block;
import simpledb.server.SimpleDB;
public class LRUKTEST3 {

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleDB.BUFFER_SIZE=8;
		SimpleDB.LRU_K_VALUE=2;
		SimpleDB.LOG_FILE="lruktest-3.log";
		SimpleDB.initFileLogAndBufferMgr("lruktest3");
		BufferMgr BM = SimpleDB.bufferMgr();
		
		Block blk1 = new Block("test3", 1);
		Block blk2 = new Block("test3", 2);
		Block blk3 = new Block("test3", 3);
		Block blk5 = new Block("test3", 5);
		Block blk7 = new Block("test3", 7);
		Block blk4 = new Block("test3", 4);
		
		BM.pin(blk1, 2);        // 1
		BM.pin(blk2, 6);
		BM.pin(blk3, 9);
		BM.pin(blk2, 12);
		BM.pin(blk5, 16);        // 1
		BM.pin(blk3, 19);
		BM.pin(blk1, 21);        // 1
		BM.pin(blk2, 25);
		BM.pin(blk3, 29);
		BM.pin(blk4, 31);
		BM.pin(blk7, 32);
		BM.pin(blk5, 35);        // 1
		BM.pin(blk7, 36);        // 1
		BM.pin(blk2, 42);
		BM.pin(blk2, 43);
		BM.pin(blk1, 47);
		BM.pin(blk4, 49);		// 1
		BM.pin(blk2, 55);
		BM.pin(blk3, 56);
		BM.pin(blk7, 62);
		BM.pin(blk3, 67);
		BM.pin(blk3, 68);
		BM.pin(blk1, 69);        // 1
		BM.pin(blk2, 70);
		BM.pin(blk5, 75);        // 1
		BM.pin(blk2, 76);
		BM.pin(blk1, 82);        // 1
		BM.pin(blk2, 89);
		BM.pin(blk4, 96);
		BM.pin(blk5, 98);        // 1
		BM.pin(blk7, 108);        // 1
		BM.pin(blk1, 120);
		BM.pin(blk4, 136);
		BM.pin(blk5, 145); // 1
		BM.pin(blk3, 156);
		BM.pin(blk7, 159);  
		BM.pin(blk3, 167);
		BM.pin(blk3, 168);
		BM.pin(blk1, 169);        // 1
		BM.pin(blk2, 170);
		BM.pin(blk2, 176);
		BM.pin(blk1, 182);        // 1
		BM.pin(blk2, 189);
		BM.pin(blk4, 196);
		BM.pin(blk5, 198);        // 1
		BM.pin(blk3, 206);
		BM.pin(blk7, 208);        // 1
		BM.pin(blk1, 220);
		BM.pin(blk2, 249);
		BM.pin(blk4, 256);
		BM.pin(blk5, 268);
		BM.pin(blk5, 277);
		BM.pin(blk7, 289);  
		BM.pin(blk4, 296);
		BM.pin(blk3, 298);        // 1
		BM.pin(blk7, 308);        // 1
		BM.pin(blk1, 320);
		BM.pin(blk2, 389);
		BM.pin(blk4, 396);
		BM.pin(blk5, 398);        // 1
		BM.pin(blk7, 408);        // 1
		BM.pin(blk1, 420);
		BM.pin(blk3, 468);
		BM.pin(blk1, 469);        // 1
		BM.pin(blk2, 570);
		BM.pin(blk2, 576);
		BM.pin(blk1, 582);        // 1
		BM.pin(blk2, 589);
		BM.pin(blk4, 596);
		BM.pin(blk5, 598);        // 1
		BM.pin(blk7, 608);        // 1
		
		BM.printBufferPoolBlocks();	
	}

}
