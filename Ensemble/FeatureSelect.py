import pandas as pd
import numpy as np

def recordTotalWords():
    trainFilePath = 'exp2.train.csv'
    df1 = pd.read_csv(trainFilePath, encoding='utf-8')
    words = []  # 初始的词汇列表
    for i in range(len(df1)):
        splitWords = str(df1.loc[i, 'review']).split(' ')
        for j in range(len(splitWords)):
            if splitWords[j] not in words:
                words.append(splitWords[j])
    print(words)
    print(len(words))
    f = open("totalWords.npy", "wb")
    np.save(f, words)
    f.close()

if __name__ == '__main__':
    recordTotalWords()