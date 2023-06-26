package api

import zio.*
import zio.json.*
import zhttp.http.*

import services.CandyService

object CandyAPI:
  def apply(): Http[CandyService, Nothing, Request, Response] = 
    /*
    - `GET` a `/status`:  retorna, en forma de _json_, l'status actual de la màquina

    - `POST` a `/coin`: realitza l'acció de posar una moneda a la màquina i retorna l'status http `ok`

    - `POST` a `/turn`: realitza l'acció de girar la maneta i retorna el número de `melonGums` que té la màquina
    */
    Http.collectZIO[Request] {
      // GET /status
      case Method.GET -> !! / "status" =>
        CandyService.status.map(status => Response.json(status.toJson))

      // POST /coin
      // TODO: comprobar acciones invalidas
      case Method.POST -> !! / "coin" => 
        // CandyService.coin.map(_ => Response.status(Status.Ok))

        CandyService.coin.map(_ => Response.status(Status.Ok))

        // for 
        //   action <- req.bodyAsString.map(_.fromJson[CandyService])
        //   result <- action match
        //     case Left(e) => ???
        //     case Right(action) => ???
        // yield result

      // POST /turn
      case Method.POST -> !! / "turn" =>
        CandyService.turn.map(melonGums => Response.json(melonGums.toJson))
    }
