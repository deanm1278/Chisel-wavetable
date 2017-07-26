package modules.test

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.PWM
import org.scalatest.Matchers
import scala.util.Random

class PWMUnitTester(c: PWM) extends PeekPokeTester(c) {

  var x = 0

  val r = Random

  for(n <- 1 to 100) {
    val enable: BigInt = BigInt(r.nextInt % 2).abs
    val period: BigInt = BigInt(r.nextInt % (1 << c.w)).abs
    val duty: BigInt  = BigInt(r.nextInt % period.toInt).abs

    val out = enable & (x > duty)

    poke(c.io.en, enable)
    poke(c.io.per, period)
    poke(c.io.dc,   duty)
    expect(c.io.out,  out)

    step(1)

    x = x + 1
    if (x > period) x = 0
  }
}

class PWMTester extends ChiselFlatSpec with Matchers {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "PWM" should s"generate pwm wave (with $backendName)" in {
      Driver(() => new PWM(12), backendName) {
        c => new PWMUnitTester(c)
      } should be (true)
    }
  }
}
