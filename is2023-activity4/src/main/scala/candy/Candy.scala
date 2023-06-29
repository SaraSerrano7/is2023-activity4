package candy

import State.*

enum Input:
  case Coin, Turn, Restart

case class Machine(locked: Boolean, melonGums: Int, coins: Int):
  import Input.*
  def update(i: Input): Machine =
    (i, this) match
      case (Coin, Machine(true, melonGums, coins)) if melonGums > 0 =>
        Machine(false, melonGums, coins + 1)
      case (Turn, Machine(false, melonGums, coins)) if melonGums > 0 =>
        Machine(true, melonGums - 1, coins)
      case _ => 
        this

object Machine:
  def simulateMachine(inputs: List[Input]): State[Machine, (Int, Int)] =
    for
      _ <- State.traverse(inputs)(i => State.modify[Machine](_.update(i)))
      m <- State.get
    yield (m.melonGums, m.coins)
