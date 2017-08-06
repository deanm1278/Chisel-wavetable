package modules.test

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.ChangeDetect
import org.scalatest.Matchers
import scala.util.Random

class ChangeDetectUnitTester(c: ChangeDetect) extends PeekPokeTester(c) {

  val r = Random

  var in = 0
  var last = 0

  for(i <- 0 until 10){
    val comp = r.nextInt(1 << 2)
    poke(c.io.comp, comp)
    poke(c.io.in, in)
    step(1)
    for(k <- 0 until 25){
      in = r.nextInt(1 << 2)
      poke(c.io.in, in)
      step(1)
      if(last == comp && in != comp){ expect(c.io.changed, 1) }
      else { expect(c.io.changed, 0) }
      last = in
    }
  }
}

class ChangeDetectTester extends ChiselFlatSpec with Matchers {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "ChangeDetect" should s"detect when a value changes from a given value (with $backendName)" in {
      Driver(() => new ChangeDetect(2), backendName) {
        c => new ChangeDetectUnitTester(c)
      } should be (true)
    }
  }
}