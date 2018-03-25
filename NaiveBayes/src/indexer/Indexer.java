package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Indexer {
	public final static String indexPath = "input/label/index_cut"; //index文件的地址
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
		//for (int i = 0; i < this.indexPathes.size(); i++) {
			int i = 0;
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
					System.out.println(line);
				}
				input.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}
	}
	
	public static void main(String[] args) {
		Indexer index1 = new Indexer();
		index1.readIndex();
		index1.setWordList();
	}
}
