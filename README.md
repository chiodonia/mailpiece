# Mailpiece

A playground to experiment stuff around streaming.

## Deploy
kubectl delete namespace mailpiece
kubectl create namespace mailpiece

kubectl -n mailpiece apply -f "https://strimzi.io/install/latest?namespace=mailpiece" 
kubectl -n mailpiece apply -f ./kubernetes/kafka/kafka.yml 
kubectl -n mailpiece apply -f ./kubernetes/ksqldb-server/ksqldb-server.yml

kubectl -n mailpiece apply -f ./mailpiece-processor/mailpiece-topics.yml
kubectl -n mailpiece apply -f ./mailpiece-processor/mailpiece-processor.yml
kubectl -n mailpiece apply -f ./mailpiece-apps/mailpiece-apps.yml

kubectl -n mailpiece create secret docker-registry docker-registry-credentials \
--docker-server=https://index.docker.io/v1/ --docker-username=chiodonia \
--docker-password=xxx --docker-email=andrea.chiodoni@gmail.com 
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

## Endpoints

### elasticsearch
kubectl port-forward deployment/elasticsearch 9200:9200 -n mailpiece &
http://localhost:9200/logistics.mailpiece-state
http://localhost:9200/logistics.mailpiece-state/_search
http://localhost:9200/logistics.mailpiece-state/_search?q=id:990000000000000200

## kibaba
http://localhost:30601/

## Stream programming notes
- stream: infinite flow of messages (topic in Kafka)
- message semantic: event, document, state, command
- message: key-value (record in Kafka)
- message ordering (message key) vs message timestamps
- scalability: sharding. Shard key is the message key (partitions in Kafka)
- co-partitioning
- streams - tables duality
- stateless stream operations
- stateful stream operations
	- state management
	- windowing
- https://developer.confluent.io/learn/kafka-storage-and-processing/
- programming models
	- Kafka producer and consumer API
		- library
		- imperative - any lanuage
		- https://developer.confluent.io/learn/apache-kafka-faqs/
		- https://developer.confluent.io/learn/kafka-transactions-and-guarantees/
	- Kafka streams
		- library
		- declarative - Java
		- stateless and stateful stream operations
		- reliable and distributed (sharding) state stores
		- interactive query
		- https://developer.confluent.io/learn-kafka/kafka-streams/
	- ksqlDB
		- runtime
		- SQL
		- stateless and stateful stream operations
		- https://developer.confluent.io/learn-kafka/ksqldb/


	


