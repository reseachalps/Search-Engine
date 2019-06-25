import sys

import pandas as pd
from sklearn import linear_model
from sklearn import cross_validation
from sklearn import metrics
import numpy as np


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Please indicate data CSV file")
        sys.exit()
    
    data = pd.read_csv(sys.argv[1])

    X_pos = data[data['class'] == 1]
    X_neg = data[data['class'] == 0]
    X = np.vstack([np.array(X_pos.iloc[:,1:-1]), np.array(X_neg.iloc[:,1:-1])])
    y = np.concatenate([np.array(X_pos['class']), np.array(X_neg['class'])])

    sFolds = cross_validation.StratifiedKFold(y, n_folds=5)

    clf = linear_model.LinearRegression()

    LC_scores = [metrics.accuracy_score(y[test], [p for p in map(lambda x: int(round(x)), clf.fit(X[train], y[train]).predict(X[test]))]) for train, test in sFolds]

    print("CV accuracy score:", np.mean(LC_scores), sep='\t')
    print('\n\n')
    print("Confusion matrix:", metrics.confusion_matrix(y, [p for p in map(lambda x: int(round(x)), clf.fit(X, y).predict(X))]), sep='\n')
    print('\n\n')
    print("Classification report:", metrics.classification_report(y, [p for p in map(lambda x: int(round(x)), clf.fit(X, y).predict(X))]), sep='\n')
    print('\n\n')
    print("Model coefficients:", clf.fit(X, y).coef_, sep='\n')
