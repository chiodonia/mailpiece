# AsyncAPI

## Spec
https://www.asyncapi.com/docs/specifications/2.0.0#schemaObject

## Design
https://playground.asyncapi.io

## Generate 

### Documentation
npm install -g @asyncapi/html-template
ag api.yml @asyncapi/html-template -o ./target/docs

### Java model
https://github.com/asyncapi/modelina

npm i ts-node
npm install @asyncapi/modelina
npm i && npm run generate
mvn install

Issue
Generate JSON Schema, xml Schema


