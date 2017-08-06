package modules.test

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.ResetLatch
import org.scalatest.Matchers
import scala.util.Random

class ResetLatchUnitTester(c: ResetLatch) extends PeekPokeTester(c) {

  val r = Random

  poke(c.io.hold, 1)
  poke(c.io.trigger, 0)

  step(10)
  expect(c.io.out, 0)

  poke(c.io.hold, 0)
  step(10)
  expect(c.io.out, 0)

  poke(c.io.trigger, 1)
  step(1)
  poke(c.io.trigger, 0)
  expect(c.io.out, 1)

  step(10)
}

class ResetLatchTester extends ChiselFlatSpec with Matchers {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "ResetLatch" should s"be held low when hold input is high. if !hold and trigger, latch high. (with $backendName)" in {
      Driver(() => new ResetLatch, backendName) {
        c => new ResetLatchUnitTester(c)
      } should be (true)
    }
  }
}