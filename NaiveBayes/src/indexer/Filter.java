package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Filter {
	public static final String filterPath = "output/topfilterword.txt";
	public static final String stopwordPath = "input/stopwords.txt";
	ArrayList<String> stopWords = new ArrayList<String>(); //用来存储停用词表
	ArrayList<String> topfilterword = new ArrayList<String>(); //用来存储过滤后的高频词
	
	public Filter() {
		try {
			Scanner input = new Scanner(new File(Filter.stopwordPath));
			while (input.hasNextLine()) {
				String word = input.nextLine();
				//System.out.println(word);
				this.stopWords.add(word);
			}
			System.out.println("stop word size:"+this.stopWords.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 去掉热门词汇中的停用词(以及数字)，并将结果保存至文件中
	 */
	public void doFilter() {
		try {
			Scanner input = new Scanner(new File(Indexer.topWordPath));
			int count = 0;
			while (input.hasNextLine()) {
				String line = input.nextLine();
				count++;
				String[] splits = line.split(" ");
				if (!this.stopWords.contains(splits[0])) {
					this.topfilterword.add(line);
				}
			}
			System.out.println("total lines:"+count);
			System.out.println("top word after filtering:"+this.topfilterword.size());
			this.printFilterResult();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 将过滤后的高频词写入文件中
	 */
	private void printFilterResult() {
		try {
			PrintStream output = new PrintStream(new File(Filter.filterPath));
			for (int i = 0; i < this.topfilterword.size(); i++) {
				output.println(this.topfilterword.get(i));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		Filter filter = new Filter();
		filter.doFilter();
	}
}
