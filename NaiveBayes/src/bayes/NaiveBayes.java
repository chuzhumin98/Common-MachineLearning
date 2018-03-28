package bayes;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import indexer.FeatureExtract;

public class NaiveBayes {
	public static final String matrixPath = "output/featurematrix.txt";
	//public static final String matrixPath = "output/featurematrix_nonfilter.txt";
	public int docSize = 64620; //总的文档个数
	public static final int trainSize = 45000; //训练集数据的大小
	public static final int testStart = 45001; //测试集数据开始位置
	public double alpha = 1/Math.sqrt(trainSize); //设置平滑系数
	public ArrayList<Entity> entities = new ArrayList<Entity>(); //存储实体集
	
	public int featureNum; //特征的维度
	public int mailNum; //mail特征的维度
	public int timeNum;
	public int xMailerNum;
	
	public ArrayList<Integer> sampleIndex; //样本的索引，前trainsize个为训练集，testsize之后为测试集
	
	public int[][] featureCount; //记录各类训练集各维度的信息
	public int[][] mailCount; //记录mail特征在各类中的信息
	public int[][] timeCount; 
	public int[][] xMailerCount;
	public int[] classCount; //记录各类训练集总个数，其中0为ham,1为spam
	
	public boolean useMailInfo = true; ////是否使用邮箱信息
	public boolean useTimeInfo = false;
	public boolean useXMailerInfo = true;
	
	public double alphaMail = 6; //邮箱提供信息的权重
	public double alphaTime = 1;
	public double alphaXMailer = 8;
	
