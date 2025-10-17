#Unfinished code

import random

def rand_int(low=-10, high=10):
    """Generate a random integer in [low, high]."""
    return random.randint(low, high)

def format_seq(seq, indent=0):
    """Format a nested Python list as Scala Seq(...) with .S suffixes."""
    sp = " " * indent
    if not seq:
        return "Seq()"
    if isinstance(seq[0], list):
        inner = ",\n".join(format_seq(s, indent+2) for s in seq)
        return f"Seq(\n{inner}\n{sp})"
    else:
        return "Seq(" + ", ".join(f"{n}.S" for n in seq) + ")"

with open("generated_tests.txt", "w") as f:
    # Header
    f.write("""package conv

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.EphemeralSimulator._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

""")

    # --- DotProductSpec ---
    a = [rand_int() for _ in range(3)]
    b = [rand_int() for _ in range(3)]
    expected_dp = sum(x*y for x,y in zip(a,b))
    f.write("""class DotProductSpec extends AnyFreeSpec with Matchers {
  "DotProduct should compute correct output with random values" in {
    simulate(new DotProduct(3)) { dut =>
""")
    for i, ai in enumerate(a):
        f.write(f"      dut.io.a({i}).poke({ai}.S)\n")
    f.write("\n")
    for i, bi in enumerate(b):
        f.write(f"      dut.io.b({i}).poke({bi}.S)\n")
    f.write(f"""
      val expected = ({' + '.join(f"{x}*{y}" for x,y in zip(a,b))}).S

      dut.io.result.expect(expected)

      println(s"DotProduct test - a: ({', '.join(map(str,a))}), b: ({', '.join(map(str,b))}), expected: $expected, got: ${{dut.io.result.peek().litValue}}")
""")
    f.write("""
    }}
  }}
}}
""")

    # --- DepthwiseConvSpec ---
    in_channels = 2; H = W = 5; K = 3
    inData = [[[rand_int() for _ in range(W)] for _ in range(H)] for _ in range(in_channels)]
    filtData = [[[rand_int() for _ in range(K)] for _ in range(K)] for _ in range(in_channels)]
    f.write(f"""
class DepthwiseConvSpec extends AnyFreeSpec with Matchers {{
  "DepthwiseConv should compute depthwise convolution with random values" in {{
    simulate(new DepthwiseConv({H}, {W}, {in_channels}, {K}, {K})) {{ dut =>
      // random input data
""")
    for c in range(in_channels):
        f.write(f"      // channel {c}\n")
        for i in range(H):
            row = ", ".join(f"{inData[c][i][j]}.S" for j in range(W))
            f.write(f"      dut.io.input({c})({i}) := VecInit(Seq({row}))\n")
        f.write("\n")
    f.write("      // random filters\n")
    for c in range(in_channels):
        f.write(f"      // filter channel {c}\n")
        for i in range(K):
            row = ", ".join(f"{filtData[c][i][j]}.S" for j in range(K))
            f.write(f"      dut.io.weights({c})(0)({i}) := VecInit(Seq({row}))\n")
    f.write("""
      dut.clock.step()

      // You would compute & expect here as before...
    }}
  }}
}}
""")

    # --- PointwiseConvSpec ---
    in_channels = 3; H = W = 5; out_channels = 4
    inDataPW = [[[rand_int() for _ in range(W)] for _ in range(H)] for _ in range(in_channels)]
    filtDataPW = [[rand_int() for _ in range(in_channels)] for _ in range(out_channels)]
    f.write(f"""
class PointwiseConvSpec extends AnyFreeSpec with Matchers {{
  "PointwiseConv should compute pointwise convolution with random values" in {{
    simulate(new PointwiseConvolution({H}, {W}, {in_channels}, {out_channels})) {{ dut =>
      // random input data
""")
    for c in range(in_channels):
        f.write(f"      // channel {c}\n")
        for i in range(H):
            row = ", ".join(f"{inDataPW[c][i][j]}.S" for j in range(W))
            f.write(f"      dut.io.input({c})({i}) := VecInit(Seq({row}))\n")
        f.write("\n")
    f.write("      // random pointwise filters\n")
    for f_idx in range(out_channels):
        weights = ", ".join(f"{w}.S" for w in filtDataPW[f_idx])
        f.write(f"      dut.io.weights({f_idx}) := VecInit(Seq({weights}))\n")
    f.write("""
      dut.clock.step()

      // You would compute & expect here as before...
    }}
  }}
}}
""")

    # --- RegularConvSpec ---
    in_channels = 2; H = W = 5; out_filters = 3; K = 3
    inDataR = [[[rand_int() for _ in range(W)] for _ in range(H)] for _ in range(in_channels)]
    filtDataR = [[[[rand_int() for _ in range(K)] for _ in range(K)] for _ in range(in_channels)] for _ in range(out_filters)]
    f.write(f"""
class RegularConvSpec extends AnyFreeSpec with Matchers {{
  "RegularConv should compute full convolution with random values" in {{
    simulate(new RegularConv({H}, {W}, {in_channels}, {K}, {K}, {out_filters})) {{ dut =>
      // random input data
""")
    for c in range(in_channels):
        f.write(f"      // channel {c}\n")
        for i in range(H):
            row = ", ".join(f"{inDataR[c][i][j]}.S" for j in range(W))
            f.write(f"      dut.io.input({c})({i}) := VecInit(Seq({row}))\n")
        f.write("\n")
    f.write("      // random filters\n")
    for filt in range(out_filters):
        f.write(f"      // filter {filt}\n")
        for c in range(in_channels):
            for i in range(K):
                row = ", ".join(f"{filtDataR[filt][c][i][j]}.S" for j in range(K))
                f.write(f"      dut.io.weights({filt})({c})({i}) := VecInit(Seq({row}))\n")
    f.write("""
      dut.clock.step()

      // You would compute & expect here as before...
    }}
  }}
}}
""")

print("Done! Wrote randomized tests to generated_tests.txt")