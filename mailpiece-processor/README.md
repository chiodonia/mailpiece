# Mailpiece processor

Mailpiece processor processes MailpieceEvent events to a Mailpiece state.
Both MailpieceEvent and Mailpiece are defined by the mailpiece-stream-api.

Mailpiece state can be quieried using Kafka Streams Interactive Queries.

https://rsocket.io is used between between mailpiece processor application instances to query remote state stores.

A GraphQL (https://graphql.org) engine is used as a flexible query language on top of Kafka Streams Interactive Queries.

## GraphQL queries
```
	curl 'http://localhost:8080/graphql' \
		-H 'Content-Type: application/json' \
		-H 'Accept: application/json' \
		--data '{
			"query": "{findMailpiece(id: '990000000000000201') {id state priority events {timestamp zip state}}}"
		}' \
	| jq '.'
```

```
    export RSC_VERSION=0.9.1
    curl -L -o rsc.jar https://github.com/making/rsc/releases/download/${RSC_VERSION}/rsc-${RSC_VERSION}.jar

    java -jar rsc.jar \
        --request \
        --route=graphql \
        --dataMimeType="application/graphql+json" \
		--data='{
			"query": "{findMailpiece(id: '990000000000000201') {id state priority events {timestamp zip state}}}"
		}'
		tcp://localhost:9091 \
    | jq '.'
```

see also http://localhost:8080/graphiql/

## Endpoints

http://localhost:8080/kstreams/topology
http://localhost:8080/kstreams/state
http://localhost:8080/kstreams/stores
http://localhost:8080/kstreams/stores/mailpiece-store/data
http://localhost:8080/kstreams/stores/mailpiece-store/data?key=990000000000000208

http://localhost:8080/actuator/
http://localhost:8080/actuator/health
http://localhost:8080/actuator/prometheus

## Documentation
- https://itnext.io/health-checks-for-kafka-streams-application-on-kubernetes-e9c5e8c21b0d