	public NaiveBayes() {
		this.LoadMatrix();
		this.loadOtherFeature(FeatureExtract.mailPath, 0);
		this.loadOtherFeature(FeatureExtract.timePath, 1);
		this.loadOtherFeature(FeatureExtract.xMailerPath, 2);
		this.mailNum = this.entities.get(0).mail.length;
		this.timeNum = this.entities.get(0).time.length;
		this.xMailerNum = this.entities.get(0).xMailer.length;
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
			System.out.println("total doc size:"+this.docSize);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 加载提取出来的其他信息的矩阵
	 * type:0为mail,1为time,2为xMailer
	 * 
	 * @param path
	 * @param matrix
	 */
	public void loadOtherFeature(String path, int type) {
		try {
			Scanner input = new Scanner(new File(path));
			int index = 0; //现在的doc索引号
			int length = 0; //特征的长度
			while (input.hasNextLine()) {
				String line = input.nextLine();
				String[] splits = line.split(" ");
				if (splits.length > 0) {
					if (type == 0) {
						this.entities.get(index).mail = new int [splits.length];
						for (int i = 0; i < splits.length; i++) {
							this.entities.get(index).mail[i] = Integer.valueOf(splits[i]);
						}
					} else if (type == 1) {
						this.entities.get(index).time = new int [splits.length];
						for (int i = 0; i < splits.length; i++) {
							this.entities.get(index).time[i] = Integer.valueOf(splits[i]);
						}
					} else {
						this.entities.get(index).xMailer = new int [splits.length];
						for (int i = 0; i < splits.length; i++) {
							this.entities.get(index).xMailer[i] = Integer.valueOf(splits[i]);
						}
					}
					
					
					
					index++;
				}
			}
			input.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
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
		this.mailCount = new int [2][this.mailNum];
		this.timeCount = new int [2][this.timeNum];
		this.xMailerCount = new int [2][this.xMailerNum];
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
			int myLabel = tempEntity.label;
			this.classCount[myLabel]++; //对应的类型文档数加一
			for (int j = 0; j < this.featureNum; j++) {
				if (tempEntity.feature[j] > 0) { 
					this.featureCount[myLabel][j]++; //该为特征中有值则记入
				}
			}
			for (int j = 0; j < this.mailNum; j++) {
				if (tempEntity.mail[j] > 0) {
					this.mailCount[myLabel][j]++;
				}
			}
			for (int j = 0; j < this.timeNum; j++) {
				if (tempEntity.time[j] > 0) {
					this.timeCount[myLabel][j]++;
				}
			}
			for (int j = 0; j < this.xMailerNum; j++) {
				if (tempEntity.xMailer[j] > 0) {
					this.xMailerCount[myLabel][j]++;
				}
			}
		}
		//训练结果输出展示
		System.out.println("train spam doc size:"+this.classCount[1]);
		System.out.println("train ham doc size:"+this.classCount[0]);
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
	public int[][] testModel1(int low, int high) {
		int countRight = 0;
		int[][] infiniteCount = new int [3][2]; //第一维：0-仅ham P=0,1~仅spam P=0,2~P均为0; 第二维：0-总数,1-分对的个数
		int[][] evaluateTable = new int [2][2]; //第一维记录真实类别，第二维记录分类类别，0为ham，1为spam
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				evaluateTable[i][j] = 0;
			}
		}
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
			if (this.useMailInfo) {
				for (int j = 0; j < this.mailNum; j++) {
					if (tempEntity.mail[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = (this.mailCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += this.alphaMail*Math.log(prob); //计算对数化后的概率
						}
					}
				}
			}
			if (this.useTimeInfo) {
				for (int j = 0; j < this.timeNum; j++) {
					if (tempEntity.time[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = (this.timeCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += this.alphaTime*Math.log(prob); //计算对数化后的概率
						}
					}
				}
			}
			if (this.useXMailerInfo) {
				for (int j = 0; j < this.xMailerNum; j++) {
					if (tempEntity.xMailer[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = (this.xMailerCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += this.alphaXMailer*Math.log(prob); //计算对数化后的概率
						}
					}
				}
			}
			int myLabel = 0;
			if (postProb[1] > postProb[0]) {
				myLabel = 1;
			}
			evaluateTable[tempEntity.label][myLabel]++; //向表中计数
			if (this.alpha < 1e-14) {
				//System.out.println(postProb[0] + " " + postProb[1]);
				int index1 = -1;
				if (postProb[0] == Double.NEGATIVE_INFINITY && postProb[1] == Double.NEGATIVE_INFINITY) {
					index1 = 2;
				} else if (postProb[0] == Double.NEGATIVE_INFINITY) {
					index1 = 0;
				} else if (postProb[1] == Double.NEGATIVE_INFINITY) {
					index1 = 1;
				}
				if (index1 >= 0) {
					infiniteCount[index1][0]++;
					if (tempEntity.label == myLabel) {
						infiniteCount[index1][1]++;
					}
				}
			}
			//System.out.println(postProb[0]+" "+postProb[1]);
			if (tempEntity.label == myLabel) {
				countRight++;
			}
		}
		if (this.alpha < 1e-14) {
			System.out.println("only P(.|ham)=0, Num: "+infiniteCount[0][0]
					+" , accuracy = "+(1.0*infiniteCount[0][1]/infiniteCount[0][0]));
			System.out.println("only P(.|spam)=0, Num: "+infiniteCount[1][0]
					+" , accuracy = "+(1.0*infiniteCount[1][1]/infiniteCount[1][0]));
			System.out.println("both P(.|ham/spam)=0, Num: "+infiniteCount[2][0]
					+" , accuracy = "+(1.0*infiniteCount[2][1]/infiniteCount[2][0]));
		}
		return evaluateTable;
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
	public int[][] testModel2(int low, int high) {
		int countRight = 0;
		int[][] evaluateTable = new int [2][2]; //第一维记录真实类别，第二维记录分类类别，0为ham，1为spam
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				evaluateTable[i][j] = 0;
			}
		}
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
			if (this.useMailInfo) {
				for (int j = 0; j < this.mailNum; j++) {
					if (tempEntity.mail[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = (this.mailCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += this.alphaMail*Math.log(prob); //计算对数化后的概率
						}
					}
				}
			}
			if (this.useTimeInfo) {
				for (int j = 0; j < this.timeNum; j++) {
					if (tempEntity.time[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = (this.timeCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += this.alphaTime*Math.log(prob); //计算对数化后的概率
						}
					}
				}
			}
			if (this.useXMailerInfo) {
				for (int j = 0; j < this.xMailerNum; j++) {
					if (tempEntity.xMailer[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = (this.xMailerCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += this.alphaXMailer*Math.log(prob); //计算对数化后的概率
						}
					}
				}
			}
			int myLabel = 0;
			if (postProb[1] > postProb[0]) {
				myLabel = 1;
			}
			evaluateTable[tempEntity.label][myLabel]++; //向表中计数
			if (tempEntity.label == myLabel) {
				countRight++;
			}
		}
		return evaluateTable;
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
	public int[][] testModel3(int low, int high) {
		int countRight = 0;
		int[][] evaluateTable = new int [2][2]; //第一维记录真实类别，第二维记录分类类别，0为ham，1为spam
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				evaluateTable[i][j] = 0;
			}
		}
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
			if (this.useMailInfo) {
				for (int j = 0; j < this.mailNum; j++) {
					if (tempEntity.mail[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = (this.mailCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += this.alphaMail*Math.log(prob); //计算对数化后的概率
						}
					}
				}
			}
			if (this.useTimeInfo) {
				for (int j = 0; j < this.timeNum; j++) {
					if (tempEntity.time[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = (this.timeCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += this.alphaTime*Math.log(prob); //计算对数化后的概率
						}
					}
				}
			}
			if (this.useXMailerInfo) {
				for (int j = 0; j < this.xMailerNum; j++) {
					if (tempEntity.xMailer[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = (this.xMailerCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += this.alphaXMailer*Math.log(prob); //计算对数化后的概率
						}
					}
				}
			}
			int myLabel = 0;
			if (postProb[1] > postProb[0]) {
				myLabel = 1;
			}
			evaluateTable[tempEntity.label][myLabel]++; //向表中计数
			if (tempEntity.label == myLabel) {
				countRight++;
			}
		}
		return evaluateTable;
	}
	
	/**
	 * 在Model1的基础上改进的版本，可变调节alpha
	 * index在[low,high)之间的样本
	 * 不带加权信息的，普通Naive Bayes
	 * 返回正确分类的个数
	 * 
	 * @param low
	 * @param high
	 * @return
	 */
	public int[][] testModel1_1(int low, int high) {
		int countRight = 0;
		int[][] infiniteCount = new int [3][2]; //第一维：0-仅ham P=0,1~仅spam P=0,2~P均为0; 第二维：0-总数,1-分对的个数
		int[][] evaluateTable = new int [2][2]; //第一维记录真实类别，第二维记录分类类别，0为ham，1为spam
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				evaluateTable[i][j] = 0;
			}
		}
		for (int i = low; i < high; i++) {
			int myLabel = 0; //该样本所属的类别
			int index = this.sampleIndex.get(i); //随机化的第i个样本点
			Entity tempEntity = this.entities.get(index);
			double[] postProb = new double [2]; //对数化的后验概率
			for (int j = 0; j < 2; j++) {
				postProb[j] = Math.log(this.classCount[j]*1.0/this.trainSize); //先验概率
			}
			boolean[] hasZero = {false, false}; //记录两个类别是否存在含有概率为0的项
			for (int j = 0; j < this.featureNum; j++) {
				if (tempEntity.feature[j] > 0) {
					for (int k = 0; k < 2; k++) {
						if (this.featureCount[k][j] == 0) {
							hasZero[k] = true;
						}
					}
				}	
			}
			if (hasZero[0] && !hasZero[1]) {
				myLabel = 1;
			} else if (!hasZero[0] && hasZero[1]) {
				
			} else {
				for (int j = 0; j < this.featureNum; j++) {
					if (tempEntity.feature[j] > 0) {
						for (int k = 0; k < 2; k++) {
							double prob = 1.0*(this.featureCount[k][j]+this.alpha)
									/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
							postProb[k] += Math.log(prob); //计算对数化后的概率
						}
					}
				}
				if (this.useMailInfo) {
					for (int j = 0; j < this.mailNum; j++) {
						if (tempEntity.mail[j] > 0) {
							for (int k = 0; k < 2; k++) {
								double prob = (this.mailCount[k][j]+this.alpha)
										/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
								postProb[k] += this.alphaMail*Math.log(prob); //计算对数化后的概率
							}
						}
					}
				}
				if (this.useTimeInfo) {
					for (int j = 0; j < this.timeNum; j++) {
						if (tempEntity.time[j] > 0) {
							for (int k = 0; k < 2; k++) {
								double prob = (this.timeCount[k][j]+this.alpha)
										/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
								postProb[k] += this.alphaTime*Math.log(prob); //计算对数化后的概率
							}
						}
					}
				}
				if (this.useXMailerInfo) {
					for (int j = 0; j < this.xMailerNum; j++) {
						if (tempEntity.xMailer[j] > 0) {
							for (int k = 0; k < 2; k++) {
								double prob = (this.xMailerCount[k][j]+this.alpha)
										/(this.classCount[k]+NaiveBayes.trainSize*this.alpha); //平滑后的概率
								postProb[k] += this.alphaXMailer*Math.log(prob); //计算对数化后的概率
							}
						}
					}
				}
				if (postProb[1] > postProb[0]) {
					myLabel = 1;
				}
			}
			evaluateTable[tempEntity.label][myLabel]++; //向表中计数
			if (this.alpha < 1e-14) {
				//System.out.println(postProb[0] + " " + postProb[1]);
				int index1 = -1;
				if (postProb[0] == Double.NEGATIVE_INFINITY && postProb[1] == Double.NEGATIVE_INFINITY) {
					index1 = 2;
				} else if (postProb[0] == Double.NEGATIVE_INFINITY) {
					index1 = 0;
				} else if (postProb[1] == Double.NEGATIVE_INFINITY) {
					index1 = 1;
				}
				if (index1 >= 0) {
					infiniteCount[index1][0]++;
					if (tempEntity.label == myLabel) {
						infiniteCount[index1][1]++;
					}
				}
			}
			//System.out.println(postProb[0]+" "+postProb[1]);
			if (tempEntity.label == myLabel) {
				countRight++;
			}
		}
		if (this.alpha < 1e-14) {
			System.out.println("only P(.|ham)=0, Num: "+infiniteCount[0][0]
					+" , accuracy = "+(1.0*infiniteCount[0][1]/infiniteCount[0][0]));
			System.out.println("only P(.|spam)=0, Num: "+infiniteCount[1][0]
					+" , accuracy = "+(1.0*infiniteCount[1][1]/infiniteCount[1][0]));
			System.out.println("both P(.|ham/spam)=0, Num: "+infiniteCount[2][0]
					+" , accuracy = "+(1.0*infiniteCount[2][1]/infiniteCount[2][0]));
		}
		return evaluateTable;
	}
	
	public static void main(String[] args) {
		NaiveBayes bayes = new NaiveBayes();
		bayes.trainModel();
		//int low = 0;
		//int high = bayes.trainSize;
		int low = bayes.testStart;
		int high = bayes.docSize;
		int[][] table = bayes.testModel1(low, high); //采用模型1进行预测
		double accuracy = 1.0 * (table[0][0]+table[1][1]) / (table[0][0]+table[0][1]+table[1][0]+table[1][1]);
		double precision = 1.0 * table[1][1] / (table[0][1]+table[1][1]);
		double recall = 1.0 * table[1][1] / (table[1][0]+table[1][1]);
		double f1Measure = 2.0*precision*recall/(precision+recall);
		System.out.println("test doc size:"+(high-low));
		System.out.println("model accuracy = "+accuracy);
		System.out.println("model precision = "+precision);
		System.out.println("model recall = "+recall);
		System.out.println("model F1-Measure = "+f1Measure);
	}
	
	public class Entity {
		public int[] feature; //特征向量
		public int[] mail;
		public int[] time;
		public int[] xMailer;
		public int label; //实体的标签，1为spam，0为ham
		public Entity(int[] feature1, int label1) {
			this.feature = feature1;
			this.label = label1;
		}
	}
}
