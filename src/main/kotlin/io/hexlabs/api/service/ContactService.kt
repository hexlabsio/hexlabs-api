package io.hexlabs.api.service

interface ContactService {
    fun send()
}

class ConnectedContactService : ContactService {
    override fun send() {
    }
}