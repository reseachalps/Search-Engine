import sys
import os


data = None

def get_most_informative_features(data, features):
    scores = {}
    for f in features:
        trues = [elem['class'] for elem in data if elem[f] == '1']
        falses = [elem['class'] for elem in data if elem[f] == '0']
        score_true = max(trues.count('1'), trues.count('0')) / max(min(trues.count('1'), trues.count('0')), 1)
        score_false = max(falses.count('1'), falses.count('0')) / max(min(falses.count('1'), falses.count('0')), 1)
        scores[f + ' = True'] = ['pos : neg' if trues.count('1') >= trues.count('0') else 'neg : pos', score_true]
        scores[f + ' = False'] = ['pos : neg' if falses.count('1') >= falses.count('0') else 'neg : pos', score_false]
    return scores

if __name__ == '__main__':
    global data
    
    if len(sys.argv) > 1:
        data_filename = sys.argv[1]
    else:
        print("Using default arguments.")
        data_filename = os.path.join(os.path.realpath('.'), 'features.csv')
    data = []
    lines = open(data_filename).readlines()
    keys = [elem for elem in lines[0].strip().split(',')]
    for line in lines[1:]:
        data.append(dict(zip(keys, line.strip().split(','))))
    features = keys[1:-1]
    scores = get_most_informative_features(data, features)
    best_scores = sorted(scores.items(), key=lambda x: x[1][1], reverse=True)[:10]
    for elem in best_scores:
        print(elem[0], '\t\t\t', elem[1][0], '\t:\t', "%.1f" % elem[1][1], ' : 1.0')
    print('\n\n\n\n')
    
