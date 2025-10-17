# Chisel Convolution Modules

## Project Overview
This repository contains the hardware design, software references, and lab documentation for EE310 Lab 4. The project develops parameterizable depthwise, pointwise, and regular 2D convolution blocks in Chisel, exercises them with signed data, and cross-checks behavior against Python reference scripts. The workflow targets rapid iteration from algorithm exploration to synthesizable SystemVerilog suitable for downstream FPGA or ASIC tooling.

## Features
- **Chisel Implementations**: Modular depthwise, pointwise, and full convolutions backed by a reusable signed dot-product primitive.
- **Configurable Arithmetic**: Parameter inputs define tensor dimensions, filter sizes, and channel counts to explore different convolution footprints.
- **Simulation & Testing**: ScalaTest suites run on EphemeralSimulator to validate each module with positive and negative stimuli.
- **SystemVerilog Generation**: `ChiselStage.emitSystemVerilogFile` creates clean RTL for each convolution block with FIRTool optimizations.
- **Python References**: `dw.py`, `pw.py`, and `rc.py` reproduce the same operations in NumPy for quick sanity checks or dataset creation.
- **Lab Artifacts**: PDF reports document design goals, derivations, and experimental outcomes for the course submission.

## Repository Structure
```
Chisel_Convolution_Module/
├── build.sbt                         # sbt project definition and Chisel/ScalaTest dependencies
├── project/build.properties          # sbt version pin
├── src/main/scala/conv/Conv.scala    # Depthwise, pointwise, and regular convolution hardware
├── src/main/scala/conv/Main.scala    # Entry point that emits SystemVerilog for each module
├── src/test/scala/conv/ConvSpec.scala # ScalaTest specs exercising all convolution variants
├── dw.py | pw.py | rc.py             # NumPy reference scripts for each convolution style
├── random_sample.py                  # Helper that generates randomized Scala tests (WIP)
├── EE310_Lab_4.pdf                   # Lab handout and assignment notes
├── EE310_lab4_report_yagizebil.pdf   # Final report summarizing design and results
├── target/                           # sbt build artifacts
└── venv/                             # Optional Python virtual environment
```

## How to Build and Run
1. **Clone the Repository**
   ```bash
   git clone https://github.com/YagizEbil/ee310_lab4_yagizebil.git
   cd ee310_lab4_yagizebil
   ```

2. **Install Scala/Chisel Tooling**  
   Make sure Java 11+, `sbt`, and a recent Scala toolchain are available. Resolve dependencies and compile the project:
   ```bash
   sbt compile
   ```

3. **Run Hardware Tests**  
   Execute the ScalaTest suites to simulate the convolution blocks with signed data sets:
   ```bash
   sbt test
   ```

4. **Generate SystemVerilog**  
   Emit RTL for each module. Depthwise output lands in the project root, while pointwise and regular convolution modules are written into dedicated folders:
   ```bash
   sbt "runMain conv.Main"
   ```
   Inspect the generated `.sv` files before integrating them into downstream toolflows.

5. **Verify with Python References (Optional)**  
   Use NumPy scripts to reproduce expected outputs or craft new stimuli. A lightweight setup looks like:
   ```bash
   python3 -m venv venv
   source venv/bin/activate
   pip install numpy

   python dw.py   # depthwise convolution walk-through
   python pw.py   # pointwise convolution walk-through
   python rc.py   # regular convolution walk-through
   ```
   Script logs detail every multiply-accumulate term, making it easy to match against simulation traces.

## Additional Resources
- `EE310_Lab_4.pdf` outlines the original assignment prompt and design constraints.
- `EE310_lab4_report_yagizebil.pdf` documents the final architecture, verification strategy, and conclusions.

## Authors
- [Kadir Yagiz Ebil](https://github.com/YagizEbil)

## License
No license file has been provided. Please contact the author before reusing this work outside the course setting.
