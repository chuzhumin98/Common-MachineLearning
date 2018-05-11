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
    weight = np.ones(len(trainLabels))/len(trainLabels)
    print(weight)
    for i in range(T):
        if (i == 0):
            clf = tree.DecisionTreeClassifier()


if __name__ == '__main__':
    # 导入数据部分
    trainFilePath = 'exp2.train.csv'
    df1 = pd.read_csv(trainFilePath, encoding='utf-8')
    trainLabels0 = np.array(df1['label'])
    trainAppearMatrix0 = loadData('matrix200.npy')
    testAppearMatrix = loadData('testMatrix200.npy')
    print('succeed load data')
    # 进行模型训练训练和预测部分
    trainAppearMatrix, trainLabels, validateAppearMatrix, validateLabels = splitDatas(trainAppearMatrix0, trainLabels0)
    AdaBoost(trainAppearMatrix, trainLabels, validateAppearMatrix, 5, 0) #使用AdaBoost进行分类