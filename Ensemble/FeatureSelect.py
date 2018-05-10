import pandas as pd
import numpy as np

# 记录所有的词项
#    dataframe:输入的csv数据
def recordTotalWords(dataframe):
    words = []  # 初始的词汇列表
    for i in range(len(dataframe)):
        splitWords = str(dataframe.loc[i, 'review']).split(' ')
        for j in range(len(splitWords)):
            if splitWords[j] not in words:
                words.append(splitWords[j])
    print(words)
    print(len(words))
    # 将数据导出到文件中
    f = open("totalWords.npy", "wb")
    np.save(f, words)
    f.close()


#在所有词项中挑选出top-k信息量的词汇
#    dataframe:输入的csv文件
#    k:所导出的词汇数目
def selectTopWords(dataframe, k):
    f = open("totalWords.npy", "rb")
    totalWords = np.load(f)
    f.close()
    labelCounts = np.zeros([3]) #记录各类别出现的总次数，分别为标签1,0，-1
    labelCounts[0] = len(dataframe.loc[(dataframe['label'] == 1)])
    labelCounts[1] = len(dataframe.loc[(dataframe['label'] == 0)])
    labelCounts[2] = len(dataframe.loc[(dataframe['label'] == -1)])
    totalAverage = (labelCounts[0] - labelCounts[2]) / len(totalWords)
    print(labelCounts)
    print(totalAverage)
    infoGains = [] #计算各词汇的信息量(采用标签delta法计算信息量）
    for i in range(len(totalWords)):
        #print(totalWords[i])
        countIn = np.zeros([3])
        try:
            dfTemp = dataframe.loc[(dataframe['review'].str.contains(totalWords[i]))]
            countIn[0] = len(dfTemp.loc[(dataframe['label'] == 1)])
            countIn[1] = len(dfTemp.loc[(dataframe['label'] == 0)])
            countIn[2] = len(dfTemp.loc[(dataframe['label'] == -1)])
        except:
            countIn = np.zeros([3])
            print('error in word ',totalWords[i])
        totalNum = sum(countIn)
        if (totalNum == 0):
            infoGains.append(0)
        else:
            averageIn = (countIn[0] - countIn[2]) / totalNum  # 含有这一类的平均得分
            deltaScore = abs(averageIn - totalAverage)
            infoGains.append(deltaScore * totalNum)
        if ((i+1) % 100 == 0):
            print(infoGains[-100:])
            print('complete ',(i+1), ' of ',len(totalWords))
    infoGains = np.array(infoGains)
    topkIndex = np.argpartition(-infoGains, k-1)[0:k] #找到topk信息量的词汇
    usefulWords = totalWords[topkIndex]
    # 将数据导出到文件中
    f = open("usefulWords1000.npy", "wb")
    np.save(f, usefulWords)
    f.close()
    return usefulWords


# 导入有用的词汇
def loadData(path):
    f = open(path, "rb")
    data = np.load(f)
    f.close()
    return data


# 计算词项矩阵
def calculateAppearMatrix(usefulWords, dataframe, outPath):
    size = len(dataframe)
    k = len(usefulWords)
    AppearMatrix = np.zeros([size, k])
    for i in range(len(usefulWords)):
        wordInIndex = dataframe['review'].str.contains(usefulWords[i])
        AppearMatrix[wordInIndex,i] = 1
    print(AppearMatrix)
    # 将数据导出到文件中
    f = open(outPath, "wb")
    np.save(f, AppearMatrix)
    f.close()

if __name__ == '__main__':
    trainFilePath = 'exp2.train.csv'
    testFilePath = 'exp2.validation_review.csv'
    df1 = pd.read_csv(trainFilePath, encoding='utf-8')
    #recordTotalWords(df1)
    #usefulWords = selectTopWords(df1, 1000)
    usefulWords = loadData('usefulWords200.npy')
    #calculateAppearMatrix(usefulWords, df1, 'matrix1000.npy')
    df2 = pd.read_csv(testFilePath, encoding='utf-8')
    calculateAppearMatrix(usefulWords, df2, 'testMatrix200.npy')