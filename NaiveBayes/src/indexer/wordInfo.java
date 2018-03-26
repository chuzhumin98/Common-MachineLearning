package indexer;

/**
 * 存储term相关信息的类，包括在spam、ham类中出现的文档个数，信息增益值
 * 
 * @author chuzhumin
 *
 */
public class WordInfo {
	public int spamNum; //spam类出现在的文档个数
	public int hamNum; //ham类出现在的文档个数
	public Double gain; //该term所带来的信息增益
	public int spamLastIndex; //spam类的上一个文档索引
	public int hamLastIndex; //ham类的上一个文档索引
	public String count; //记录该term总的出现次数
	
	public WordInfo() {
		this.spamNum = 0;
		this.hamNum = 0;
		this.gain = 0.0;
	}
	
	/**
	 * 一个spam类文件第一次出现该词时才计数一次
	 * 
	 * @param index
	 */
	public void spamIncrease(int index) {
		if (index > this.spamLastIndex) {
			this.spamNum++;
			this.spamLastIndex = index;
		}	
	}
	
	public void hamIncrease(int index) {
		if (index > this.hamLastIndex) {
			this.hamNum++;
			this.hamLastIndex = index;
		}
	}
	
	/**
	 * 计算信息增益
	 * 
	 * @param spamTotal
	 * @param hamTotal
	 */
	public void calculateInfoGain(int spamTotal, int hamTotal) {
		double entropy = this.calcalateEntropy(spamTotal, spamTotal+hamTotal);
		double newEntropy = spamTotal * this.calcalateEntropy(this.spamNum, spamTotal) / (spamTotal+hamTotal)
				+ hamTotal * this.calcalateEntropy(this.hamNum, hamTotal) / (spamTotal+hamTotal);
		this.gain = entropy - newEntropy; //信息增益为两者之差
	}
	
	/**
	 * 计算信息熵
	 * 
	 * @param n1
	 * @param N
	 * @return
	 */
	private double calcalateEntropy(int n1, int N) {
		double p = 1.0*n1/N;
		double q = 1 - p;
		double entropy = 0.0;
		if (p > 1e-5) { //防止碰到0/0的情况
			entropy -= p * Math.log(p) / Math.log(2);
		}
		if (q > 1e-5) {
			entropy -= q * Math.log(q) / Math.log(2);
		}
		return entropy;
	}
}