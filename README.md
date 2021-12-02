# Mailpiece

## Deploy
kubectl delete namespace mailpiece
kubectl create namespace mailpiece

kubectl -n mailpiece apply -f "https://strimzi.io/install/latest?namespace=mailpiece" 
kubectl -n mailpiece apply -f ./kubernetes/kafka/kafka.yml 

kubectl -n mailpiece apply -f ./mailpiece-processor/mailpiece-topics.yml
kubectl -n mailpiece delete -f ./mailpiece-processor/mailpiece-processor.yml
kubectl -n mailpiece apply -f ./mailpiece-processor/mailpiece-processor.yml

kubectl -n mailpiece apply -f ./kubernetes/ksqldb-server/ksqldb-server.yml
kubectl -n mailpiece apply -f ./mailpiece-app/mailpiece-app.yml

kubectl -n mailpiece create secret docker-registry docker-registry-credentials \
--docker-server=https://index.docker.io/v1/ --docker-username=chiodonia \
--docker-password=galippa --docker-email=andrea.chiodoni@gmail.com 
kubectl -n mailpiece apply -f ./kubernetes/kafka-connect/kafka-connect.yml
kubectl -n mailpiece apply -f ./kubernetes/elasticsearch/elasticsearch-connectors.yml
kubectl -n mailpiece apply -f ./kubernetes/elasticsearch/elasticsearch.yml
kubectl -n mailpiece apply -f ./kubernetes/kibana/kibana.yml

## Commands
kubectl -n mailpiece get pods 
kubectl -n mailpiece get pvc 
kubectl -n mailpiece logs -f mailpiece-processor-0
kubectl -n mailpiece delete pod mailpiece-processor-0 
kubectl -n mailpiece exec --stdin --tty mailpiece-processor-0 -- /bin/bash 
kubectl -n mailpiece describe pod mailpiece-processor-0 
kubectl -n mailpiece get services 
kubectl -n mailpiece describe services ksqldb-server 

kubectl -n mailpiece scale --replicas=3 StatefulSet/mailpiece-processor

## Connecting from inside Kubernates
* kafka-kafka-bootstrap.mailpiece.mailpiece:9092
* kafka-connect-connect-api:8083
* psql -h postgres -U mydbadmin --password mydbadmin -p 5432 mydb

## Connecting from outside Kubernates
```
kubectl port-forward service/kafka-kafka-external-bootstrap 9094:9094 -n mailpiece &
kubectl port-forward service/kafka-connect-connect-api 8083:8083 -n mailpiece &
kubectl port-forward deployment/ksqldb-server 8088:8088 -n mailpiece &
kubectl port-forward deployment/postgres 5432:5432 -n mailpiece &
```

* localhost:9094
* http://localhost:8083/
* http://localhost:8088/
* psql -h localhost -U mydbadmin --password mydbadmin -p 5432 mydb

## Tests
## mailpiece-processor
http://localhost:30080/kstreams/topology
http://localhost:30080/kstreams/state
http://localhost:30080/kstreams/stores
http://localhost:30080/kstreams/stores/mailpiece-table/get
http://localhost:30080/kstreams/stores/mailpiece-table/get?key=990000000000000010

http://localhost:30080/actuator/
http://localhost:30080/actuator/health
http://localhost:30080/actuator/prometheus

http://localhost:30080/generate?nr=10&from=200&delay=10000
http://localhost:30080/mailpiece?id=990000000000000200

## mailpiece-app
http://localhost:30090/ingested?zip=6900
http://localhost:30090/mailpiece?id=990000000000000200

### elasticsearch
kubectl port-forward deployment/elasticsearch 9200:9200 -n mailpiece &
http://localhost:9200/logistics.mailpiece-state
http://localhost:9200/logistics.mailpiece-state/_search
http://localhost:9200/logistics.mailpiece-state/_search?q=id:990000000000001945

## kibaba
http://localhost:30601/

## Stream processing concepts
- co-partitioning
	- https://medium.com/xebia-france/kafka-streams-co-partitioning-requirements-illustrated-2033f686b19c
	- rekey
- streams - tables duality
- stateless stream operations
- stateful stream operations
	- state management
	- time and Windows
- programming models
	https://docs.ksqldb.io/en/0.8.x-ksqldb/concepts/ksqldb-and-kafka-streams/
	https://developer.confluent.io/learn-kafka/kafka-streams/
	https://developer.confluent.io/learn-kafka/ksqldb/



	


