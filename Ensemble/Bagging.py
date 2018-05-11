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

# 实现bagging算法
#    times：bootstrap的次数
def baggingDT(trainMatrix, trainLabels, testMatrix, times):
    predictResultSamples = np.zeros([times, len(testMatrix)])
    for i in range(times):
        bootstrapIndexs = np.random.randint(low=0, high=len(trainLabels), size=len(trainLabels))
        print(bootstrapIndexs)
        clf = tree.DecisionTreeClassifier()
        clf = clf.fit(trainMatrix, trainLabels)
        predictResultSamples[i,:] = clf.predict(testMatrix)
    predictResult = np.mean(predictResultSamples, axis=0)
    return predictResult

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
    #predictResult = baggingDT(trainAppearMatrix, trainLabels, validateAppearMatrix, 10)
    #print('RMSE in validateSet:', evaluateResult(validateLabels, predictResult))
    # 结果导出部分
    predictResult = baggingDT(trainAppearMatrix, trainLabels, testAppearMatrix, 10)
    exportResult(predictResult, 'result/decision_tree_20bagging1000_0_8.csv')


