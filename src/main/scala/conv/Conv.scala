package conv

import chisel3._
import circt.stage._

// class DotProduct(val length: Int) extends Module {
//   val io = IO(new Bundle {
//     val a = Input(Vec(length, UInt(8.W)))
//     val b = Input(Vec(length, UInt(8.W)))
//     val result = Output(UInt(32.W))
//   })

//   val products = Wire(Vec(length, UInt(16.W)))
//   for (i <- 0 until length) {
//     products(i) := io.a(i) * io.b(i)
//   }

//   io.result := products.reduce(_ + _)
// }

// abstract class BaseConvolution( //standardization base rather than a logic container, it dictates the interface of the subclasses.
//                                 //this is a base class for all convolution types, it will not be instantiated directly.
//                                 //all implementation will be inside the subclasses: DepthwiseConv, PointwiseConv, and RegularConv.
//                     val h_in: Int, w_in: Int, d_in: Int,
//                     val h_filt: Int, w_filt: Int, d_filt: Int, num_filt: Int, 
//                     val h_out: Int, w_out: Int, d_out: Int
// ) extends Module {
//   val io = IO(new Bundle {
//     val input = Input(Vec(d_in,Vec(w_in, Vec(h_in, UInt(8.W))))) //3d structure
//     val weights = Input(Vec(num_filt, Vec(d_filt, Vec(w_filt, Vec(h_filt, UInt(8.W)))))) //4d structure
//     val output = Output(Vec(num_filt, Vec(w_out, Vec(h_out, UInt(32.W))))) //3d structure
//   })

//     /* 

//     op           in = h x w x d                          filter = (h x w x d) x num_filt                                        output = h x w x d 
//   _____________________________________________________________________________________________________________________________________________________________________
//     dw  |       h_in x w_in x d_in         |               (h_filt x w_filt x 1) x d_in               |          (h_in - h_filt + 1) x (w_in - w_filt + 1) x d_in 
//     ----|----------------------------------|----------------------------------------------------------|----------------------------------------------------------------
//     pw  |       h_in x w_in x d_in         |                 (1 x 1 x d_in) x d_out                   |                         h_in x w_in x d_out
//     ----|----------------------------------|----------------------------------------------------------|----------------------------------------------------------------
//     rc  |       h_in x w_in x d_in         |             (h_filt x w_filt x d_in) x d_out             |          (h_in - h_filt + 1) x (w_in - w_filt + 1) x d_out
//     ----|----------------------------------|----------------------------------------------------------|----------------------------------------------------------------

//      */
// }
// //Below is a self-type annotation in Scala. It's used to express that the trait SpatialCompute requires
// // being mixed into a class that is (or inherits from) BaseConvolution.
// trait SpatialCompute { 
// this: BaseConvolution => def spatialConvolve2D(
//                         input2D: Vec[Vec[UInt]],
//                         kernel2D: Vec[Vec[UInt]]
//                         ): Vec[Vec[UInt]] = 
// {
//     val h_in = input2D.head.length
//     val w_in = input2D.length
//     val h_filt = kernel2D.head.length
//     val w_filt = kernel2D.length

//     val h_out = h_in - h_filt + 1
//     val w_out = w_in - w_filt + 1

//     val output = Wire(Vec(w_out, Vec(h_out, UInt(32.W))))

//     for (i <- 0 until w_out) {
//       for (j <- 0 until h_out) {
//         val patch = Wire(Vec(w_filt, Vec(h_filt, UInt(8.W))))
//         for (ki <- 0 until w_filt) {
//           for (kj <- 0 until h_filt) {
//             patch(ki)(kj) := input2D(i + ki)(j + kj)
//           }
//         }

//         val dot = Module(new DotProduct(w_filt * h_filt))
//         dot.io.a := patch.flatten
//         dot.io.b := kernel2D.flatten

//         output(i)(j) := dot.io.result
//       }
//     }

//     output //returning the output of the convolution operation
//   }
// }

// class DepthwiseConv(
//   h_in: Int, w_in: Int, d_in: Int,
//   h_filt: Int, w_filt: Int,
// ) extends BaseConvolution(
//   h_in, w_in, d_in,
//   h_filt, w_filt, 1, d_in,
//   (h_in - h_filt + 1), (w_in - w_filt + 1), d_in
// ) with SpatialCompute {

