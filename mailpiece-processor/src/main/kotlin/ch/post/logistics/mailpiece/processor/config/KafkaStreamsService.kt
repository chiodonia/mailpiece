package ch.post.logistics.mailpiece.processor.config

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.TopologyDescription
import org.apache.kafka.streams.processor.internals.InternalTopologyBuilder
import org.apache.kafka.streams.query.Query
import org.apache.kafka.streams.query.RangeQuery
import org.apache.kafka.streams.query.StateQueryRequest
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Service
import java.util.function.Consumer
import java.util.stream.Collectors

@Service
class KafkaStreamsService(
    val streamsBuilderFactoryBean: StreamsBuilderFactoryBean,
    val stateListener: KafkaStreamsStateListener
) {

    fun topology(): TopologyDescription {
        return streamsBuilderFactoryBean.topology!!.describe()
    }

    fun state(): KafkaStreams.State {
        return stateListener.state
    }

    fun stores(): Set<String> {
        val stores: MutableSet<String> = HashSet()
        streamsBuilderFactoryBean.topology!!.describe().subtopologies().stream()
            .flatMap { subTopology: TopologyDescription.Subtopology ->
                subTopology.nodes().stream()
            }
            .filter { node: TopologyDescription.Node? -> node is InternalTopologyBuilder.Processor }
            .map { node: TopologyDescription.Node -> (node as InternalTopologyBuilder.Processor).stores() }
            .forEach { c: Set<String>? ->
                stores.addAll(
                    c!!
                )
            }
        streamsBuilderFactoryBean.topology!!.describe().globalStores()
            .forEach(Consumer { globalStore: TopologyDescription.GlobalStore ->
                stores.addAll(
                    globalStore.processor().stores()
                )
            })
        return stores
    }

    fun store(name: String, query: Query<Any>): Any? {
        return kafkaStreams().query(
            StateQueryRequest.inStore(name).withQuery(query)
        ).onlyPartitionResult.result
    }

    fun store(name: String, query: RangeQuery<Any, Any>): List<Any> {
        return kafkaStreams().query(StateQueryRequest.inStore(name).withQuery(query)).partitionResults.values
            .stream().map { r -> r.result }.collect(Collectors.toList())
    }

    private fun kafkaStreams(): KafkaStreams {
        return streamsBuilderFactoryBean.kafkaStreams!!
    }

}