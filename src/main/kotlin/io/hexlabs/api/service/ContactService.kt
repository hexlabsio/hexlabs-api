package io.hexlabs.api.service

import io.hexlabs.api.model.Contact
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request

import org.http4k.format.Jackson.asJsonObject
import org.http4k.format.Jackson.asPrettyJsonString

interface ContactService {
    fun send(contact: Contact)
}

class ConnectedContactService : ContactService {
    override fun send(contact: Contact) {
        val slackMessage = SlackMessage(contact.message)
        val request = Request(Method.POST, "https://hooks.slack.com/services/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")
            .header("Content-Type", "application/json")
            .body(slackMessage.asJsonObject().asPrettyJsonString())
        val client: HttpHandler = JavaHttpClient()
        val hello = client(request)
        hello
    }
}

data class SlackMessage(val text: String)