//     val w_out_local = (w_in - w_filt + 1)
//     val h_out_local = (h_in - h_filt + 1)
//     for (c <- 0 until d_in) {
//         val input2D = io.input(c)
//         val kernel2D = io.weights(c)(0)
//         val out2D = spatialConvolve2D(input2D, kernel2D)

//         for (i <- 0 until w_out_local) {
//             for (j <- 0 until h_out_local) {
//                 io.output(c)(i)(j) := out2D(i)(j)(31, 0) //limiting to 32 bits
//             }
//         }
//     }
// }

// trait TemporalCompute { 
//   this: BaseConvolution => def temporalConvolve(
//     input3D: Vec[Vec[Vec[UInt]]],  // (d_in x w x h)
//     kernel: Vec[UInt]              // (d_in)
//   ): UInt = {
//     val d = input3D.length
    
//     // For each position in the input at coordinates (i,j),
//     // compute the dot product across all channels
//     val dot = Module(new DotProduct(d))
    
//     // Extract values at position (i,j) across all channels
//     val inputChannels = Wire(Vec(d, UInt(8.W)))
//     for (c <- 0 until d) {
//       inputChannels(c) := input3D(c)(0)(0) // Default to position (0,0) - will be used with proper indices
//     }
    
//     // Connect to dot product module
//     dot.io.a := inputChannels
//     dot.io.b := kernel
    
//     // Return result
//     dot.io.result
//   }
// }

// // class PointwiseConvolution(
// //   h_in: Int, w_in: Int, d_in: Int, d_out: Int
// // ) extends BaseConvolution(
// //   h_in, w_in, d_in,
// //   1, 1, d_in, d_out,
// //   h_in, w_in, d_out
// // ) with TemporalCompute {

// //   // For each output channel and each spatial position,
// //   // apply the temporal convolution
// //   for (f <- 0 until d_out) {
// //     for (i <- 0 until w_out) {
// //       for (j <- 0 until h_out) {
// //         // Extract the 1D kernel weights for this filter
// //         val kernel = Wire(Vec(d_in, UInt(8.W)))
// //         for (c <- 0 until d_in) {
// //           kernel(c) := io.weights(f)(c)(0)(0)  // For pointwise: filter shape is 1x1
// //         }
        
// //         // At each spatial position (i,j), compute dot product across all channels
// //         io.output(f)(i)(j) := temporalConvolve(io.input, kernel)
// //       }
// //     }
// //   }
// // }

// class PointwiseConvolution(
//   h_in: Int, w_in: Int, d_in: Int, d_out: Int
// ) extends BaseConvolution(
//   h_in, w_in, d_in,
//   1, 1, d_in, d_out,
//   h_in, w_in, d_out
// ) with TemporalCompute {

//   // For each output channel and each spatial position,
//   // apply the temporal convolution
//   for (f <- 0 until d_out) {
//     for (i <- 0 until w_in) {
//       for (j <- 0 until h_in) {
//         // Extract the 1D kernel weights for this filter
//         val kernel = Wire(Vec(d_in, UInt(8.W)))
//         for (c <- 0 until d_in) {
//           kernel(c) := io.weights(f)(c)(0)(0)  // For pointwise: filter shape is 1x1
//         }
        
//         // Extract the input values at this position across all channels
//         val inputAtPos = Wire(Vec(d_in, Vec(1, Vec(1, UInt(8.W)))))
//         for (c <- 0 until d_in) {
//           inputAtPos(c)(0)(0) := io.input(c)(i)(j)
//         }
        
//         // Apply the temporal convolution
//         io.output(f)(i)(j) := temporalConvolve(inputAtPos, kernel)
//       }
//     }
//   }
// }

// class RegularConvSpec extends AnyFreeSpec with Matchers {
//   "RegularConv should compute full convolution for 5x5x2 input with 3 output filters" in {
//     simulate(new RegularConv(5, 5, 2, 3, 3, 3)) { dut =>
//       // Input data with 2 channels
//       val inData = Seq(
//         // Channel 0 - a pattern of alternating values
//         Seq(
//           Seq(1.U, 2.U, 2.U, 2.U, 1.U),
//           Seq(3.U, 0.U, 3.U, 0.U, 3.U),
//           Seq(1.U, 1.U, 1.U, 1.U, 1.U),
//           Seq(1.U, 3.U, 3.U, 3.U, 1.U),
//           Seq(1.U, 1.U, 1.U, 1.U, 1.U)
//         ),
//         // Channel 1 - another pattern
//         Seq(
//           Seq(0.U, 1.U, 0.U, 2.U, 0.U),
//           Seq(1.U, 1.U, 0.U, 1.U, 1.U),
//           Seq(4.U, 1.U, 0.U, 1.U, 3.U),
//           Seq(4.U, 1.U, 0.U, 1.U, 2.U),
//           Seq(0.U, 1.U, 0.U, 2.U, 0.U)
//         )
//       )
      
