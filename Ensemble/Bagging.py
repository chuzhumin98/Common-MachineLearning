from FeatureSelect import loadData
from usefulMethod import *
from commonFunction import *


# 实现决策树的bagging算法
#    times：bootstrap的次数
def baggingDT(trainMatrix, trainLabels, testMatrix, times, maxDepth = 26):
    predictResultSamples = np.zeros([times, len(testMatrix)])
    for i in range(times):
        bootstrapIndexs = np.random.randint(low=0, high=len(trainLabels), size=len(trainLabels))
        print(i,':',bootstrapIndexs)
        clf = tree.DecisionTreeClassifier(max_depth=maxDepth)
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


# 普适的bagging算法
#    methodLists:Bagging的方法向量
def commonBagging(trainMatrix, trainLabels, testMatrix, methodLists):
    predictResultSamples = np.zeros([len(methodLists), len(testMatrix)])
    for i in range(len(methodLists)):
        bootstrapIndexs = np.random.randint(low=0, high=len(trainLabels), size=len(trainLabels))
        print(i, ':', bootstrapIndexs)
        bootstrapTrainMatrix = trainMatrix[bootstrapIndexs]
        bootstrapTrainLabels = trainLabels[bootstrapIndexs]
        predictResultSamples[i, :] = chooseMethod(bootstrapTrainMatrix, bootstrapTrainLabels, testMatrix, methodLists[i])
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

    #这个for循环用于剪枝深度的选取
    """
    for i in range(30,45):
        predictResult = baggingDT(trainAppearMatrix, trainLabels, validateAppearMatrix, 1, i)
        print('RMSE in validateSet with depth = ',i,':', evaluateResult(validateLabels, predictResult))
        predictResult1 = baggingDT(trainAppearMatrix, trainLabels, trainAppearMatrix, 1, i)
        print('RMSE in trainSet with depth = ',i,':', evaluateResult(trainLabels, predictResult1))
    """
    predictResult = baggingDT(trainAppearMatrix, trainLabels, validateAppearMatrix, 20, maxDepth=26)
    print('RMSE in validateSet:', evaluateResult(validateLabels, predictResult))
    methodLists = [1, 2, 3, 4, 5, 6]
    predictResult = commonBagging(trainAppearMatrix, trainLabels, validateAppearMatrix, methodLists)
    print('RMSE in validateSet:', evaluateResult(validateLabels, predictResult))
    # 采用单一方法进行测试部分
    #predictResult = RandomForest(trainAppearMatrix, trainLabels, validateAppearMatrix, 50)
    #predictResult = DecisionTree(trainAppearMatrix0, trainLabels0, testAppearMatrix, maxDepth=26)
    #predictResult = NaiveBayes(trainAppearMatrix, trainLabels, validateAppearMatrix)
    #predictResult = KNN(trainAppearMatrix, trainLabels, validateAppearMatrix)
    #predictResult = Logistic(trainAppearMatrix, trainLabels, validateAppearMatrix)
    #predictResult = SVM(trainAppearMatrix0, trainLabels0, testAppearMatrix)
    #predictResult = SVR(trainAppearMatrix0, trainLabels0, testAppearMatrix)
    #print('RMSE in validateSet:', evaluateResult(validateLabels, predictResult))
    #exportResult(predictResult, 'result/SVR_v1.csv')
    # 结果导出部分
    predictResult = baggingDT(trainAppearMatrix0, trainLabels0, testAppearMatrix, 20, maxDepth=50)
    exportResult(predictResult, 'result/decision_tree_bagging_20_prunning50_v1.csv')
    methodLists = [0,1,2,3,5,6,7]
    predictResult = commonBagging(trainAppearMatrix0, trainLabels0, testAppearMatrix, methodLists)
    exportResult(predictResult, 'result/bagging[0,1,2,3,5,6,7]_v1.csv')
