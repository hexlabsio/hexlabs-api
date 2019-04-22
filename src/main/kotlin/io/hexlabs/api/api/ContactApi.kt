package io.hexlabs.api.api

import io.hexlabs.api.model.Contact
import io.hexlabs.api.service.ContactService
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes

private val extractContactFrom = Body.auto<Contact>().toLens()

class ContactApi(private val contactService: ContactService) : Api {
    override fun apiRoutes() = routes(
        "/contact" bind routes(
            Method.POST to { request: Request ->
                val contact = extractContactFrom(request)
                contactService.send(contact)
                Response(OK)
            }
        )
    )
}