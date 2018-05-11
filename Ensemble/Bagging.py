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
        RMSE = math.sqrt(np.mean(np.square(np.subtract(label,predictResult))))
        return RMSE

#随机划分训练集和验证集
#    参数samples：所有样本数据
#    参数samplesLabels：样本所对应的标签
#    return：[训练集数据, 训练集标签, 验证集数据, 验证集标签]
def splitDatas(samples, samplesLabels):
    size = len(samplesLabels) #总的样本点个数
    indexArray = np.array(range(size), dtype=int) #下标数组
    np.random.shuffle(indexArray)
    validateStart = size * 4 // 5 #按照4:1的比例划分划分训练集和验证集
    trainData = samples[indexArray[0:validateStart]]
    trainLabel = samplesLabels[indexArray[0:validateStart]]
    validateData = samples[indexArray[validateStart:size]]
    validateLabel = samplesLabels[indexArray[validateStart:size]]
    return [trainData, trainLabel, validateData, validateLabel]


if __name__ == '__main__':
    # 导入数据部分
    trainFilePath = 'exp2.train.csv'
    df1 = pd.read_csv(trainFilePath, encoding='utf-8')
    trainLabels = np.array(df1['label'])
    trainAppearMatrix = loadData('matrix1000.npy')
    trainAppearMatrix, trainLabels, validateAppearMatrix, validateLabels = splitDatas(trainAppearMatrix, trainLabels)
    testAppearMatrix = loadData('testMatrix1000.npy')
    print('succeed load data')
    # 进行模型训练训练和预测部分
    clf = tree.DecisionTreeClassifier()
    clf = clf.fit(trainAppearMatrix, trainLabels)
    predictResult = clf.predict(validateAppearMatrix)
    print(predictResult)
    print(validateLabels)
    print('RMSE in validateSet:', evaluateResult(validateLabels, predictResult))
    # 结果导出部分
    predictResult = clf.predict(testAppearMatrix)
    exportResult(predictResult, 'result/decision_tree_baseline1000_0_8.csv')


