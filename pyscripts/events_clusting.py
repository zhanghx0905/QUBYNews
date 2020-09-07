import json
from collections import Counter

import requests
from sklearn.cluster import KMeans
from sklearn.feature_extraction.text import TfidfVectorizer

path = "data.json"
size = 10000
BASE_URL = f"https://covid-dashboard.aminer.cn/api/events/list?type=event&page=1&size={size}"


def get_data():
    s = requests.get(BASE_URL)
    data = s.json()['data']
    print(len(data))
    res_list = []
    for event in data:
        tmp = {
            'id': event['_id'],
            'time': event['time'],
            'title': event['title'],
            'seg_text': event['seg_text'],
            'source': '', 'type': 'event'
        }
        res_list.append(tmp)
    return res_list


def load_dataset():
    '''导入文本数据集'''
    data = get_data()
    text_list = [news['seg_text'] for news in data]
    return text_list, data


def save(dataset: list, predict: list, keywords: list):
    res = []
    for idx, news in enumerate(dataset):
        news.pop('seg_text')
        news['predict'] = int(predict[idx])
        news['keywords'] = keywords[predict[idx]]
        res.append(news)
    with open('events.json', 'w', encoding='utf8') as f:
        json.dump(res, f, ensure_ascii=False)


def transform(dataset, n_features):
    vectorizer = TfidfVectorizer(
        max_df=0.5, max_features=n_features, min_df=2, use_idf=True)
    X = vectorizer.fit_transform(dataset)
    return X, vectorizer


def train(X, vectorizer, n_clusters, keys=5):

    km = KMeans(n_clusters=n_clusters, init='k-means++', max_iter=300, n_init=1,
                verbose=False)
    km.fit(X)
    order_centroids = km.cluster_centers_.argsort()[:, ::-1]
    terms = vectorizer.get_feature_names()
    keywords_list = []
    for i in range(n_clusters):
        keywords = []
        for ind in order_centroids[i, :keys]:
            keywords.append(terms[ind])
        keywords_list.append(' '.join(keywords))

        print(f"Cluster {i}: {keywords_list[-1]}")

    result = list(km.predict(X))
    print('Cluster distribution:')
    print(Counter(result))
    return result, keywords_list


def clean(keywords):
    res = []
    for keyword in keywords:
        key_list = keyword.split(' ')
        key_res = []

        for i in range(len(key_list)):
            mark = True
            for j in range(len(key_list)):
                if i != j and key_list[i] in key_list[j]:
                    mark = False
            if mark:
                key_res.append(key_list[i])
        res.append(' '.join(key_res))
    return res


def main():
    texts, dataset = load_dataset()
    X, vectorizer = transform(texts, n_features=300)

    predict, keywords = train(X, vectorizer, n_clusters=25)
    keywords = clean(keywords)
    print(keywords)
    save(dataset, predict, keywords)


main()
