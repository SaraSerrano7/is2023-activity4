package services.live

import zio.*

import candy.*
import services.*

final case class CandyServiceLive(ref: Ref[Machine]) extends CandyService:

  def coin: ZIO[Any, Nothing, Unit] =
    for
      _ <- ref.update(_.update(Input.Coin))
    yield ()

  def turn: ZIO[Any, Nothing, Int] =
    for 
      _ <- ref.update(_.update(Input.Turn))
      machine <- ref.get
    yield machine.melonGums

  def status: ZIO[Any, Nothing, MachineStatus] =
    for
      machine <- ref.get
    yield(MachineStatus(machine.locked, machine.melonGums))
  
object CandyServiceLive:
  def layer(melonGums: Int): ZLayer[Any, String, CandyServiceLive] = 
    ZLayer.fromZIO(
      for 
        ref <- Ref.make(Machine(false, melonGums, 0))
      yield CandyServiceLive(ref)
    )