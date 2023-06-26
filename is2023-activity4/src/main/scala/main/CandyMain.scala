package main

import zio.*
import zhttp.service.Server

import api.CandyAPI
import services.CandyService
import services.live.CandyServiceLive

object CandyMain extends ZIOAppDefault:

// TODO: puede fallar con String porque el metodo 
// CandyService.status puede fallar con tipo String
// si se le pasa un número negativo de melonGums
  val run: ZIO[Any, String | Throwable, Nothing] =
    Server.start(
      port = 8080,
      http = CandyAPI()
    ).provide(
      CandyServiceLive.layer(100)
    )
