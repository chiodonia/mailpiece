# Mailpiece apps

Mailpiece apps contains the following showcases:

## Mailpiece producer
The generation of random but related MailpieceEvent using coroutines. For each mailpiece and ingested and then a delivered MailpieceEvent will be produced. 

See http://localhost:30090/producer/generate?nr=10&from=200&delay=10000

## GraphQL
A GraphQL (https://graphql.org/) controller to try it out, see https://spring.io/blog/2022/06/14/spring-tips-learn-spring-for-graphql-parts-5-and-6-of-an-ongoing-series

### Queries
```
    export RSC_VERSION=0.9.1
    curl -L -o rsc.jar https://github.com/making/rsc/releases/download/${RSC_VERSION}/rsc-${RSC_VERSION}.jar

    java -jar rsc.jar \
        --request \
        --route=graphql \
        --dataMimeType="application/graphql+json" \
        --data='{"query":"{greeting {greeting timestamp greetingFrom {name}} }" }' \
        tcp://localhost:30091 \
    | jq '.'
```

### Subscriptions
```
    export RSC_VERSION=0.9.1
    curl -L -o rsc.jar https://github.com/making/rsc/releases/download/${RSC_VERSION}/rsc-${RSC_VERSION}.jar

    java -jar rsc.jar \
        --stream \
        --route=graphql \
        --dataMimeType="application/graphql+json" \
        --data='{"query":"subscription { greetings { greeting timestamp greetingFrom { name } } }" }' \
        tcp://localhost:30091 \
    | jq '.'
```

## Streaming
Streaming shows how to leverage https://ksqldb.io/ to query streams and tables.

---
**NOTE**
There is a ksqlDB API client to interact with the ksqlDB REST API:
- https://docs.ksqldb.io/en/latest/developer-guide/ksqldb-clients/java-client/
- https://docs.ksqldb.io/en/latest/developer-guide/api/
---

GraphQL (https://graphql.org/) supports the following queries and subscriptions: 

### Queries
Queries are used to access tables (state): 
```
{findMailpiece(id: "990000000000000200") {
    id 
    state 
    priority 
    events {
        timestamp 
        zip 
        state
    }
}}
```
``` 
    curl 'http://localhost:30090/graphql' \
        -H 'Content-Type: application/json' \
        -H 'Accept: application/json' \
        --data '{
            "query": "{findMailpiece(id: '990000000000000200') {id state priority events {timestamp zip state}}}"
        }' \
    | jq '.'
```

A GraphQL queries are also streamed over https://rsocket.io/. To test GraphQL queries use https://github.com/making/rsc
```
    export RSC_VERSION=0.9.1
    curl -L -o rsc.jar https://github.com/making/rsc/releases/download/${RSC_VERSION}/rsc-${RSC_VERSION}.jar

    java -jar rsc.jar \
        --request \
        --route=graphql \
        --dataMimeType="application/graphql+json" \
        --data='{"query":"{findMailpiece(id: '990000000000000200') {id state priority events {timestamp zip state}}}" }' \
        tcp://localhost:30091 \
    | jq '.'
```

You can play with the GraphQL engine using http://localhost:30090/graphiql

See also http://localhost:30090/streaming/mailpiece?id=990000000000000200

### Subscriptions
Queries are used to query streams. A GraphQL subscriptions are streamed over https://rsocket.io/.

To test GraphQL subscriptions use https://github.com/making/rsc

```
    export RSC_VERSION=0.9.1
    curl -L -o rsc.jar https://github.com/making/rsc/releases/download/${RSC_VERSION}/rsc-${RSC_VERSION}.jar

    java -jar rsc.jar \
        --stream \
        --route=graphql \
        --dataMimeType="application/graphql+json" \
        --data='{"query":"subscription { delivered(zip: \"6900\", take: 10) { id timestamp delivered } }" }' \
        tcp://localhost:30091 \
    | jq '.'
```

To debug rsocket:

```
    export RSC_VERSION=0.9.1
    curl -L -o rsc.jar https://github.com/making/rsc/releases/download/${RSC_VERSION}/rsc-${RSC_VERSION}.jar

    java -jar rsc.jar \
        --stream \
        --route=graphql \
        --dataMimeType="application/graphql+json" \
        --data='{"query":"subscription { delivered(zip: \"6900\", take: 10) { id timestamp delivered } }" }' \
        --debug \
        tcp://localhost:30091 
```

See also http://localhost:30090/streaming/delivered?zip=6900&take=1

