package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Filter {
	public static final String filterPath = "output/topfilterword.txt";
	public static final String stopwordPath = "input/stopwords.txt";
	ArrayList<String> stopWords = new ArrayList<String>();
	
	public Filter() {
		try {
			Scanner input = new Scanner(new File(Filter.stopwordPath));
			while (input.hasNextLine()) {
				this.stopWords.add(input.nextLine());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 去掉热门词汇中的停用词(以及数字)，并将结果保存至文件中
	 */
	void doFilter() {
		try {
			Scanner input = new Scanner(new File(Indexer.indexPath));
			while (input.hasNextLine()) {
				String line = input.nextLine();
				String[] splits = line.split(" ");
				
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
