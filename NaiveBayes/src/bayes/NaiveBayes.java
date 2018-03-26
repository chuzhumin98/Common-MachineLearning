package bayes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class NaiveBayes {
	public static final String matrixPath = "output/featurematrix.txt";
	public int docSize = 64620; //总的文档个数
	public static final int trainSize = 45000; //训练集数据的大小
	public ArrayList<Entity> entities = new ArrayList<Entity>(); //存储实体集
	public int featureNum;
	
	public NaiveBayes() {
		this.LoadMatrix();
	}
	
	/**
	 * 载入特征矩阵信息
	 */
	public void LoadMatrix() {
		try {
			Scanner input = new Scanner(new File(NaiveBayes.matrixPath));
			int count = 0;
			while (input.hasNextLine()) {
				String line = input.nextLine();
				String[] splits = line.split(" "); //按照空格分割，最终一栏为label
				this.featureNum = splits.length - 1;
				int[] features = new int [this.featureNum];
				for (int i = 0; i < this.featureNum; i++) {
					features[i] = Integer.valueOf(splits[i]);
				}
				int label = Integer.valueOf(splits[this.featureNum]);
				Entity entity = new Entity(features, label);
				this.entities.add(entity);
				count++;
				if (count%10000 == 0) {
					System.out.println("has load docs "+count);
				}
			}
			this.docSize = this.entities.size();
			System.out.println("feature num:"+this.featureNum);
			System.out.println("doc size:"+this.docSize);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		NaiveBayes bayes = new NaiveBayes();
		
	}
	
	public class Entity {
		public int[] feature; //特征向量
		public int label; //实体的标签，1为spam，0为ham
		public Entity(int[] feature1, int label1) {
			this.feature = feature1;
			this.label = label1;
		}
	}
}
