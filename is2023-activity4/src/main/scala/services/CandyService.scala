package services

import zio.*
import zio.json.*

import candy.*

final case class MachineStatus(locked: Boolean, melonGums: Int)

object MachineStatus:
  given JsonEncoder[MachineStatus] = DeriveJsonEncoder.gen[MachineStatus]

trait CandyService:
  def coin: ZIO[Any, Nothing, Unit]
  def turn: ZIO[Any, Nothing, Int]
  def status: ZIO[Any, Nothing, MachineStatus]

object CandyService:

  def coin: ZIO[CandyService, Nothing, Unit] = 
    ZIO.serviceWithZIO[CandyService](_.coin)

  def turn: ZIO[CandyService, Nothing, Int] = 
    ZIO.serviceWithZIO[CandyService](_.turn)
  
  def status: ZIO[CandyService, Nothing, MachineStatus] = 
    ZIO.serviceWithZIO[CandyService](_.status)
