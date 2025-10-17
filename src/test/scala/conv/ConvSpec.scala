package conv

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class DotProductSpec extends AnyFreeSpec with Matchers {
  "DotProduct should compute correct output with positive values" in {
    simulate(new DotProduct(3)) { dut =>
      //values for input vectors a and b
      dut.io.a(0).poke(1.S)
      dut.io.a(1).poke(2.S)
      dut.io.a(2).poke(3.S)

      dut.io.b(0).poke(4.S)
      dut.io.b(1).poke(5.S)
      dut.io.b(2).poke(6.S)

      val expected = (1*4 + 2*5 + 3*6).S

      dut.io.result.expect(expected)

      println(s"Test 1 - a: (1, 2, 3), b: (4, 5, 6), expected result: $expected, output: ${dut.io.result.peek().litValue}")
    }
  }
  
  "DotProduct should compute correct output with negative values" in {
    simulate(new DotProduct(3)) { dut =>
      dut.io.a(0).poke((-1).S)
      dut.io.a(1).poke(2.S)
      dut.io.a(2).poke(-3.S)

      dut.io.b(0).poke(4.S)
      dut.io.b(1).poke(-5.S)
      dut.io.b(2).poke(6.S)

      val expected = (-1*4 + 2*(-5) + (-3)*6).S // -4 - 10 - 18 = -32

      println(s"Test 2 - a: (-1, 2, -3), b: (4, -5, 6), expected result: $expected")
      dut.io.result.expect(expected)

      println(s"Test 2 - Output: ${dut.io.result.peek().litValue}")
    }
  }
}

class DepthwiseConvSpec extends AnyFreeSpec with Matchers {
  "DepthwiseConv should compute depthwise convolution with positive and negative values" in {
    simulate(new DepthwiseConv(5, 5, 2, 3, 3)) { dut =>
      // Input data with 2 channels (using negative and positive values)
      val inData = Seq(
        // Channel 0
        Seq(
          Seq(1.S, 2.S, 1.S, 0.S, 2.S),
          Seq(0.S, 1.S, 2.S, 2.S, 1.S),
          Seq(3.S, 0.S, 1.S, 1.S, 1.S),
          Seq(1.S, 2.S, 0.S, 1.S, 0.S),
          Seq(0.S, 1.S, 2.S, 1.S, 2.S)
        ),
        // Channel 1
        Seq(
          Seq(2.S, 0.S, 1.S, 2.S, 0.S),
          Seq(1.S, 1.S, 0.S, 1.S, 1.S),
          Seq(0.S, 2.S, 2.S, 0.S, 1.S),
          Seq(1.S, 0.S, 1.S, 1.S, 2.S),
          Seq(2.S, 1.S, 0.S, 2.S, 1.S)
        )
      )

      // Depthwise filters with negative values
      val filtData = Seq(
        // Filter for channel 0
        Seq(
          Seq((-1).S, 0.S, 1.S),
          Seq(0.S, (-1).S, 1.S),
          Seq(1.S, 0.S, (-1).S)
        ),
        // Filter for channel 1
        Seq(
          Seq(1.S, 0.S, (-1).S),
          Seq(1.S, (-1).S, 0.S),
          Seq((-1).S, 1.S, 0.S)
        )
      )

      // Set input values
      for (c <- 0 until 2; i <- 0 until 5; j <- 0 until 5) {
        dut.io.input(c)(j)(i).poke(inData(c)(i)(j))
      }

      // Set filter weights
      for (c <- 0 until 2; i <- 0 until 3; j <- 0 until 3) {
        dut.io.weights(c)(0)(j)(i).poke(filtData(c)(i)(j))
      }

      dut.clock.step()

      // Calculate expected outputs for 3x3 output size and verify
      for (c <- 0 until 2) {
        println(s"Output channel $c:")
        for (i <- 0 until 3) {
          for (j <- 0 until 3) {
            var expected = 0
            // For each position in the 3x3 filter
            for (ki <- 0 until 3; kj <- 0 until 3) {
              val inVal = inData(c)(i + ki)(j + kj).litValue.toInt
              val filtVal = filtData(c)(ki)(kj).litValue.toInt
              expected += inVal * filtVal
            }
            
            val observed = dut.io.output(c)(j)(i).peek().litValue.toInt
            println(f"pos=($i,$j): expected=$expected, got=$observed")
            dut.io.output(c)(j)(i).expect(expected.S)
          }
        }
      }
    }
  }
}

