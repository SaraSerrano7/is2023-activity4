package services.live

import zio.*

import candy.*
import services.*
import zio.Config.Bool

final case class CandyServiceLive(ref: Ref[Machine]) extends CandyService:

  /*
  Entiendo que aqui debo implementar los metodos, como en InmemoryUserRepo
  */

  def restart: ZIO[Any, Nothing, Unit] =
    for 
      _ <- Console.printLine("restarting").orDie
      _ <- ref.update(_.update(Input.Restart))
    yield ()

  def coin: ZIO[Any, Nothing, Unit] =
    // ref.update(_.update(Input.Coin))
    for
      _ <- ref.update(_.update(Input.Coin))
    yield ()


  def coinError: ZIO[Any, String, Unit] =
    for 
      _ <- ref.update(_.update(Input.Coin))
      newMachine <- ref.get
      _ <- Console.printLine(s"newMachine: $newMachine").orDie
      // _ <-  if newMachine.error then ZIO.fail("invalid action")
      //       else ref.update(_.update(Input.Restart))
    yield if newMachine.error then 
      Console.printLine(s"sending fail").orDie
      ZIO.fail("invalid action")
            else 
              Console.printLine(s"newMachine.error is ${newMachine.error}").orDie 
              ()

  def turn: ZIO[Any, Nothing, Int] =
    for 
      _ <- ref.update(_.update(Input.Turn))
      machine <- ref.get
    yield machine.melonGums

  def turnError: ZIO[Any, String, Int] =
    for
      _ <- ref.update(_.update(Input.Turn))
      newMachine <- ref.get
      _ <-  if newMachine.error then ZIO.fail("invalid action")
            // TODO: arreglar esto
            else ref.update(_.update(Input.Restart))

    yield newMachine.melonGums
    // yield if newMachine.error then ZIO.fail("invalid action")
    //         else newMachine.melonGums
    

  def status: ZIO[Any, Nothing, MachineStatus] =
    /*
  - Status:
    - Flujo que actualiza el estado de la maquina
    - Viene representado por esta clase (que se puede codificar en json)
      - final case class MachineStatus(locked: Boolean, melonGums: Int)
      - object MachineStatus:
      given JsonEncoder[MachineStatus] = 
        DeriveJsonEncoder.gen[MachineStatus]
    */
    for
      machine <- ref.get
      _ <- Console.printLine(s"status: $machine").orDie
    yield(MachineStatus(machine.locked, machine.melonGums))
  
object CandyServiceLive:
  /*
  Layer no depende de nada -> crearemos la referencia que contiene la maquina
  - La maquina creada tendrá el número de melonGums que se le pasan (no negativo)
  - Estará desbloqueada
  - No tendra ninguna moneda
  - Si el número pasado es negativo -> construccion falla con error de tipo String

  1. Constructing layers:
    a. ZLayer.succeed -> crea una layer con el valor provisto
      (cuando el servicio no necesita inicializacion ni finalizacion)
    b. ZLayer.apply / Zlayer.fromZIO -> crea una layer de un efecto
      (cuando necesitemos codigo de inicializacion)
      (ej: arrancar un servicio antes de hacer X)
    c. ZLayer.scoped -> para asegurarnos de que se finalice
    d. ZLayer.fromFunction -> crea una layer desde una funcion dada
  */
  def layer(melonGums: Int): ZLayer[Any, String, CandyServiceLive] = 
    /*copiado de InmemoryUserRepo
        - tendra el numero de melongums pasados
          - si es negativo -> fallara con error de tipo String // TODO: 
          - si falla... no muere? que ocurre entonces? // TODO:
    */
    ZLayer.fromZIO(
      for 
        ref <-  if melonGums < 0 then ZIO.fail("Negative gums") 
                else Ref.make(Machine(false, melonGums, 0, false))
      //        Ref.make(Machine(false, melonGums, 0))
      yield CandyServiceLive(ref)
    )