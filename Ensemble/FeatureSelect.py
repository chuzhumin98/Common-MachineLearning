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
    print(labelCounts)
    for i in range(len(totalWords)):
        dfTemp = dataframe.loc[(dataframe['review'].str.contains(totalWords[i]))]
        countIn = np.zeros([3])
        print(len(dfTemp.loc[(dataframe['label'] == 1)]))
        print(len(dfTemp.loc[(dataframe['label'] == 0)]))
        print(len(dfTemp.loc[(dataframe['label'] == -1)]))
    print(totalWords)
    print(len(totalWords))

if __name__ == '__main__':
    trainFilePath = 'exp2.train.csv'
    df1 = pd.read_csv(trainFilePath, encoding='utf-8')
    #recordTotalWords(df1)
    selectTopWords(df1, 100)