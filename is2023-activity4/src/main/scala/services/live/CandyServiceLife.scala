package services.live

import zio.*

import candy.*
import services.*

final case class CandyServiceLive(ref: Ref[Machine]) extends CandyService:

  /*
  Entiendo que aqui debo implementar los metodos, como en InmemoryUserRepo
  */

  def coin: ZIO[Any, Nothing, Unit] =
    // ref.update(_.update(Input.Coin))
    for
      _ <- ref.update(_.update(Input.Coin))
    yield ()

  def turn: ZIO[Any, Nothing, Int] =
    // ref.update(_.update(Input.Turn))
    // ref.get.map(_.melonGums)
    for 
      _ <- ref.update(_.update(Input.Turn))
      machine <- ref.get
    yield machine.melonGums
    

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
    yield(MachineStatus(machine.locked, machine.melonGums))



  def coin3: ZIO[Any, Nothing, Unit] =
    /*
    - Coin: 
      - Flujo que actualiza el estado de la maquina
      - Su entrada es una moneda (Coin)
    */ 
    ref.get.map((machine:Machine) => machine.update(Input.Coin))
    // for 
    //   currentMachine: Machine <- ref.get
    //   _ <- currentMachine.update(Input.Coin)
    // yield ()

  def coin2: ZIO[Any, Nothing, Unit] =
    for 
      machine <- ref.get
    yield machine.update(Input.Coin)
      
  def turn3: ZIO[Any, Nothing, Int] = 
    /*
  - Turn:
    - Flujo que actualiza el estado de la maquina
    - Su entrada sea girar la manija (Turn)
    - Retorna el número de melonGums restantes despues de hacer la acción
    */
    for 
      machine <- ref.get
    yield machine.update(Input.Turn).melonGums

  
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
      Creamos una capa a partir de un codigo de inicializacion 
        -> partimos de una maquina en un estado concreto
      - Como tenemos que devolver una ZLayer, la creamos
      - necesitamos partir de la maquina que nos dicen -> fromZIO
      - Nos hablan de mutabilidad -> lo que vimos en el temario de Ref
      - Para crear una referencia, Ref.make
      - De qué partimos? de una máquina
      - cómo nos piden la maquina?
        - desbloqueada (false)
        - tendra el numero de melongums pasados
          - si es negativo -> fallara con error de tipo String // TODO: 
          - si falla... no muere? que ocurre entonces? // TODO:
        - no tendra ninguna moneda (0)
    */
    ZLayer.fromZIO(
      for 
        ref <- Ref.make(Machine(false, melonGums, 0))
      yield CandyServiceLive(ref)
    )