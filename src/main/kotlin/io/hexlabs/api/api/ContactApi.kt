package io.hexlabs.api.api

import io.hexlabs.api.service.ContactService
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes

class ContactApi(val contactService: ContactService) : Api {
    override fun apiRoutes() = routes(
        "/contact" bind routes(
            Method.POST to { request: Request ->
                contactService.send()
                Response(OK)
            }
        )
    )
}