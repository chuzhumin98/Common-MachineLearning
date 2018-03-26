package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MatrixIndexer {
	public static final String topWordPath = "output/topwordreorder.txt"; //使用的高频词表
	public static final String featureMatrixPath = "output/featurematrix.txt"; //记录的文档-词项表
	public WordIndexer wordIndex; //导入index地址文件
	
	public MatrixIndexer() {
		this.wordIndex = new WordIndexer();
		this.wordIndex.readIndex();
	}
	
	public void setFeatureMatrix() {
		try {
			PrintStream output = new PrintStream(new File(MatrixIndexer.featureMatrixPath));
			Scanner input = new Scanner(new File(MatrixIndexer.topWordPath));
			Map<String, Integer> topWordCount = new HashMap<String, Integer>(); //记录高频词在某个文档中出现的次数
			/**
			 * 这一部分导入高频词到哈希表中
			 */
			while (input.hasNextLine()) {
				String line = input.nextLine();
				String[] splits = line.split(" ");
				topWordCount.put(splits[0], 0);
			}
			/**
			 * 开始文档的读取和特征的写入
			 */
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