//       // Define 3 filters, each with weights for both input channels
//       val filtData = Seq(
//         // Filter 0
//         Seq(
//           // Channel 0 weights
//           Seq(
//             Seq(-1, 0, 1),
//             Seq(0, -1, 1),
//             Seq(0, 1, -1)
//           ),
//           // Channel 1 weights
//           Seq(
//             Seq(-1, -1, 1),
//             Seq(-1, 1, 0),
//             Seq(1, 0, 0)
//           )
//         ),
//         // Filter 1
//         Seq(
//           // Channel 0 weights
//           Seq(
//             Seq(-1, 0, 1),
//             Seq(0, -1, 1),
//             Seq(0, 1, -1)
//           ),
//           // Channel 1 weights
//           Seq(
//             Seq(-1, -1, 1),
//             Seq(-1, 1, 1),
//             Seq(1, 1, 1)
//           )
//         ),
//         // Filter 2
//         Seq(
//           // Channel 0 weights
//           Seq(
//             Seq(-1, -1, 1),
//             Seq(-1, 1, 1),
//             Seq(1, 1, 1)
//           ),
//           // Channel 1 weights
//           Seq(
//             Seq(-1, -1, 1),
//             Seq(-1, 1, 0),
//             Seq(1, 0, 0)
//           )
//         )
//       )
      
//       // Set input values
//       for (c <- 0 until 2; j <- 0 until 5; i <- 0 until 5) {
//         dut.io.input(c)(j)(i).poke(inData(c)(j)(i))
//       }
      
//       // Set filter weights
//       for (f <- 0 until 3; c <- 0 until 2; j <- 0 until 3; i <- 0 until 3) {
//         dut.io.weights(f)(c)(j)(i).poke(filtData(f)(c)(j)(i).U)
//       }
      
//       dut.clock.step()
      
//       // Calculate expected outputs and verify
//       for (f <- 0 until 3) {
//         println(s"Output filter $f:")
//         for (i <- 0 until 3) {  // output size is (5-3+1)=3
//           for (j <- 0 until 3) {
//             var expected = 0
            
//             // For each input channel
//             for (c <- 0 until 2) {
//               // For each position in the 3x3 filter
//               for (ki <- 0 until 3; kj <- 0 until 3) {
//                 val inVal = inData(c)(i + ki)(j + kj).litValue.toInt
//                 val filtVal = filtData(f)(c)(ki)(kj)
//                 expected += inVal * filtVal
//               }
//             }
            
//             val observed = dut.io.output(f)(j)(i).peek().litValue.toInt
//             println(f"pos=($i,$j): expected=$expected, got=$observed")
//             dut.io.output(f)(j)(i).expect(expected.U)
//           }
//         }
//       }
//     }
//   }
// }

// class DotProduct(val length: Int) extends Module {
//   val io = IO(new Bundle {
//     val a = Input(Vec(length, UInt(8.W)))
//     val b = Input(Vec(length, UInt(8.W)))
//     val result = Output(UInt(32.W))
//   })

//   val products = Wire(Vec(length, UInt(16.W)))
//   for (i <- 0 until length) {
//     products(i) := io.a(i) * io.b(i)
//   }

//   io.result := products.reduce(_ + _)
// }

// abstract class BaseConvolution(
//                     val h_in: Int, w_in: Int, d_in: Int,
//                     val h_filt: Int, w_filt: Int, d_filt: Int, num_filt: Int, 
//                     val h_out: Int, w_out: Int, d_out: Int
// ) extends Module {
//   val io = IO(new Bundle {
//     val input = Input(Vec(d_in,Vec(w_in, Vec(h_in, UInt(8.W))))) //3d structure
//     val weights = Input(Vec(num_filt, Vec(d_filt, Vec(w_filt, Vec(h_filt, UInt(8.W)))))) //4d structure
//     val output = Output(Vec(num_filt, Vec(w_out, Vec(h_out, UInt(32.W))))) //3d structure
//   })
// }

