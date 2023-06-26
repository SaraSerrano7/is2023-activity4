package candy

import zio.test.*

import candy.*
import Input.*

object CandySuite extends ZIOSpecDefault:

  private val genPosInt = Gen.int(1, 1000)
  private val genNonNegInt = Gen.int(0, 1000)
  private val genInput = Gen.elements(Coin, Turn)
  private val genInputList = Gen.listOf(genInput)

  private val genNoCandiesMachine =
    for
      locked <- Gen.boolean
      coins <- genNonNegInt
    yield Machine(locked, 0, coins)

  private val genLockedMachine =
    for
      melonGums <- genPosInt // A machine must have at least one candy
      coins <- genNonNegInt
    yield Machine(true, melonGums, coins)

  private val genUnlockedMachine =
    for
      melonGums <- genPosInt // A machine must have at least one candy
      coins <- genNonNegInt
    yield Machine(false, melonGums, coins)

  private val genMachine =
    for
      locked <- Gen.boolean
      melonGums <- genNonNegInt
      coins <- genNonNegInt
    yield Machine(locked, melonGums, coins)

  val spec = suite("Candy")(
    test("empty inputs") {
      check(genMachine) { machine =>
        val ((melonGums, coins), machine1): ((Int, Int), Machine) =
          Machine.simulateMachine(List.empty[Input]).run(machine)

        assertTrue(
          melonGums == machine.melonGums,
          coins == machine.coins,
          machine1 == machine // Nothing changed
        )
      }
    },
    test("a machine that's out of candy") {
      // A machine that’s out of candy ignores all inputs.
      check(genInputList, genNoCandiesMachine) { (inputs, machine) =>
        val ((melonGums, coins), machine1) =
          Machine.simulateMachine(inputs).run(machine)

        assertTrue(
          melonGums == 0,
          coins == machine.coins,
          machine1 == machine // Nothing changed
        )
      }
    },
    test("inserting a coin into a locked machine") {
      check(genLockedMachine) { machine =>
        val ((melonGums, coins), machine1) =
          Machine.simulateMachine(List(Coin)).run(machine)

        assertTrue(
          melonGums == machine.melonGums,
          coins == machine.coins + 1, // One more coin
          machine1 == Machine(false, melonGums, coins) // Unlocks the machine
        )
      }
    },
    test("turning the knob on a locked machine") {
      check(genLockedMachine) { machine =>
        val ((melonGums, coins), machine1) =
          Machine.simulateMachine(List(Turn)).run(machine)

        assertTrue(
          melonGums == machine.melonGums,
          coins == machine.coins,
          machine1 == machine // Nothing changed
        )
      }
    },
    test("inserting a coin into an unlocked machine") {
      check(genUnlockedMachine) { machine =>
        val ((melonGums, coins), machine1) =
          Machine.simulateMachine(List(Coin)).run(machine)

        assertTrue(
          melonGums == machine.melonGums,
          coins == machine.coins,
          machine1 == machine // Nothing changed
        )
      }
    },
    test("turning the knob on an unlocked machine") {
      check(genUnlockedMachine) { machine =>
        val ((melonGums, coins), machine1) =
          Machine.simulateMachine(List(Turn)).run(machine)

        assertTrue(
          melonGums == machine.melonGums - 1, // The buyer has taken the candy
          coins == machine.coins,
          machine1 == Machine(true, melonGums, coins) // Locks the machine
        )
      }
    },
    test("spend some coins") {
      check(genLockedMachine, genPosInt) { (machine, myCoins) =>
        val wantToSpendAllMyCoins =
          (0 until myCoins).flatMap(_ => List(Coin, Turn)).toList
        val ((melonGums, coins), machine1) =
          Machine.simulateMachine(wantToSpendAllMyCoins).run(machine)
        val spentCoins = math.min(machine.melonGums, myCoins)

        assertTrue(
          melonGums == machine.melonGums - spentCoins,
          coins == machine.coins + spentCoins,
          machine1 == Machine(true, melonGums, coins)
        )
      }
    }
  )