class PointwiseConvSpec extends AnyFreeSpec with Matchers {
  "PointwiseConv should compute temporal convolution with positive and negative values" in {
    simulate(new PointwiseConvolution(5, 5, 3, 4)) { dut =>
      // Input data for 3 channels, 5x5 each (with negative values)
      val inData = Seq(
        // Channel 0
        Seq(
          Seq(1.S, 2.S, 1.S, 0.S, 2.S),
          Seq(0.S, 1.S, 2.S, 2.S, 1.S),
          Seq(3.S, 0.S, 1.S, 1.S, 1.S),
          Seq(1.S, 2.S, 0.S, 1.S, 0.S),
          Seq(0.S, 1.S, 2.S, 1.S, 2.S)
        ),
        // Channel 1
        Seq(
          Seq(2.S, 0.S, 1.S, 2.S, 0.S),
          Seq(1.S, 1.S, 0.S, 1.S, 1.S),
          Seq(0.S, 2.S, 2.S, 0.S, 1.S),
          Seq(1.S, 0.S, 1.S, 1.S, 2.S),
          Seq(2.S, 1.S, 0.S, 2.S, 1.S)
        ),
        // Channel 2
        Seq(
          Seq(1.S, 1.S, 1.S, 1.S, 1.S),
          Seq(1.S, 1.S, 1.S, 1.S, 1.S),
          Seq(1.S, 1.S, 1.S, 1.S, 1.S),
          Seq(1.S, 1.S, 1.S, 1.S, 1.S),
          Seq(1.S, 1.S, 1.S, 1.S, 1.S)
        )
      )
      
      // Pointwise filters (4 filters, each with 3 input channel weights, include negative values)
      val filtData = Seq(
        Seq(1, -1, 1),  // Filter 0
        Seq(0, 2, 1),   // Filter 1
        Seq(-1, 0, 1),  // Filter 2
        Seq(-2, 1, 1)   // Filter 3
      )
      
      // Set input values
      for (c <- 0 until 3; i <- 0 until 5; j <- 0 until 5) {
        dut.io.input(c)(j)(i).poke(inData(c)(i)(j))
      }
      
      // Set filter weights
      for (f <- 0 until 4; c <- 0 until 3) {
        dut.io.weights(f)(c)(0)(0).poke(filtData(f)(c).S)
      }
      
      dut.clock.step()
      
      // Verify outputs
      for (f <- 0 until 4) {
        println(s"Output filter $f:")
        for (i <- 0 until 5) {
          for (j <- 0 until 5) {
            // Calculate expected result for each position
            val expected = (0 until 3).map { c =>
              val inVal = inData(c)(i)(j).litValue.toInt
              val filtVal = filtData(f)(c)
              inVal * filtVal
            }.sum
            
            val observed = dut.io.output(f)(j)(i).peek().litValue.toInt
            println(f"pos=($i,$j): expected=$expected, got=$observed")
            dut.io.output(f)(j)(i).expect(expected.S)
          }
        }
      }
    }
  }
}

class RegularConvSpec extends AnyFreeSpec with Matchers {
  "RegularConv should compute full convolution with positive and negative values" in {
    simulate(new RegularConv(5, 5, 2, 3, 3, 3)) { dut =>
      // Input data with 2 channels
      val inData = Seq(
        // Channel 0
        Seq(
          Seq(1.S, 2.S, 2.S, 2.S, 1.S),
          Seq(3.S, 0.S, 3.S, 0.S, 3.S),
          Seq(1.S, 1.S, 1.S, 1.S, 1.S),
          Seq(1.S, 3.S, 3.S, 3.S, 1.S),
          Seq(1.S, 1.S, 1.S, 1.S, 1.S)
        ),
        // Channel 1
        Seq(
          Seq(0.S, 1.S, 0.S, 2.S, 0.S),
          Seq(1.S, 1.S, 0.S, 1.S, 1.S),
          Seq(4.S, 1.S, 0.S, 1.S, 3.S),
          Seq(4.S, 1.S, 0.S, 1.S, 2.S),
          Seq(0.S, 1.S, 0.S, 2.S, 0.S)
        )
      )
      
      // Define 3 filters, each with weights for both input channels (with negative values)
      val filtData = Seq(
        // Filter 0
        Seq(
          // Channel 0 weights
          Seq(
            Seq((-1).S, 0.S, 1.S),
            Seq(0.S, (-1).S, 1.S),
            Seq(0.S, 1.S, (-1).S)
          ),
          // Channel 1 weights
          Seq(
            Seq((-1).S, (-1).S, 1.S),
            Seq((-1).S, 1.S, 0.S),
            Seq(1.S, 0.S, 0.S)
          )
        ),
        // Filter 1
        Seq(
          // Channel 0 weights
          Seq(
            Seq((-1).S, 0.S, 1.S),
            Seq(0.S, (-1).S, 1.S),
            Seq(0.S, 1.S, (-1).S)
          ),
          // Channel 1 weights
          Seq(
            Seq((-1).S, (-1).S, 1.S),
            Seq((-1).S, 1.S, 1.S),
            Seq(1.S, 1.S, 1.S)
          )
        ),
        // Filter 2
        Seq(
          // Channel 0 weights
          Seq(
            Seq((-1).S, (-1).S, 1.S),
            Seq((-1).S, 1.S, 1.S),
            Seq(1.S, 1.S, 1.S)
          ),
          // Channel 1 weights
          Seq(
            Seq((-1).S, (-1).S, 1.S),
            Seq((-1).S, 1.S, 0.S),
            Seq(1.S, 0.S, 0.S)
          )
        )
      )
      
      // Set input values
      for (c <- 0 until 2; j <- 0 until 5; i <- 0 until 5) {
        dut.io.input(c)(j)(i).poke(inData(c)(j)(i))
      }
      
      // Set filter weights
      for (f <- 0 until 3; c <- 0 until 2; j <- 0 until 3; i <- 0 until 3) {
        dut.io.weights(f)(c)(j)(i).poke(filtData(f)(c)(j)(i))
      }
      
      dut.clock.step()
      
      // Calculate expected outputs and verify
      for (f <- 0 until 3) {
        println(s"Output filter $f:")
        for (i <- 0 until 3) {  // output size is (5-3+1)=3
          for (j <- 0 until 3) {
            var expected = 0
            
            // For each input channel
            for (c <- 0 until 2) {
              // For each position in the 3x3 filter
              for (ki <- 0 until 3; kj <- 0 until 3) {
                val inVal = inData(c)(i + ki)(j + kj).litValue.toInt
                val filtVal = filtData(f)(c)(ki)(kj).litValue.toInt
                expected += inVal * filtVal
              }
            }
            
            val observed = dut.io.output(f)(j)(i).peek().litValue.toInt
            println(f"pos=($i,$j): expected=$expected, got=$observed")
            dut.io.output(f)(j)(i).expect(expected.S)
          }
        }
      }
    }
  }
}