package candy

import State.*

enum Input:
  case Coin, Turn, Restart

case class Machine(locked: Boolean, melonGums: Int, coins: Int, error: Boolean):
  import Input.*
  def update(i: Input): Machine =
    //la idea es que auqi no llegara ninguna en error=true
    //en la api se updatea la ref con los mismos atributos pero error=False
    (i, this) match
      case (Coin, Machine(true, melonGums, coins, _)) if melonGums > 0 =>
        Machine(false, melonGums, coins + 1, false)
      case (Turn, Machine(false, melonGums, coins, _)) if melonGums > 0 =>
        Machine(true, melonGums - 1, coins, false)
      //no quedan melonGums
      case (_, Machine(locked, melonGums, coins, _)) if melonGums < 0 =>
        Machine(locked, melonGums, coins, true) 
      //hacer turn cuando está locked
      case (Turn, Machine(true, melonGums, coins, _)) =>
        Machine(true, melonGums, coins, true)
      //meter coin cuando no está locked
      case (Coin, Machine(false, melonGums, coins, _)) =>
        Machine(false, melonGums, coins, true)
      //acción restart --> la misma máquina, pero sin estar en error
      case (Restart, Machine(locked, melonGums, coins, true)) =>
        Machine(locked, melonGums, coins, false)
      //compilador obliga
      case _ => 
        this

object Machine:
  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] =
    for
      _ <- State.traverse(inputs)(i => State.modify[Machine](_.update(i)))
      m <- State.get
    yield (m.melonGums, m.coins)
