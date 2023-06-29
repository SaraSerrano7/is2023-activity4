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

         CandyService
          .coinError
          .catchAll{
            _ => 
              CandyService.restart.map {
                _ => Response.status(Status.BadRequest)
              }
          }
          .map(_ => Response.status(Status.Ok))

        // CandyService.coin.map{
        //   _ => CandyService.status.map{
        //     (machine: Machine) => if machine.error then
        //       Response.status(Status.BadRequest)
        //       else ???
        //   }
        // }

        // CandyService.coin.map(_ => Response.status(Status.Ok))

        // invalidAction("coin").map{
        //     (invalid: Boolean) => 
        //       if invalid then Response.status(Status.BadRequest)
        //       else CandyService.coin.map(_ => Response.status(Status.Ok))
        // }

        // if invalidAction("coin") then Response.status(Status.BadRequest)
        // else CandyService.coin.map(_ => Response.status(Status.Ok))

        // for 
        //   action <- req.bodyAsString.map(_.fromJson[CandyService])
        //   result <- action match
        //     case Left(e) => ???
        //     case Right(action) => ???
        // yield result

      // POST /turn
      case Method.POST -> !! / "turn" =>

        CandyService
          .turnError
          .map{
            melonGums => Response.json(melonGums.toJson)
          }
          .catchAll{
            _ => CandyService.restart.map{
              _ => Response.status(Status.BadRequest)
            }
          }
        
        // CandyService.turn.map(melonGums => Response.json(melonGums.toJson))
        
        
        // if invalidAction("turn") then Response.status(Status.BadRequest)
        // else CandyService.turn.map(melonGums => Response.json(melonGums.toJson))
    }

  def invalidAction(action: String): ZIO[CandyService, Nothing, Boolean] =
    CandyService.status.map{
      (status: MachineStatus) => 
        val gums = status.melonGums
        val lock = status.locked
        (action, gums, lock) match
          case (_, gums, _) if gums < 0 => true
          case ("turn", _, true) => true
          case ("coin", _, false) => true
          case _ => false
        
    }
