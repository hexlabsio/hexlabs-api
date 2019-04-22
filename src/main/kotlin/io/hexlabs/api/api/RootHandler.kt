package io.hexlabs.api.api

import io.hexlabs.api.filter.Filters
import io.hexlabs.api.service.ConnectedContactService
import io.hexlabs.api.service.ContactService
import org.http4k.core.HttpHandler
import org.http4k.core.then
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.http4k.serverless.AppLoader

fun main() {
    Root().apiRoutes().asServer(SunHttp(8080)).start()
    println("Server started on port 8080")
}

object RootApi : AppLoader {
    override fun invoke(environment: Map<String, String>): HttpHandler = Root().apiRoutes()
}

class Root(
    contactService: ContactService = ConnectedContactService()
) : Api {
    private val contactApi = ContactApi(contactService)
    override fun apiRoutes() =
        Filters.TRACING
        .then(Filters.CATCH_ALL)
        .then(Filters.CORS)
        .then(routes(
            contactApi.apiRoutes()
        ))
}