// trait SpatialCompute { 
//   this: BaseConvolution => def spatialConvolve2D(
//                         input2D: Vec[Vec[UInt]],
//                         kernel2D: Vec[Vec[UInt]]
//                         ): Vec[Vec[UInt]] = 
//   {
//     val h_in = input2D.head.length
//     val w_in = input2D.length
//     val h_filt = kernel2D.head.length
//     val w_filt = kernel2D.length

//     val h_out = h_in - h_filt + 1
//     val w_out = w_in - w_filt + 1

//     val output = Wire(Vec(w_out, Vec(h_out, UInt(32.W))))

//     for (i <- 0 until w_out) {
//       for (j <- 0 until h_out) {
//         val patch = Wire(Vec(w_filt, Vec(h_filt, UInt(8.W))))
//         for (ki <- 0 until w_filt) {
//           for (kj <- 0 until h_filt) {
//             patch(ki)(kj) := input2D(i + ki)(j + kj)
//           }
//         }

//         val dot = Module(new DotProduct(w_filt * h_filt))
//         dot.io.a := patch.flatten
//         dot.io.b := kernel2D.flatten

//         output(i)(j) := dot.io.result
//       }
//     }

//     output
//   }
// }

// class DepthwiseConv(
//   h_in: Int, w_in: Int, d_in: Int,
//   h_filt: Int, w_filt: Int,
// ) extends BaseConvolution(
//   h_in, w_in, d_in,
//   h_filt, w_filt, 1, d_in,
//   (h_in - h_filt + 1), (w_in - w_filt + 1), d_in
// ) with SpatialCompute {

//     val w_out_local = (w_in - w_filt + 1)
//     val h_out_local = (h_in - h_filt + 1)
//     for (c <- 0 until d_in) {
//         val input2D = io.input(c)
//         val kernel2D = io.weights(c)(0)
//         val out2D = spatialConvolve2D(input2D, kernel2D)

//         for (i <- 0 until w_out_local) {
//             for (j <- 0 until h_out_local) {
//                 io.output(c)(i)(j) := out2D(i)(j)(31, 0)
//             }
//         }
//     }
// }

// trait TemporalCompute { 
//   this: BaseConvolution => def temporalConvolve(
//     input3D: Vec[Vec[Vec[UInt]]],  // (d_in x w x h)
//     kernel: Vec[UInt]              // (d_in)
//   ): UInt = {
//     val d = input3D.length
    
//     val dot = Module(new DotProduct(d))
    
//     val inputChannels = Wire(Vec(d, UInt(8.W)))
//     for (c <- 0 until d) {
//       inputChannels(c) := input3D(c)(0)(0) // Default to position (0,0)
//     }
    
//     dot.io.a := inputChannels
//     dot.io.b := kernel
    
//     dot.io.result
//   }
// }

// class PointwiseConvolution(
//   h_in: Int, w_in: Int, d_in: Int, d_out: Int
// ) extends BaseConvolution(
//   h_in, w_in, d_in,
//   1, 1, d_in, d_out,
//   h_in, w_in, d_out
// ) with TemporalCompute {

//   for (f <- 0 until d_out) {
//     for (i <- 0 until w_in) {
//       for (j <- 0 until h_in) {
//         val kernel = Wire(Vec(d_in, UInt(8.W)))
//         for (c <- 0 until d_in) {
//           kernel(c) := io.weights(f)(c)(0)(0)
//         }
        
//         val inputAtPos = Wire(Vec(d_in, Vec(1, Vec(1, UInt(8.W)))))
//         for (c <- 0 until d_in) {
//           inputAtPos(c)(0)(0) := io.input(c)(i)(j)
//         }
        
//         io.output(f)(i)(j) := temporalConvolve(inputAtPos, kernel)
//       }
//     }
//   }
// }

// class RegularConv(
//   h_in: Int, w_in: Int, d_in: Int,
//   d_out: Int, k_h: Int, k_w: Int
// ) extends BaseConvolution(
//   h_in, w_in, d_in,
//   k_h, k_w, d_in, d_out,
//   (h_in - k_h + 1), (w_in - k_w + 1), d_out
// ) {
//   val h_out_local = h_in - k_h + 1
//   val w_out_local = w_in - k_w + 1

