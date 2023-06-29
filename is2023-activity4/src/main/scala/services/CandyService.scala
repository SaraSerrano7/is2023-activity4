package services

import zio.*
import zio.json.*

import candy.*

final case class MachineStatus(locked: Boolean, melonGums: Int)

object MachineStatus:
  given JsonEncoder[MachineStatus] = DeriveJsonEncoder.gen[MachineStatus]

trait CandyService:
  def restart: ZIO[Any, Nothing, Unit]
  def coin: ZIO[Any, Nothing, Unit]
  def coinError: ZIO[Any, String, Unit]
  def turn: ZIO[Any, Nothing, Int]
  def turnError: ZIO[Any, String, Int]
  def status: ZIO[Any, Nothing, MachineStatus]

object CandyService:

  def restart: ZIO[CandyService, Nothing, Unit] =
    ZIO.serviceWith[CandyService](_.restart)

  def coin: ZIO[CandyService, Nothing, Unit] = 
  //actualiza estado de la maquina, input = Coin
  /*
  Basandome en zio-restful-webservices
  tiene user que sería como la MachineStatus y el given
  luego tiene UserRepo, que tiene como dependencia el propio userRepo
  def register(user: User): ZIO[UserRepo, Throwable, String] =
    ZIO.serviceWithZIO[UserRepo](_.register(user))
  */
    // ZIO.serviceWithZIO[CandyService](Machine.simulateMachine(List(Input.Coin)))
    ZIO.serviceWithZIO[CandyService](_.coin)

  
  def coinError: ZIO[CandyService, String, Unit] = 
    ZIO.serviceWithZIO[CandyService](_.coinError)

  def turn: ZIO[CandyService, Nothing, Int] = 
    ZIO.serviceWithZIO[CandyService](_.turn)

  def turnError: ZIO[CandyService, String, Int] = 
    ZIO.serviceWithZIO[CandyService](_.turnError)
  
  def status: ZIO[CandyService, Nothing, MachineStatus] = 
    ZIO.serviceWithZIO[CandyService](_.status)
