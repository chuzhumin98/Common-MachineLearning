from sklearn import tree
from FeatureSelect import loadData
import pandas as pd
import numpy as np
import math

# 将测试结果写入csv文件
def exportResult(predictResult, path):
    resultArray = np.zeros([len(predictResult), 2])  # 结果矩阵
    resultArray[:, 0] = range(1, len(predictResult) + 1)
    resultArray[:, 1] = predictResult
    resultdf = pd.DataFrame(resultArray, columns=['id', 'label'])
    resultdf['id'] = resultdf['id'].astype('int')
    resultdf.to_csv(path, index=False)


# 对结果按照所给的公式进行评价
def evaluateResult(label, predictLabel):
    if (len(label) != len(predictResult)):
        print('error for label and predictLabel is not equavalant length')
        return 2
    else:
        RMSE = math.sqrt(np.sum(np.square(np.subtract(label-predictResult))/len(label)))
        return RMSE


if __name__ == '__main__':
    # 导入数据部分
    trainFilePath = 'exp2.train.csv'
    df1 = pd.read_csv(trainFilePath, encoding='utf-8')
    trainLabels = df1['label']
    trainAppearMatrix = loadData('matrix200.npy')
    testAppearMatrix = loadData('testMatrix200.npy')
    # 进行模型训练训练和预测部分
    clf = tree.DecisionTreeClassifier()
    clf = clf.fit(trainAppearMatrix, trainLabels)
    predictResult = clf.predict(testAppearMatrix)
    print(predictResult)
    print('error:',np.sum(np.abs(np.subtract(predictResult, df1['label']))))
    # 结果导出部分
    #exportResult(predictResult, 'result/decision_tree_baseline1000.csv')


