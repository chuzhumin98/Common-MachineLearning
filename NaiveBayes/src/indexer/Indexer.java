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

public class Indexer {
	public final static String indexPath = "input/label/index_cut"; //index文件的地址
	public final static String topWordPath = "output/topword.txt"; //存储出现次数最多的11000个term
	public final static int topWordSize = 11000; //这里我们仅输出11000个top的terms
	ArrayList<String> indexPathes; //用来记录各文件的地址ַ
	ArrayList<Boolean> isSpam; //用来存储这些文件对应的是不是垃圾邮件
	Map<String,Integer> wordList = new HashMap<String,Integer>(); //存储词项列表的哈希表
	
	public Indexer() {
		indexPathes = new ArrayList<String>();
		isSpam = new ArrayList<Boolean>();
	}
	
	/**
	 * 读取索引文件
	 */
	public void readIndex() {
		try {
			Scanner input = new Scanner(new File(Indexer.indexPath));
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
			PrintStream output = new PrintStream(new File(Indexer.topWordPath));
			for (int i = 0; i < Indexer.topWordSize; i++) {
				output.println(list.get(i).getKey()+" "+list.get(i).getValue());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	public static void main(String[] args) {
		Indexer index1 = new Indexer();
		index1.readIndex();
		index1.setWordList();
	}
}