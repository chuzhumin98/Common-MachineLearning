package indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

public class FeatureExtract {
	public WordIndexer wordIndex; //记录index地址
	public static final String mailPath = "output/mailMatrix.txt";
	public static final String timePath = "output/timeMatrix.txt";
	public static final String xMailerPath = "output/xMailerMatrix.txt";
	
	public String[] mailName = {"edu","126.com","163.com","qq","sina","sohu","yahoo","microsoft","other"};
	public int[][] mailCount; //第一维度是垃圾邮件类型，第二维度为邮箱类型，记录各邮箱类型维度的信息情况
	public int[][] mailType; //依次判断是否为edu,126,163,qq,sina,sohu,yahoo,microsoft邮箱或其他邮箱
	
	public int[][] timeCount; //第一维度是垃圾邮件类型，第二维度为时间0~23小时数，记录各时间维度信息
	public int[][] timeType; //该邮件的发送时间是否为i小时
	
	public String[] xMailerName = {"Outlook", "FoxMail", "VolleyMail", "None"}; //最后一项的含义是没有
	public int[][] xMailerCount; //第一维是垃圾邮件类型，第二维是XMailer类型
	public int[][] xMailerType; //该邮件是否为上述这些类型的XMailer
	
	
	FeatureExtract() {
		this.wordIndex = new WordIndexer();
		this.wordIndex.readIndex();
		this.mailCount = new int [2][this.mailName.length];
		this.mailType = new int [this.wordIndex.isSpam.size()][this.mailName.length];
		this.timeCount = new int [2][24];
		this.timeType = new int [this.wordIndex.isSpam.size()][24];
		this.xMailerCount = new int [2][this.xMailerName.length];
		this.xMailerType = new int [this.wordIndex.isSpam.size()][this.xMailerName.length];
		/**
		 * 系列初始化
		 */
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < this.mailCount[i].length; j++) {
				this.mailCount[i][j] = 0;
			}
			for (int j = 0; j < 24; j++) {
				this.timeCount[i][j] = 0;
			}
			for (int j = 0; j < this.xMailerName.length-1; j++) {
				this.xMailerCount[i][j] = 0;
			}
		}
		this.xMailerCount[0][this.xMailerName.length-1] = 21766;
		this.xMailerCount[1][this.xMailerName.length-1] = 42854;
		for (int i = 0; i < this.wordIndex.isSpam.size(); i++) {
			for (int j = 0; j < this.mailType[i].length; j++) {
				this.mailType[i][j] = 0; //默认不为该种邮箱
			}
			for (int j = 0; j < 24; j++) {
				this.timeType[i][j] = 0; //默认不为该时间段
			}
			for (int j = 0; j < this.xMailerName.length-1; j++) {
				this.xMailerType[i][j] = 0; //默认不含有XMailer
			}
			this.xMailerType[i][this.xMailerName.length-1] = 1;
		}
	}
	
	public void getMailFeature() {
		int xMailer = 0; //统计共有多少个XMailer项
		for (int i = 0; i < this.wordIndex.indexPathes.size(); i++) {
			int spamIndex = 0; //记录该文档的属性信息
			if (this.wordIndex.isSpam.get(i)) {
				spamIndex = 1; //如果是spam则index为1，否则为0
			}
			String path = this.wordIndex.indexPathes.get(i);
			//System.out.println("current path: "+path);
			try {
				Scanner input = new Scanner(new File(path));
				int lineNum = 0; //记录现在处于邮件头的行数
				//邮件头信息
				while (input.hasNextLine()) {
					String line = input.nextLine();
					lineNum++; 
					if (line.contains("X-Mailer")) {
						//System.out.println(line);
						xMailer++;
						for (int j = 0; j < this.xMailerName.length-1; j++) {
							if (line.contains(this.xMailerName[j])) {
								this.xMailerCount[spamIndex][j]++;
								this.xMailerType[i][j] = 1;
							}
						}
						this.xMailerCount[spamIndex][this.xMailerName.length-1]--;
						this.xMailerType[i][this.xMailerName.length-1] = 0;
					}
					if (lineNum == 1) {
						//System.out.println(line);
						boolean isOther = true; //记录是否为其他类型的邮箱
						for (int j = 0; j < this.mailName.length-1; j++) {
							if (line.contains(this.mailName[j])) {
								this.mailCount[spamIndex][j]++;
								this.mailType[i][j] = 1; //1表示为该类型邮箱
								isOther = false;
								break; //不可能同时属于多种邮箱
							}
						}
						if (isOther) {
							this.mailCount[spamIndex][this.mailName.length-1]++;
							this.mailType[i][this.mailName.length-1] = 1;
						}
					}
					if (lineNum == 3) {
						//System.out.println(line);
						// 一个样例结果：for <guo@ccert.edu.cn>; Mon, 15 Aug 2005 01:59:23 +0800 (CST)
						String[] split1 = line.split(":"); //按照正常情况，小时信息会被划分到第一部分
						String[] split2 = split1[0].split(" "); //正常情况下，小时信息会被划分到最后一部分
						String hourStr = split2[split2.length-1];
						if (Filter.isInteger(hourStr)) {
							int hour = Integer.parseInt(hourStr); //是整数的情况下转化为整数
							//System.out.println("hour:"+hour);
							if (hour < 24 && hour >= 0) {
								this.timeCount[spamIndex][hour]++;
								this.timeType[i][hour] = 1;
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
			if ((i+1) % 10000 == 0) {
				System.out.println("has index "+(i+1)+" docs.");
			}
		}
		System.out.println("XMailer num:"+xMailer);
		for (int i = 0; i < 2; i++) {
			System.out.println("for type "+i+": ");
			System.out.print("mail type:");
			for (int j = 0; j < this.mailCount[i].length; j++) {
				System.out.print(this.mailCount[i][j]+" ");
			}
			System.out.println();
			System.out.print("hour type:");
			for (int j = 0; j < 24; j++) {
				System.out.print(this.timeCount[i][j]+" ");
			}
			System.out.println();
			System.out.print("XMailer type:");
			for (int j = 0; j < this.xMailerName.length; j++) {
				System.out.print(this.xMailerCount[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	/**
	 * 将提取的新的特征写入文件中
	 * 
	 * @param path
	 * @param matrix
	 */
	public void writeFeatureMatrix(String path, int[][] matrix) {
		try {
			PrintStream output = new PrintStream(new File(path));
			for (int i = 0; i < matrix.length; i++) {
				String line = "";
				for (int j = 0; j < matrix[i].length-1; j++) {
					line += matrix[i][j]+" ";
				}
				line += matrix[i][matrix[i].length-1];
				output.println(line);
			}
			output.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FeatureExtract extract = new FeatureExtract();
		extract.getMailFeature();
		extract.writeFeatureMatrix(FeatureExtract.mailPath, extract.mailType);
		extract.writeFeatureMatrix(FeatureExtract.timePath, extract.timeType);
		extract.writeFeatureMatrix(FeatureExtract.xMailerPath, extract.xMailerType);
	}
}
