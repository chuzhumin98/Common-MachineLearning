from sklearn import tree
from sklearn import svm
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
    if (len(label) != len(predictLabel)):
        print('error for label and predictLabel is not equavalant length')
        print('type:',type(label),' vs ',type(predictLabel))
        print('label len:', len(label), ', predicted label len:', len(predictLabel))
        return 2
    else:
        RMSE = math.sqrt(np.mean(np.square(np.subtract(label,predictLabel))))
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

# 实现决策树的bagging算法
#    times：bootstrap的次数
def baggingDT(trainMatrix, trainLabels, testMatrix, times):
    predictResultSamples = np.zeros([times, len(testMatrix)])
    for i in range(times):
        bootstrapIndexs = np.random.randint(low=0, high=len(trainLabels), size=len(trainLabels))
        print(i,':',bootstrapIndexs)
        clf = tree.DecisionTreeClassifier(max_depth=1)
        bootstrapTrainMatrix = trainMatrix[bootstrapIndexs]
        bootstrapTrainLabels = trainLabels[bootstrapIndexs]
        clf = clf.fit(bootstrapTrainMatrix, bootstrapTrainLabels)
        predictResultSamples[i,:] = clf.predict(testMatrix)
    predictResult = np.mean(predictResultSamples, axis=0)
    return predictResult

# 实现svr的bagging算法
def baggingSVR(trainMatrix, trainLabels, testMatrix, times):
    predictResultSamples = np.zeros([times, len(testMatrix)])
    for i in range(times):
        bootstrapIndexs = np.random.randint(low=0, high=len(trainLabels), size=len(trainLabels))
        print(i,':',bootstrapIndexs)
        bootstrapTrainMatrix = trainMatrix[bootstrapIndexs]
        bootstrapTrainLabels = trainLabels[bootstrapIndexs]
        clf = svm.LinearSVR()
        clf.fit(bootstrapTrainMatrix, bootstrapTrainLabels)
        predictResultSamples[i, :] = clf.predict(testMatrix)
    predictResult = np.mean(predictResultSamples, axis=0)
    return predictResult


# 实现svm的bagging算法
def baggingSVM(trainMatrix, trainLabels, testMatrix, times):
    predictResultSamples = np.zeros([times, len(testMatrix)])
    trainLabels1 = np.copy(trainLabels) #将0和1划分成一类
    trainLabels1[trainLabels1[:] >= 0] = 1
    trainLabels1[trainLabels1[:] == -1] = -2
    print(trainLabels1)
    trainLabels2 = np.copy(trainLabels) #将0和-1划分为一类
    trainLabels2[trainLabels2[:] <= 0] = -1
    trainLabels2[trainLabels2[:] == 1] = 2
    print(trainLabels2)
    for i in range(times):
        bootstrapIndexs = np.random.randint(low=0, high=len(trainLabels), size=len(trainLabels))
        print(i,':',bootstrapIndexs)
        bootstrapTrainMatrix = trainMatrix[bootstrapIndexs]
        bootstrapTrainLabels1 = trainLabels1[bootstrapIndexs]
        bootstrapTrainLabels2 = trainLabels2[bootstrapIndexs]
        clf = svm.LinearSVC()
        clf.fit(bootstrapTrainMatrix, bootstrapTrainLabels1)
        predict1 = clf.predict(testMatrix) #采用-1和0.5进行分类
        clf.fit(bootstrapTrainMatrix, bootstrapTrainLabels2)
        predict2 = clf.predict(testMatrix) #采用1和-0.5进行分类
        predictResultSamples[i, :] = (predict1+predict2)/3
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
    predictResult = baggingDT(trainAppearMatrix, trainLabels, validateAppearMatrix, 1)
    print('RMSE in validateSet:', evaluateResult(validateLabels, predictResult))
    predictResult1 = baggingDT(trainAppearMatrix, trainLabels, trainAppearMatrix, 1)
    print('RMSE in trainSet:', evaluateResult(trainLabels, predictResult1))
    # 结果导出部分
    #predictResult = baggingSVM(trainAppearMatrix0, trainLabels0, testAppearMatrix, 20)
    #exportResult(predictResult, 'result/SVM_20bagging1000_v2.csv')


