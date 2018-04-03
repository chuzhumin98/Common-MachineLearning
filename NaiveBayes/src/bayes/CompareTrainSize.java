package bayes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class CompareTrainSize {
	public NaiveBayes bayes; //载入Naive Bayes模型
	
	public CompareTrainSize() {
		this.bayes = new NaiveBayes();
	}
	
	/**
	 * 以某个固定的训练集大小重复若干次实验
	 * 返回错误率结果
	 * 
	 * @param trainSize
	 * @param times
	 * @return
	 */
	public double[] computeError(int trainSize, int times) {
		double[] error5 = new double [times];
		NaiveBayes.setTrainSize(trainSize);
		for (int i = 0; i < times; i++) {
			bayes.shuffleSample(); //重洗牌数据
			bayes.trainModel();
			int[][] table = bayes.testModel1(NaiveBayes.testStart, bayes.docSize);
			double error = 1.0 * (table[0][1]+table[1][0]) / (table[0][0]+table[0][1]+table[1][0]+table[1][1]);
			error5[i] = error;
			System.out.println("No. "+(i+1)+": "+error);
		}
		return error5;
	}
	
	/**
	 * 将错误率输出到文件中
	 * 
	 * @param a
	 * @param b
	 */
	public void outputError(double[] a, double[] b, String path) {
		try {
			PrintStream output = new PrintStream(new File(path));
			for (int i = 0; i < Math.min(a.length, b.length); i++) {
				output.println(a[i]+" "+b[i]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		CompareTrainSize compare = new CompareTrainSize();
		int times = 1000;
		int size5 = compare.bayes.docSize / 20;
		System.out.println("对5%训练集：");
		double[] result5 = compare.computeError(size5, times);
		int size50 = compare.bayes.docSize / 2;
		System.out.println("对50%训练集：");
		double[] result50 = compare.computeError(size50, times);
		compare.outputError(result5, result50, "output/compare.txt");
	}
}
