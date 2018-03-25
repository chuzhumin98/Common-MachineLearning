package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Indexer {
	public final static String indexPath = "input/label/index_cut"; //index文件的地址
	ArrayList<String> indexPathes; //用来记录各文件的地址
	ArrayList<Boolean> isSpam; //用来存储这些文件对应的是不是垃圾邮件
	
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
			//System.out.println(this.indexPathes.size()+" "+this.isSpam.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.err.println("error to find index_cut file");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Indexer index1 = new Indexer();
		index1.readIndex();
	}
}
