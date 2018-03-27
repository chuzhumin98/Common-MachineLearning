package bayes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NaiveBayes {
	//public static final String matrixPath = "output/featurematrix.txt";
	public static final String matrixPath = "output/featurematrix_nonfilter.txt";
	public int docSize = 64620; //总的文档个数
	public static final int trainSize = 45000; //训练集数据的大小
	public static final int testStart = 45001; //测试集数据开始位置
	public double alpha = 1.0 / Math.sqrt(trainSize); //设置平滑系数
	public ArrayList<Entity> entities = new ArrayList<Entity>(); //存储实体集
	public int featureNum; //特征的维度
	public ArrayList<Integer> sampleIndex; //样本的索引，前trainsize个为训练集，testsize之后为测试集
	public int[][] featureCount; //记录各类训练集各维度的信息
	public int[] classCount; //记录各类训练集总个数，其中0为ham,1为spam
	
	public NaiveBayes() {
		this.LoadMatrix();
		this.shuffleSample();
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
	
	/**
	 * 针对样本进行洗牌操作，从而使得训练集的选取具有随机性
	 */
	public void shuffleSample() {
		this.sampleIndex = new ArrayList<Integer>();
		for (int i = 0; i < this.docSize; i++) {
			this.sampleIndex.add(i);
		}
		Collections.shuffle(sampleIndex);
		//for (int i = 0; i < Math.min(this.sampleIndex.size(),1000); i++) {
		//	System.out.println(this.sampleIndex.get(i));
		//}
	}
	
	/**
	 * 训练Naive Bayes模型，采用0-1离散模型
	 */
	public void trainModel() {
		/**
		 * 数据的初始化操作
		 */
		this.classCount = new int [2];
		this.featureCount = new int [2][this.featureNum];
		for (int i = 0; i < 2; i++) {
			this.classCount[i] = 0;
			for (int j = 0; j < this.featureNum; j++) {
				this.featureCount[i][j] = 0;
			}
		}
		/**
		 * 计数所需的相关数据
		 */
		for (int i = 0; i < this.trainSize; i++) {
			int index = this.sampleIndex.get(i); //随机化的第i个样本点
			Entity tempEntity = this.entities.get(index);
			this.classCount[tempEntity.label]++; //对应的类型文档数加一
			for (int j = 0; j < this.featureNum; j++) {
				if (tempEntity.feature[j] > 0) { 
					this.featureCount[tempEntity.label][j]++; //该为特征中有值则记入
				}
			}
		}
		//训练结果输出展示
		System.out.println("spam doc size:"+this.classCount[1]);
		System.out.println("ham doc size:"+this.classCount[0]);
		/*System.out.println("for spam type:");
		for (int i = 0; i < this.featureNum; i++) {
			System.out.print(this.featureCount[1][i]+" ");
		}
		System.out.println();
		System.out.println("for ham type:");
		for (int i = 0; i < this.featureNum; i++) {
			System.out.print(this.featureCount[0][i]+" ");
		}
		System.out.println(); */
	}
	
	/**
	 * index在[low,high)之间的样本
	 * 不带加权信息的，普通Naive Bayes
	 * 返回正确分类的个数
	 * 
	 * @param low
	 * @param high
	 * @return
	 */
	public int testModel1(int low, int high) {
		int countRight = 0;
		for (int i = low; i < high; i++) {
			int index = this.sampleIndex.get(i); //随机化的第i个样本点
			Entity tempEntity = this.entities.get(index);
			double[] postProb = new double [2]; //对数化的后验概率
			for (int j = 0; j < 2; j++) {
				postProb[j] = Math.log(this.classCount[j]*1.0/this.trainSize); //先验概率
			}
			for (int j = 0; j < this.featureNum; j++) {
				if (tempEntity.feature[j] > 0) {
					for (int k = 0; k < 2; k++) {
						double prob = 1.0*(this.featureCount[k][j]+this.alpha)
								/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
						postProb[k] += Math.log(prob); //计算对数化后的概率
					}
				}
			}
			int myLabel = 0;
			if (postProb[1] > postProb[0]) {
				myLabel = 1;
			}
			if (tempEntity.label == myLabel) {
				countRight++;
			}
		}
		return countRight;
	}
	
	/**
	 * index在[low,high)之间的样本
	 * 加权信息的，普通Naive Bayes
	 * 返回正确分类的个数
	 * 
	 * @param low
	 * @param high
	 * @return
	 */
	public int testModel2(int low, int high) {
		int countRight = 0;
		for (int i = low; i < high; i++) {
			int index = this.sampleIndex.get(i); //随机化的第i个样本点
			Entity tempEntity = this.entities.get(index);
			double[] postProb = new double [2]; //对数化的后验概率
			for (int j = 0; j < 2; j++) {
				postProb[j] = Math.log(this.classCount[j]*1.0/this.trainSize); //先验概率
			}
			for (int j = 0; j < this.featureNum; j++) {
				if (tempEntity.feature[j] > 0) {
					for (int k = 0; k < 2; k++) {
						double prob = 1.0*(this.featureCount[k][j]+this.alpha)
								/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
						postProb[k] += tempEntity.feature[j] * Math.log(prob); //计算对数化后的概率
					}
				}
			}
			int myLabel = 0;
			if (postProb[1] > postProb[0]) {
				myLabel = 1;
			}
			if (tempEntity.label == myLabel) {
				countRight++;
			}
		}
		return countRight;
	}
	
	/**
	 * index在[low,high)之间的样本
	 * 不带加权信息的，但带负面信息的Naive Bayes
	 * 返回正确分类的个数
	 * 
	 * @param low
	 * @param high
	 * @return
	 */
	public int testModel3(int low, int high) {
		int countRight = 0;
		for (int i = low; i < high; i++) {
			int index = this.sampleIndex.get(i); //随机化的第i个样本点
			Entity tempEntity = this.entities.get(index);
			double[] postProb = new double [2]; //对数化的后验概率
			for (int j = 0; j < 2; j++) {
				postProb[j] = Math.log(this.classCount[j]*1.0/this.trainSize); //先验概率
			}
			for (int j = 0; j < this.featureNum; j++) {
				if (tempEntity.feature[j] > 0) {
					for (int k = 0; k < 2; k++) {
						double prob = 1.0*(this.featureCount[k][j]+this.alpha)
								/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
						postProb[k] += Math.log(prob); //计算对数化后的概率
					}
				} else { //没有出现该词的时候利用反面信息
					for (int k = 0; k < 2; k++) {
						double prob = 1.0*(this.classCount[k]-this.featureCount[k][j]+this.alpha)
								/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
						postProb[k] += Math.log(prob);
					}
				}
			}
			int myLabel = 0;
			if (postProb[1] > postProb[0]) {
				myLabel = 1;
			}
			if (tempEntity.label == myLabel) {
				countRight++;
			}
		}
		return countRight;
	}
	
	public static void main(String[] args) {
		NaiveBayes bayes = new NaiveBayes();
		bayes.trainModel();
		//int low = 0;
		//int high = bayes.trainSize;
		int low = bayes.testStart;
		int high = bayes.docSize;
		int right = bayes.testModel1(low, high);
		System.out.println("correct "+right+" of "+(high-low)+", eta = "+(right*1.0/(high-low)));
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
