package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FeatureExtract {
	public WordIndexer wordIndex; //记录index地址
	public String[] mailName = {"edu","126.com","163.com","qq","sina","sohu","yahoo","microsoft"};
	public int[][] mailCount; //第一维度是垃圾邮件类型，第二维度为数量，记录各邮箱类型维度的信息情况
	public int[][] mailType; //依次判断是否为edu,126,163,qq,sina,sohu,yahoo,microsoft邮箱
	
	FeatureExtract() {
		this.wordIndex = new WordIndexer();
		this.wordIndex.readIndex();
		this.mailCount = new int [2][this.mailName.length];
		this.mailType = new int [this.wordIndex.isSpam.size()][this.mailName.length];
		for (int i = 0; i < this.mailCount.length; i++) {
			for (int j = 0; j < this.mailCount[i].length; j++) {
				this.mailCount[i][j] = 0;
			}
		}
		for (int i = 0; i < this.mailType.length; i++) {
			for (int j = 0; j < this.mailType[i].length; j++) {
				this.mailType[i][j] = 0; //默认不为该种邮箱
			}
		}
		this.getMailFeature();
	}
	
	public void getMailFeature() {
		for (int i = 0; i < this.wordIndex.indexPathes.size(); i++) {
			String path = this.wordIndex.indexPathes.get(i);
			//System.out.println("current path: "+path);
			try {
				Scanner input = new Scanner(new File(path));
				int lineNum = 0; //记录现在处于邮件头的行数
				//邮件头信息
				while (input.hasNextLine()) {
					String line = input.nextLine();
					lineNum++; 
					if (lineNum == 1) {
						//System.out.println(line);
						for (int j = 0; j < this.mailName.length; j++) {
							if (line.contains(this.mailName[j])) {
								int spamIndex = 0;
								if (this.wordIndex.isSpam.get(i)) {
									spamIndex = 1; //如果是spam则index为1，否则为0
								}
								this.mailCount[spamIndex][j]++;
								this.mailType[i][j] = 1; //1表示为该类型邮箱
								break; //不可能同时属于多种邮箱
							}
						}
					}
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
		for (int i = 0; i < this.mailCount.length; i++) {
			System.out.println("for type "+i+": ");
			for (int j = 0; j < this.mailCount[i].length; j++) {
				System.out.print(this.mailCount[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		FeatureExtract extract = new FeatureExtract();
	}
}
