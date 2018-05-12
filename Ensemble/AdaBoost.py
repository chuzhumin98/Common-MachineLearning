from Bagging import loadData
from Bagging import splitDatas
from Bagging import evaluateResult
from Bagging import exportResult
import pandas as pd
import numpy as np
from sklearn import tree
from sklearn import svm

# AdaBoost.M1的实现框架
#    T:AdaBoost中的尝试长度
#    method:0为决策树，1为SVM
def AdaBoost(trainMatrix, trainLabels, testMatrix, T, method):
    weight = np.ones(len(trainLabels))/len(trainLabels) #各样本点的权重，初始时权重相同
    modelWeight = np.zeros([T]) #所得到的各个模型的权重
    models = np.zeros([T, len(testMatrix)]) #各个model对测试集数据的分类结果
    for i in range(T):
        if (method == 0):
            AdaBoostIndex = np.random.choice(len(trainLabels), len(trainLabels), p=weight) #按指定概率抽取样本
            AdaBoostTrainMatrix = trainMatrix[AdaBoostIndex, :] #重采样得到的训练样本
            AdaBoostTrainLabels = trainLabels[AdaBoostIndex] #重采样得到的训练标签
            clf = tree.DecisionTreeClassifier(max_depth=15)
            clf.fit(AdaBoostTrainMatrix, AdaBoostTrainLabels)
            models[i,:] = clf.predict(testMatrix) #记录下来当前这个模型的预测结果
            predictTrainLabels = clf.predict(AdaBoostTrainMatrix) #在训练集上的效果
            deltaPredict = AdaBoostTrainLabels - predictTrainLabels #将真实标签与测试标签进行作差
            misclassifyIndex = deltaPredict[:] != 0
            epsilon = np.sum(weight[misclassifyIndex])
            print(i,':',epsilon)
            if (epsilon >= 0.6666):
                continue #当错误率太大时，则丢弃模型，不更新模型权值
            classifyIndex = deltaPredict[:] == 0
            beta = epsilon / (1-epsilon)
            weight[classifyIndex] = weight[classifyIndex] * beta #修改权重信息






if __name__ == '__main__':
    # 导入数据部分
    trainFilePath = 'exp2.train.csv'
    df1 = pd.read_csv(trainFilePath, encoding='utf-8')
    trainLabels0 = np.array(df1['label'])
    trainAppearMatrix0 = loadData('matrix1000.npy')
    testAppearMatrix = loadData('testMatrix1000.npy')
    print('succeed load data')
    # 进行模型训练训练和预测部分
    trainAppearMatrix, trainLabels, validateAppearMatrix, validateLabels = splitDatas(trainAppearMatrix0, trainLabels0)
    AdaBoost(trainAppearMatrix, trainLabels, validateAppearMatrix, 5, 0) #使用AdaBoost进行分类