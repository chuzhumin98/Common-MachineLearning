package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 筛选出来的高频词的索引，主要涉及过滤停用词、数字，对热词进行信息增益的排序
 * 
 * @author chuzhumin
 *
 */
public class WordIndexer {
	public final static String indexPath = "input/label/index_cut"; //index文件的地址
	public final static String topWordPath = "output/topword.txt"; //存储出现次数最多的12000个term
	public final static String reorderTopWordPath = "output/topwordreorder.txt"; //存储重排序后的热门词汇
	public final static int topWordSize = 12000; //这里我们仅输出12000个top的terms
	public ArrayList<String> indexPathes; //用来记录各文件的地址ַ
	public ArrayList<Boolean> isSpam; //用来存储这些文件对应的是不是垃圾邮件
	public Map<String,Integer> wordList = new HashMap<String,Integer>(); //存储词项列表的哈希表
	public boolean isFiltered = true; //记录是否进行term过滤
	
	public WordIndexer() {
		indexPathes = new ArrayList<String>();
		isSpam = new ArrayList<Boolean>();
	}
	
	/**
	 * 读取索引文件
	 */
	public void readIndex() {
		try {
			Scanner input = new Scanner(new File(WordIndexer.indexPath));
			while (input.hasNextLine()) {
				String line = input.nextLine();
				String[] splits = line.split(" ");
				if (splits[0].equals("spam")) {
					this.isSpam.add(true);
					//System.out.println("true");
				} else {
					this.isSpam.add(false);
					//System.out.println("false");
				}
				this.indexPathes.add(splits[1]);
			}
			input.close();
			//System.out.println(this.indexPathes.size()+" "+this.isSpam.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("error to find index_cut file");
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置词项列表
	 */
	public void setWordList() {
		for (int i = 0; i < this.indexPathes.size(); i++) {
			//int i = 0;
			String path = this.indexPathes.get(i);
			//System.out.println("current path: "+path);
			try {
				Scanner input = new Scanner(new File(path));
				//进入正文之前
				while (input.hasNextLine()) {
					String line = input.nextLine();
					if (line.length() == 0) {
						break;
					}			
				}
				//进入正文之后
				while (input.hasNextLine()) {
					String line = input.nextLine();
					String[] splits = line.split(" ");
					for (int j = 0; j < splits.length; j++) {
						if (splits[j].length() > 0) {
							if (this.wordList.containsKey(splits[j])) {
								int count = this.wordList.get(splits[j])+1; //如果有这个term，直接+1
								this.wordList.put(splits[j], count);
							} else {
								this.wordList.put(splits[j], 1);
							}
						}
					}
					//System.out.println(line);
				}
				input.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ((i+1) % 1000 == 0) {
				System.out.println("has index "+(i+1)+" docs.");
				System.out.println("current size: "+this.wordList.size());
			}
		}
		System.out.println("has index all the documents");
		System.out.println("current size: "+this.wordList.size());
		// 对HashMap中的key 进行排序  
		List<Map.Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(this.wordList.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {  
            public int compare(Map.Entry<String, Integer> o1,  
                    Map.Entry<String, Integer> o2) {  
                return (o2.getValue().compareTo(o1.getValue()));  
            }  
        });  
        //for (int i = 0; i < 1000; i++) {
        //	System.out.println(list.get(i).getKey()+": "+list.get(i).getValue());
        //}
        try {
			PrintStream output = new PrintStream(new File(WordIndexer.topWordPath));
			for (int i = 0; i < WordIndexer.topWordSize; i++) {
				output.println(list.get(i).getKey()+" "+list.get(i).getValue());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 根据信息增益对term进行重排序,并输出前size的结果
	 */
	public void reorderTopword(int size) {
		if (this.indexPathes.size() == 0) {
			this.readIndex(); //还没有读取索引时先读取索引信息
		}
		int spamTotal = 0; //计数所有的spam类的文档个数
		int hamTotal = 0;
		for (int i = 0; i < this.isSpam.size(); i++) {
			if (this.isSpam.get(i)) {
				spamTotal++;
			} else {
				hamTotal++;
			}
		}
		System.out.println("spam number:"+spamTotal);
		System.out.println("ham number:"+hamTotal);
		File file = null;
		if (this.isFiltered) {
			file = new File(Filter.filterPath);
		} else {
			file = new File(WordIndexer.topWordPath);
		}
		try {
			Scanner input = new Scanner(file);
			Map<String,WordInfo> topWordList = new HashMap<String,WordInfo>(); //记录高频词的信息增益
			while (input.hasNextLine()) {
				String line = input.nextLine();
				String[] splits = line.split(" ");
				WordInfo info1 = new WordInfo();
				info1.count = splits[1];
				topWordList.put(splits[0], info1);
			}
			for (int i = 0; i < this.indexPathes.size(); i++) {
				//int i = 0;
				String path = this.indexPathes.get(i);
				//System.out.println("current path: "+path);
				try {
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
							if (splits[j].length() > 0 && topWordList.containsKey(splits[j])) {
								if (this.isSpam.get(i)) {
									topWordList.get(splits[j]).spamIncrease(i);
								} else {
									topWordList.get(splits[j]).hamIncrease(i);
								}
							}
						}
						//System.out.println(line);
					}
					input1.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ((i+1) % 1000 == 0) {
					System.out.println("has index "+(i+1)+" docs.");
					System.out.println("current size: "+topWordList.size());
				}
			}
			System.out.println("has index all the documents");
			System.out.println("current size: "+topWordList.size());
			for (Map.Entry<String, WordInfo> item: topWordList.entrySet()) {
				item.getValue().calculateInfoGain(spamTotal, hamTotal);
			}
			// 对HashMap中的key 进行排序  
			List<Map.Entry<String,WordInfo>> list = new ArrayList<Map.Entry<String,WordInfo>>(topWordList.entrySet());
	        Collections.sort(list, new Comparator<Map.Entry<String, WordInfo>>() {  
	            public int compare(Map.Entry<String, WordInfo> o1,  
	                    Map.Entry<String, WordInfo> o2) {  
	                return (o2.getValue().gain.compareTo(o1.getValue().gain));  
	            }  
	        }); 
	        try {
				PrintStream output = new PrintStream(new File(WordIndexer.reorderTopWordPath));
				for (int i = 0; i < size; i++) {
					output.println(list.get(i).getKey()+" "+list.get(i).getValue().gain
							+" "+list.get(i).getValue().count);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		WordIndexer index1 = new WordIndexer();
		index1.readIndex();
		if (index1.isFiltered) {
			//index1.setWordList();
			//Filter.main(null);
		}
		index1.reorderTopword(1000);
	}
	

}
