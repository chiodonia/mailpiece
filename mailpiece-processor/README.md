# Mailpiece processor

Mailpiece processor processes MailpieceEvent events to a Mailpiece state.
Both MailpieceEvent and Mailpiece are defined by the mailpiece-stream-api.

Mailpiece state can be quieried using Kafka Streams Interactive Queries.

https://rsocket.io is used between between mailpiece processor application instances to query remote state stores.

A GraphQL (https://graphql.org) engine is used as a flexible query language on top of Kafka Streams Interactive Queries.

## GraphQL queries
```
	curl 'http://localhost:30080/graphql' \
		-H 'Content-Type: application/json' \
		-H 'Accept: application/json' \
		--data '{
			"query": "{findMailpiece(id: '990000000000000200') {id state priority events {timestamp zip state}}}"
		}' \
	| jq '.'
```

see also http://localhost:30080/graphiql/

## Endpoints

http://localhost:30080/kstreams/topology
http://localhost:30080/kstreams/state
http://localhost:30080/kstreams/stores
http://localhost:30080/kstreams/stores/mailpiece-store/data
http://localhost:30080/kstreams/stores/mailpiece-store/data?key=990000000000000200

http://localhost:30080/actuator/
http://localhost:30080/actuator/health
http://localhost:30080/actuator/prometheus

## Documentation
- https://itnext.io/health-checks-for-kafka-streams-application-on-kubernetes-e9c5e8c21b0d


