package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FeatureExtract {
	WordIndexer wordIndex; //记录index地址
	
	FeatureExtract() {
		this.wordIndex.readIndex();
	}
	
	public void getEduFeature() {
		for (int i = 0; i < this.wordIndex.indexPathes.size(); i++) {
			String path = this.wordIndex.indexPathes.get(i);
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
				input.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if ((i+1) % 1000 == 0) {
				System.out.println("has index "+(i+1)+" docs.");
			}
		}
	}
	
	public static void main(String[] args) {
		
	}
}