//   for (f <- 0 until d_out) {
//     for (j <- 0 until w_out_local) {
//       for (i <- 0 until h_out_local) {
//         // Compute convolution across channels using dot-product modules
//         val dotResults: Seq[UInt] = (0 until d_in).map { c =>
//           // Flatten the k_h x k_w patch for channel c
//           val patch      = Wire(Vec(k_h * k_w, UInt(8.W)))
//           val weightsVec = Wire(Vec(k_h * k_w, UInt(8.W)))
//           var idx = 0
//           for (kh <- 0 until k_h; kw <- 0 until k_w) {
//             // FIXED: Changed index order to match Python indexing pattern
//             // The test expects inputs to be accessed as io.input(c)(i + kh)(j + kw)
//             patch(idx)      := io.input(c)(i + kh)(j + kw)
//             weightsVec(idx) := io.weights(f)(c)(kh)(kw)
//             idx += 1
//           }
//           // Instantiate and connect a dot-product module
//           val dot = Module(new DotProduct(k_h * k_w))
//           dot.io.a := patch
//           dot.io.b := weightsVec
//           dot.io.result
//         }
//         // Sum per-channel results combinationally
//         io.output(f)(j)(i) := dotResults.reduce(_ + _)
//       }
//     }
//   }
// }

class DotProduct(val length: Int) extends Module {
  val io = IO(new Bundle {
    val a = Input(Vec(length, SInt(8.W)))  
    val b = Input(Vec(length, SInt(8.W)))  
    val result = Output(SInt(32.W))        
  })

  val products = Wire(Vec(length, SInt(16.W)))  
  for (i <- 0 until length) {
    products(i) := io.a(i) * io.b(i)  // Multiplication preserves sign
  }

  io.result := products.reduce(_ + _)  // Addition preserves sign
}

abstract class BaseConvolution(
                    val h_in: Int, w_in: Int, d_in: Int,
                    val h_filt: Int, w_filt: Int, d_filt: Int, num_filt: Int, 
                    val h_out: Int, w_out: Int, d_out: Int
) extends Module {
  val io = IO(new Bundle {
    val input = Input(Vec(d_in, Vec(w_in, Vec(h_in, SInt(8.W)))))      
    val weights = Input(Vec(num_filt, Vec(d_filt, Vec(w_filt, Vec(h_filt, SInt(8.W))))))  
    val output = Output(Vec(num_filt, Vec(w_out, Vec(h_out, SInt(32.W)))))  
  })
}

trait SpatialCompute { 
  this: BaseConvolution => def spatialConvolve2D(
                        input2D: Vec[Vec[SInt]],  
                        kernel2D: Vec[Vec[SInt]]  
                        ): Vec[Vec[SInt]] =        
  {
    val h_in = input2D.head.length
    val w_in = input2D.length
    val h_filt = kernel2D.head.length
    val w_filt = kernel2D.length

    val h_out = h_in - h_filt + 1
    val w_out = w_in - w_filt + 1

    val output = Wire(Vec(w_out, Vec(h_out, SInt(32.W))))  

    for (i <- 0 until w_out) {
      for (j <- 0 until h_out) {
        val patch = Wire(Vec(w_filt, Vec(h_filt, SInt(8.W))))
        for (ki <- 0 until w_filt) {
          for (kj <- 0 until h_filt) {
            patch(ki)(kj) := input2D(i + ki)(j + kj)
          }
        }

        val dot = Module(new DotProduct(w_filt * h_filt))
        dot.io.a := patch.flatten
        dot.io.b := kernel2D.flatten

        output(i)(j) := dot.io.result
      }
    }

    output
  }
}

class DepthwiseConv(
  h_in: Int, w_in: Int, d_in: Int,
  h_filt: Int, w_filt: Int,
) extends BaseConvolution(
  h_in, w_in, d_in,
  h_filt, w_filt, 1, d_in,
  (h_in - h_filt + 1), (w_in - w_filt + 1), d_in
) with SpatialCompute {

    val w_out_local = (w_in - w_filt + 1)
    val h_out_local = (h_in - h_filt + 1)
    for (c <- 0 until d_in) {
        val input2D = io.input(c)
        val kernel2D = io.weights(c)(0)
        val out2D = spatialConvolve2D(input2D, kernel2D)

        for (i <- 0 until w_out_local) {
            for (j <- 0 until h_out_local) {
                io.output(c)(i)(j) := out2D(i)(j)
            }
        }
    }
}

trait TemporalCompute { 
  this: BaseConvolution => def temporalConvolve(
    input3D: Vec[Vec[Vec[SInt]]],  
    kernel: Vec[SInt]               
  ): SInt = {                       
    val d = input3D.length
    
    val dot = Module(new DotProduct(d))
    
    val inputChannels = Wire(Vec(d, SInt(8.W)))  
    for (c <- 0 until d) {
      inputChannels(c) := input3D(c)(0)(0) // Default to position (0,0)
    }
    
    dot.io.a := inputChannels
    dot.io.b := kernel
    
    dot.io.result
  }
}

