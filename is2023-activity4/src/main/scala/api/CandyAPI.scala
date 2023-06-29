package api

import zio.*
import zio.json.*
import zhttp.http.*

import services.CandyService
import candy.Machine
import services.MachineStatus
// import candy.Machine.melonGums

object CandyAPI:
  def apply(): Http[CandyService, Nothing, Request, Response] = 
    Http.collectZIO[Request] {
      // GET /status
      case Method.GET -> !! / "status" =>
        CandyService.status.map(status => Response.json(status.toJson))

      // POST /coin
      case Method.POST -> !! / "coin" =>
        CandyService.coin.map(_ => Response.status(Status.Ok))

      // POST /turn
      case Method.POST -> !! / "turn" =>
        CandyService.turn.map(melonGums => Response.json(melonGums.toJson))
    }
