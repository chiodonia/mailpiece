package ch.post.logistics.mailpiece.processor.service

import ch.post.logistics.mailpiece.processor.config.StateListener
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.processor.internals.InternalTopologyBuilder
import org.apache.kafka.streams.state.HostInfo
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Service
import java.util.function.Consumer

@Service
class KafkaStreamsService(val streamsBuilderFactoryBean: StreamsBuilderFactoryBean, val stateListener: StateListener) {

    fun topology(): TopologyDescription {
        return streamsBuilderFactoryBean.topology.describe()
    }

    fun state(): KafkaStreams.State {
        return stateListener.state
    }

    fun stores(): Set<String> {
        val stores: MutableSet<String> = HashSet()
        streamsBuilderFactoryBean.topology.describe().subtopologies().stream()
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
        streamsBuilderFactoryBean.topology.describe().globalStores()
            .forEach(Consumer { globalStore: TopologyDescription.GlobalStore ->
                stores.addAll(
                    globalStore.processor().stores()
                )
            })
        return stores
    }

    fun store(name: String): ReadOnlyKeyValueStore<String, Any>? {
        return streamsBuilderFactoryBean.kafkaStreams.store(
            StoreQueryParameters
                .fromNameAndType(
                    name,
                    QueryableStoreTypes.keyValueStore()
                )
        )
    }

    fun store(name: String, key: String): Any? {
        val store = store(name)
        if (store?.get(key) == null) {
            throw NoSuchElementException("Key $key does not exist")
        } else {
            return store.get(key)
        }
    }

    fun metadata(store: String, key: String): KeyQueryMetadata {
        return streamsBuilderFactoryBean.kafkaStreams.queryMetadataForKey(store, key, Serdes.String().serializer())
    }

    fun hostInfo(): HostInfo {
        return HostInfo.buildFromEndpoint(streamsBuilderFactoryBean.streamsConfiguration!![StreamsConfig.APPLICATION_SERVER_CONFIG] as String)
    }

}