class PointwiseConvolution(
  h_in: Int, w_in: Int, d_in: Int, d_out: Int
) extends BaseConvolution(
  h_in, w_in, d_in,
  1, 1, d_in, d_out,
  h_in, w_in, d_out
) with TemporalCompute {

  for (f <- 0 until d_out) {
    for (i <- 0 until w_in) {
      for (j <- 0 until h_in) {
        val kernel = Wire(Vec(d_in, SInt(8.W)))  
        for (c <- 0 until d_in) {
          kernel(c) := io.weights(f)(c)(0)(0)
        }
        
        val inputAtPos = Wire(Vec(d_in, Vec(1, Vec(1, SInt(8.W)))))  
        for (c <- 0 until d_in) {
          inputAtPos(c)(0)(0) := io.input(c)(i)(j)
        }
        
        io.output(f)(i)(j) := temporalConvolve(inputAtPos, kernel)
      }
    }
  }
}

// class RegularConv(
//   h_in: Int, w_in: Int, d_in: Int,
//   d_out: Int, k_h: Int, k_w: Int
// ) extends BaseConvolution(
//   h_in, w_in, d_in,
//   k_h, k_w, d_in, d_out,
//   (h_in - k_h + 1), (w_in - k_w + 1), d_out
// ) {
//   val h_out_local = h_in - k_h + 1
//   val w_out_local = w_in - k_w + 1

//   for (f <- 0 until d_out) {
//     for (j <- 0 until w_out_local) {
//       for (i <- 0 until h_out_local) {
//         // Compute convolution across channels using dot-product modules
//         val dotResults: Seq[SInt] = (0 until d_in).map { c =>  
//           // Flatten the k_h x k_w patch for channel c
//           val patch      = Wire(Vec(k_h * k_w, SInt(8.W)))      
//           val weightsVec = Wire(Vec(k_h * k_w, SInt(8.W)))      
//           var idx = 0
//           for (kh <- 0 until k_h; kw <- 0 until k_w) {
//             // Keeping the index order to match Python indexing pattern
//             patch(idx)      := io.input(c)(i + kh)(j + kw)
//             weightsVec(idx) := io.weights(f)(c)(kh)(kw)
//             idx += 1
//           }
//           // Instantiate and connect a dot-product module
//           val dot = Module(new DotProduct(k_h * k_w))
//           dot.io.a := patch
//           dot.io.b := weightsVec
//           dot.io.result
//         }
//         // Sum per-channel results combinationally
//         io.output(f)(j)(i) := dotResults.reduce(_ + _)
//       }
//     }
//   }
// }

class RegularConv(
  h_in: Int, w_in: Int, d_in: Int,
  d_out: Int, k_h: Int, k_w: Int
) extends BaseConvolution(
  h_in, w_in, d_in,
  k_h, k_w, d_in, d_out,
  (h_in - k_h + 1), (w_in - k_w + 1), d_out
) with TemporalCompute {
  val h_out_local = h_in - k_h + 1
  val w_out_local = w_in - k_w + 1

  for (f <- 0 until d_out) {
    for (j <- 0 until w_out_local) {
      for (i <- 0 until h_out_local) {
        
        val spatialRegions = Reg(Vec(d_in, Vec(1, Vec(1, SInt(8.W)))))
        
        // Calculate the result for each channel separately and sum them
        var totalSum = 0.S(32.W)
        
        for (c <- 0 until d_in) {
          val spatialFilter = Reg(Vec(k_h * k_w, SInt(8.W)))
          var idx = 0
          for (kh <- 0 until k_h; kw <- 0 until k_w) {
            spatialFilter(idx) := io.weights(f)(c)(kh)(kw)
            idx += 1
          }
          
          val spatialRegion = Reg(Vec(k_h * k_w, SInt(8.W)))
          idx = 0
          for (kh <- 0 until k_h; kw <- 0 until k_w) {
            spatialRegion(idx) := io.input(c)(i + kh)(j + kw)
            idx += 1
          }
          
          val dot = Module(new DotProduct(k_h * k_w))
          dot.io.a := spatialRegion
          dot.io.b := spatialFilter
          
          // Add this channel's result to the total
          totalSum = totalSum + dot.io.result
        }
        
        io.output(f)(j)(i) := totalSum
      }
    }
  }
}