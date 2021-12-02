package ch.post.logistics.mailpiece.processor.service.remote

data class QueryStateStoreByKeyRequest(val host: String, val port: Int, val store: String, val id: String)