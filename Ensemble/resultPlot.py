import matplotlib.pyplot as plt
import pandas as pd


if __name__ == '__main__':
    resultPath = 'DT.txt'
    df1 = pd.read_csv(resultPath, encoding='utf-8')
    print(df1)

    plt.figure(0)
    plt.plot(df1['depth'], df1['train'], c='b')
    plt.plot(df1['depth'], df1['RMSE'], c='r')
    plt.xlabel('max_depth')
    plt.ylabel('RMSE')
    plt.title('RMSE vs max_depth')
    plt.legend(['train data', 'validate data'])
    plt.savefig('image/RMSE vs depth.png', dpi=150)