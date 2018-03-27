package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class MatrixIndexer {
	public static final String topWordPath = "output/topwordreorder.txt"; //使用的高频词表
	public static final String topWordPath_nonfilter = "output/topwordreorder_nonfilter.txt"; //使用的高频词表(未滤词)
	public static final String featureMatrixPath = "output/featurematrix.txt"; //记录的文档-词项表
	public static final String featureMatrixPath_nonfilter = "output/featurematrix_nonfilter.txt"; //记录的文档-词项表
	public WordIndexer wordIndex; //导入index地址文件
	
	public MatrixIndexer() {
		this.wordIndex = new WordIndexer();
		this.wordIndex.readIndex();
	}
	
	public void setFeatureMatrix() {
		try {
			PrintStream output = null;
			Scanner input = null;
			if (this.wordIndex.isFiltered) {
				 output = new PrintStream(new File(MatrixIndexer.featureMatrixPath));
				 input = new Scanner(new File(MatrixIndexer.topWordPath));
			} else {
				 output = new PrintStream(new File(MatrixIndexer.featureMatrixPath_nonfilter));
				 input = new Scanner(new File(MatrixIndexer.topWordPath_nonfilter));
			}
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
			for (int i = 0; i < this.wordIndex.indexPathes.size(); i++) {
				//int i = 0;
				String path = this.wordIndex.indexPathes.get(i);
				//System.out.println("current path: "+path);
				Scanner input1 = new Scanner(new File(path));
				//进入正文之前
				while (input1.hasNextLine()) {
					String line = input1.nextLine();
					if (line.length() == 0) {
						break;
					}			
				}
				//进入正文之后
				while (input1.hasNextLine()) {
					String line = input1.nextLine();
					String[] splits = line.split(" ");
					for (int j = 0; j < splits.length; j++) {
						if (topWordCount.containsKey(splits[j])) {
							int count = topWordCount.get(splits[j])+1;
							topWordCount.put(splits[j], count);
						}
					}
					//System.out.println(line);
				}
				input1.close();
				//将这个文档相关数据写入文件中，并将计数置为0
				String thisData = ""; //记录这个文档的相关特征数据
				for (Map.Entry<String, Integer> item: topWordCount.entrySet()) {
					thisData += item.getValue()+" ";
				}
				if (this.wordIndex.isSpam.get(i)) {
					thisData += "1";
				} else {
					thisData += "0";
				}
				output.println(thisData);
				for (Map.Entry<String, Integer> item: topWordCount.entrySet()) {
					topWordCount.put(item.getKey(), 0); //初始化
				}
				if ((i+1) % 1000 == 0) {
					System.out.println("has index "+(i+1)+" docs.");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		MatrixIndexer matrix1 = new MatrixIndexer();
		matrix1.wordIndex.setFilter(false);
		matrix1.setFeatureMatrix();
	}
	
}
