package conv

import chisel3._
import circt.stage._

object Main extends App {
  // Generate SystemVerilog for Depthwise Convolution
  ChiselStage.emitSystemVerilogFile(
    new DepthwiseConv(
      h_in = 5, w_in = 5, d_in = 2, 
      h_filt = 3, w_filt = 3
    ),
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
  )
  
  // Generate SystemVerilog for Pointwise Convolution
  ChiselStage.emitSystemVerilogFile(
    new PointwiseConvolution(
      h_in = 5, w_in = 5, d_in = 3, d_out = 4
    ),
    args = Array("--target-dir", "pointwise_output"),
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
  )
  
  // Generate SystemVerilog for Regular Convolution
  ChiselStage.emitSystemVerilogFile(
    new RegularConv(
      h_in = 5, w_in = 5, d_in = 2,
      d_out = 3, k_h = 3, k_w = 3
    ),
    args = Array("--target-dir", "regular_conv_output"),
    firtoolOpts = Array("-disable-all-randomization", "-strip-debug-info")
  )
}