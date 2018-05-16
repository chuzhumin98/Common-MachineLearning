from sklearn import tree
from sklearn import svm
from sklearn.ensemble import RandomForestClassifier
from sklearn.naive_bayes import MultinomialNB
from sklearn.neighbors import KNeighborsClassifier
from sklearn.linear_model import LogisticRegression
import MLP


# 采用random forest算法进行预测
#    state:采用的森林的大小
def RandomForest(trainMatrix, trainLabels, testMatrix, state = 10):
    clf = RandomForestClassifier(oob_score=True, random_state=state)
    clf.fit(trainMatrix, trainLabels)
    return clf.predict(testMatrix)

# 采用带深度限制剪枝的决策树进行预测
def DecisionTree(trainMatrix, trainLabels, testMatrix, maxDepth=26):
    clf = tree.DecisionTreeClassifier(max_depth=maxDepth)
    clf.fit(trainMatrix, trainLabels)
    return clf.predict(testMatrix)

#朴素贝叶斯分类器
def NaiveBayes(trainMatrix, trainLabels, testMatrix, alpha=0.01):
    clf = MultinomialNB(alpha=alpha)
    clf.fit(trainMatrix, trainLabels)
    return clf.predict(testMatrix)

#采用KNN方法进行预测
def KNN(trainMatrix, trainLabels, testMatrix):
    clf = KNeighborsClassifier()
    clf.fit(trainMatrix, trainLabels)
    return clf.predict(testMatrix)

#采用logistic回归进行预测
def Logistic(trainMatrix, trainLabels, testMatrix):
    clf = LogisticRegression(penalty='l2')
    clf.fit(trainMatrix, trainLabels)
    return clf.predict(testMatrix)

#采用SVM进行分类
def SVM(trainMatrix, trainLabels, testMatrix):
    clf = svm.LinearSVC()
    clf.fit(trainMatrix, trainLabels)
    return clf.predict(testMatrix)

#采用SVR进行回归预测
def SVR(trainMatrix, trainLabels, testMatrix):
    clf = svm.LinearSVR()
    clf.fit(trainMatrix, trainLabels)
    return clf.predict(testMatrix)

#根据不同的参数method来挑选采用的分类模型
#    method取值：0-决策树，1-SVM，2-SVR，3-Naive Bayes，4-KNN，5-Logistic Regression，6-MLP,other-随机森林
def chooseMethod(trainMatrix, trainLabels, testMatrix, method):
    if (method == 0):
        return DecisionTree(trainMatrix, trainLabels, testMatrix, maxDepth=None)
    elif (method == 1):
        return SVM(trainMatrix, trainLabels, testMatrix)
    elif (method == 2):
        return SVR(trainMatrix, trainLabels, testMatrix)
    elif (method == 3):
        return NaiveBayes(trainMatrix, trainLabels, testMatrix)
    elif (method == 4):
        return KNN(trainMatrix, trainLabels, testMatrix)
    elif (method == 5):
        return Logistic(trainMatrix, trainLabels, testMatrix)
    elif (method == 6):
        return MLP.MLPmethod(trainMatrix, trainLabels, testMatrix)
    else:
        return RandomForest(trainMatrix, trainLabels, testMatrix)