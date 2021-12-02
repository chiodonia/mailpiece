import { load } from "js-yaml";
import { readFileSync } from 'fs';
import { parse } from '@asyncapi/parser';
import { JavaFileGenerator, JAVA_COMMON_PRESET, JAVA_CONSTRAINTS_PRESET, JAVA_JACKSON_PRESET, JAVA_DESCRIPTION_PRESET } from '@asyncapi/modelina';

const generator = new JavaFileGenerator({
  collectionType: "List",
  presets: [
    JAVA_CONSTRAINTS_PRESET,
    JAVA_JACKSON_PRESET,
    JAVA_DESCRIPTION_PRESET
  ]
});

export async function generate() : Promise<void> {
  const apiDefinition = 'api.yml';
  const packageName = 'ch.post.logistics.api';
  
  const modelGenerationOptions = {
    packageName: packageName
  };

  const api = await parse(JSON.stringify(load(readFileSync(apiDefinition, 'utf8'))));
  const models = await generator.generateToFiles(api as any, './target/java/', modelGenerationOptions);
  for (const model of models) {
    console.log(model.result);
  }
}
generate();
