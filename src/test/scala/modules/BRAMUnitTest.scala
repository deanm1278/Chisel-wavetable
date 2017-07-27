package modules.test

/*
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import modules.RamArb
import org.scalatest.Matchers

class RamArbUnitTester(c: RamArb) extends PeekPokeTester(c) {

	poke(c.io.WCLK, 0)
	poke(c.io.RCLK, 0)

  def rd(bank: Int, addr: Int, data: Int) = {
    poke(c.io.RCLK, 1)
    poke(c.io.RBANK, bank)
    poke(c.io.RADDR, addr)
    step(1)
    poke(c.io.RCLK, 0)
    expect(c.io.RDATA, data)
  }
  def wr(bank: Int, addr: Int, data: Int)  = {
    poke(c.io.WCLK, 1)
    poke(c.io.WADDR, addr)
    poke(c.io.WBANK, bank)
    poke(c.io.WDATA, data)
    step(1)
    poke(c.io.WCLK, 0)
  }
  wr(1, 0, 1)
  rd(1, 0, 1)
  wr(3, 9, 11)
  rd(3, 9, 11)
}

class RamArbTester extends ChiselFlatSpec with Matchers {
  private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "RamArb" should s"just be a BRAM module (with $backendName)" in {
      Driver(() => new RamArb, backendName) {
        c => new RamArbUnitTester(c)
      } should be (true)
    }
  }
}
*/