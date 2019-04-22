package io.hexlabs.api.api

import org.http4k.routing.RoutingHttpHandler

interface Api {
    fun apiRoutes(): RoutingHttpHandler
}