from Bagging import loadData
from Bagging import splitDatas
from Bagging import evaluateResult
from Bagging import exportResult
import pandas as pd
import tensorflow as tf
import numpy as np

def add_layer(inputs, in_size, out_size, activation_function=None):
    # add one more layer and return the output of this layer
    Weights = tf.Variable(tf.random_normal([in_size, out_size]))
    biases = tf.Variable(tf.zeros([1, out_size]))
    Wx_plus_b = tf.matmul(inputs, Weights) + biases
    if activation_function is None:
        outputs = Wx_plus_b
    else:
        outputs = activation_function(Wx_plus_b)
    return outputs

if __name__ == '__main__':
    # 导入数据部分
    trainFilePath = 'exp2.train.csv'
    df1 = pd.read_csv(trainFilePath, encoding='utf-8')
    trainLabels0 = np.array(df1['label'])
    trainAppearMatrix0 = loadData('matrix1000.npy')
    testAppearMatrix = loadData('testMatrix1000.npy')
    print('succeed load data')
    # 进行模型训练训练和预测部分
    #trainAppearMatrix, trainLabels, validateAppearMatrix, validateLabels = splitDatas(trainAppearMatrix0, trainLabels0)
    trainLabels0 = np.transpose([trainLabels0])
    # define placeholder for inputs to network
    xs = tf.placeholder(tf.float32, [None, 1000])
    ys = tf.placeholder(tf.float32, [None, 1])

    # add hidden layer
    l1 = add_layer(xs, 1000, 100, activation_function=tf.nn.tanh)
    # add output layer
    prediction = add_layer(l1, 100, 1, activation_function=tf.nn.tanh)

    # the error between prediciton and real data
    loss = tf.reduce_mean(tf.reduce_sum(tf.square(ys - prediction),
                                        reduction_indices=[1]))
    global_step = tf.Variable(0)
    learing_rate = tf.train.exponential_decay(0.3, global_step, 500, 0.96, staircase=False)
    #train_step = tf.train.GradientDescentOptimizer(lam).minimize(loss)
    optimizer = tf.train.MomentumOptimizer(learing_rate, 0.1).minimize(loss,global_step)

    # init variables
    init = tf.global_variables_initializer()
    sess = tf.Session()
    sess.run(init)

    # learning process
    for i in range(30000):
        indexArray = np.array(range(len(trainLabels0)), dtype=int)  # 下标数组
        np.random.shuffle(indexArray)
        batchTrainMatrix = trainAppearMatrix0[indexArray[:100],:]
        batchTrainLabels = trainLabels0[indexArray[:100],:]
        sess.run(optimizer, feed_dict={xs: batchTrainMatrix, ys: batchTrainLabels})
        if (i + 1) % 200 == 0:
            predictions = (sess.run(prediction, feed_dict={xs: trainAppearMatrix0, ys: trainLabels0}))
            print('#iter ',i+1,' RMSE = ',evaluateResult(trainLabels0, predictions))


    predictResult = (sess.run(prediction, feed_dict={xs: testAppearMatrix, ys: np.zeros([len(testAppearMatrix),1])}))
    print(predictResult)
    predictResult = predictResult[:,0]
    exportResult(predictResult, 'result/MLP_v4_iter30000.csv')