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