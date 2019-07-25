import requests
import json
import time
from datetime import datetime, timedelta
import random
from random import randint
import uuid
import threading

filters = ['EVENT', 'LOL', 'LEARN', 'SECRET', 'FOOD', 'LOL', 'DEAL', 'CUTE']
response = requests.get('https://api.adviceslip.com/advice')
data = response.json()
content = data['slip']['advice']

def dropMessage():
    print('Dropping message!')

    response = requests.get('https://api.adviceslip.com/advice')
    data = response.json()
    content = data['slip']['advice']

    current_dt = datetime.now() - timedelta(hours=1)
    
    with open('random_message.json', 'r') as f:
        json_data = json.load(f)
        json_data['fields']['date']['timestampValue'] = str(current_dt.strftime('%Y-%m-%dT%H:%M:%S.%fZ'))
        json_data['fields']['duration']['integerValue'] = randint(30, 180)
        json_data['fields']['distance']['integerValue'] = randint(1000, 5000)
        json_data['fields']['filter']['stringValue'] = random.choice(filters)
        json_data['fields']['unixTime']['doubleValue'] = int(time.time()) 
        json_data['fields']['latitude']['doubleValue'] = random.uniform(52.450000000000000, 52.560000000000000)
        json_data['fields']['longitude']['doubleValue'] = random.uniform(13.300000000000000, 13.500000000000000)
        json_data['fields']['content']['stringValue'] = content
        json_data['fields']['uuid']['stringValue'] = str(uuid.uuid4())

    with open('random_message.json', 'w') as f:
        f.write(json.dumps(json_data))

    url = 'https://firestore.googleapis.com/v1beta1/projects/radar-6d6c2/databases/(default)/documents/messages/'
    payload = {'file': open('random_message.json', 'rb')}
    headers = {'Content-type': 'application/json', 'Accept': 'text/plain'}
    res = requests.post(url, json = json_data, headers = headers)
    threading.Timer(10, dropMessage).start()

dropMessage()

##print(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S:%fZ'))
##print(randint(30, 180))
##print(randint(1000, 5000))
##print(random.choice(filters))
##print(int(time.time()))
##print(random.uniform(52.450000000000000, 52.560000000000000))
##print(random.uniform(13.300000000000000, 13.500000000000000))
##print(content)
##print(uuid.uuid4())






