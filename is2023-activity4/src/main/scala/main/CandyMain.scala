package main

import zio.*
import zhttp.service.Server

import api.CandyAPI
import services.CandyService
import services.live.CandyServiceLive

object CandyMain extends ZIOAppDefault:

  val run: ZIO[Any, String | Throwable, Nothing] =
    Server.start(
      port = 8080,
      http = CandyAPI()
    ).provide(
      CandyServiceLive.layer(100)
    )
