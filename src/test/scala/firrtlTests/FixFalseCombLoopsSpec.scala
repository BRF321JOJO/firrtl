package firrtlTests

import firrtl.options.Dependency
import firrtl.testutils.LeanTransformSpec
import firrtl.transforms.{CheckCombLoops, EnableFixFalseCombLoops}

class FixFalseCombLoopsSpec extends LeanTransformSpec(Seq(Dependency[CheckCombLoops])) {

  //TODO: Add tests for, "Did not modify circuit"
  //These should be circuits we officially do not support (at least yet)

  "False combinational loop" should "not throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input c : UInt<1>
                  |    input d : UInt<1>
                  |    output a_output : UInt<2>
                  |    output b_output : UInt<1>
                  |    wire a : UInt<2>
                  |    wire b : UInt<1>
                  |
                  |    a <= cat(b, c)
                  |    b <= xor(bits(a, 0, 0), d)
                  |    a_output <= a
                  |    b_output <= b
                  |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))

    val resultSerialized = result.circuit.serialize
    val correctForm = """circuit hasloops :
                        |  module hasloops :
                        |    input clk : Clock
                        |    input c : UInt<1>
                        |    input d : UInt<1>
                        |    output a_output : UInt<2>
                        |    output b_output : UInt<1>
                        |
                        |    wire a0 : UInt<1>
                        |    wire a1 : UInt<1>
                        |    node a = cat(a1, a0)
                        |    wire b : UInt<1>
                        |    a_output <= a
                        |    b_output <= b
                        |    a0 <= c
                        |    a1 <= b
                        |    b <= xor(a0, d)
                        |""".stripMargin

    if (resultSerialized == correctForm) {
      print("Output has correct form\n")
    } else {
      print("ERROR: Incorrect output form\n")
    }

    print(resultSerialized)
    compile(parse(resultSerialized))

  }

  "False combinational loop with an intermediate variable" should "not throw an exception" in {
    val input =
      """circuit hasloops :
        |  module hasloops :
        |    input clk : Clock
        |    input c : UInt<1>
        |    input d : UInt<1>
        |    output a_output : UInt<2>
        |    output b_output : UInt<1>
        |    wire a : UInt<2>
        |    wire e : UInt<2>
        |    wire b : UInt<1>
        |
        |    a <= e
        |    e <= cat(b, c)
        |    b <= xor(bits(a, 0, 0), d)
        |    a_output <= a
        |    b_output <= b
        |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))
    val resultSerialized = result.circuit.serialize
    print(resultSerialized)
    compile(parse(resultSerialized))
  }

  "False combinational loop where primitive inside cat" should "not throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input c : UInt<1>
                  |    input d : UInt<1>
                  |    input e : UInt<1>
                  |    output a_output : UInt<2>
                  |    output b_output : UInt<1>
                  |    wire a : UInt<2>
                  |    wire b : UInt<1>
                  |
                  |    a <= cat(xor(b, e), c)
                  |    b <= xor(bits(a, 0, 0), d)
                  |    a_output <= a
                  |    b_output <= b
                  |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))
    val resultSerialized = result.circuit.serialize
    print(resultSerialized)
    compile(parse(resultSerialized))
  }

  "False loop where there are two loops" should "not throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input d : UInt<1>
                  |    input e : UInt<1>
                  |    output a_output : UInt<2>
                  |    output b_output : UInt<1>
                  |    output c_output : UInt<2>
                  |    wire a : UInt<2>
                  |    wire b : UInt<1>
                  |    wire c : UInt<2>
                  |
                  |    a <= cat(b, bits(c, 0, 0))
                  |    b <= xor(bits(a, 0, 0), d)
                  |    c <= cat(bits(a, 1, 1), e)
                  |    a_output <= a
                  |    b_output <= b
                  |    c_output <= c
                  |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))
    val resultSerialized = result.circuit.serialize
    print(resultSerialized)
    compile(parse(resultSerialized))
  }

  "False loop with subword in a cat" should "not throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input c : UInt<1>
                  |    input d : UInt<1>
                  |    output a_output : UInt<3>
                  |    output b_output : UInt<2>
                  |    wire a : UInt<2>
                  |    wire b : UInt<1>
                  |
                  |    a <= cat(b, c)
                  |    b <= cat(bits(a, 0, 0), d)
                  |    a_output <= a
                  |    b_output <= b
                  |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))
    val resultSerialized = result.circuit.serialize
    print(resultSerialized)
    compile(parse(resultSerialized))
  }

  "False loop where output uses subword" should "not throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input c : UInt<1>
                  |    input d : UInt<1>
                  |    output a_output : UInt<3>
                  |    output b_output : UInt<2>
                  |    wire a : UInt<2>
                  |    wire b : UInt<1>
                  |
                  |
                  |    a <= cat(b, c)
                  |    b <= cat(bits(a, 0, 0), d)
                  |    a_output <= bits(a, 0, 0)
                  |    b_output <= b
                  |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))
    val resultSerialized = result.circuit.serialize
    print(resultSerialized)
    compile(parse(resultSerialized))
  }

  "False loop where a narrower wire is assigned to a wider value" should "not throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input c : UInt<3>
                  |    input d : UInt<1>
                  |    output a_output : UInt<4>
                  |    output b_output : UInt<2>
                  |    wire a : UInt<4>
                  |    wire b : UInt<2>
                  |
                  |    a <= cat(b, c)
                  |    b <= cat(bits(a, 0, 0), d)
                  |    a_output <= a
                  |    b_output <= b
                  |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))
    val resultSerialized = result.circuit.serialize
    print(resultSerialized)
    compile(parse(resultSerialized))
  }

  "False loop where output uses a wider subword" should "not throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input c : UInt<3>
                  |    input d : UInt<1>
                  |    output a_output : UInt<4>
                  |    output b_output : UInt<2>
                  |    wire a : UInt<4>
                  |    wire b : UInt<2>
                  |
                  |    a <= cat(b, c)
                  |    b <= cat(bits(a, 0, 0), d)
                  |    a_output <= bits(a, 3, 2)
                  |    b_output <= b
                  |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))
    val resultSerialized = result.circuit.serialize
    print(resultSerialized)
    compile(parse(resultSerialized))
  }

  //TODO: fix
  "False loop with nested cat with multiple bits" should "not throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input c : UInt<2>
                  |    input d : UInt<2>
                  |    output a_output : UInt<6>
                  |    output b_output : UInt<2>
                  |    wire a : UInt<6>
                  |    wire b : UInt<2>
                  |
                  |    a <= cat(b, cat(c, d))
                  |    b <= bits(a, 0, 0)
                  |    a_output <= a
                  |    b_output <= b
                  |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))
    val resultSerialized = result.circuit.serialize
    print(resultSerialized)
    compile(parse(resultSerialized))
  }

  "False loop where subword is over multiple values" should "not throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input d : UInt<1>
                  |    input e : UInt<1>
                  |    input f : UInt<1>
                  |    output a_output : UInt<3>
                  |    output d_output : UInt<3>
                  |    wire a : UInt<3>
                  |
                  |    a <= cat(d, cat(e, f))
                  |    d_output <= bits(a, 2, 0)
                  |    a_output <= a
                  |""".stripMargin

    val result = compile(parse(input), Seq(EnableFixFalseCombLoops))
    val resultSerialized = result.circuit.serialize
    print(resultSerialized)
    compile(parse(resultSerialized))
  }

  //All tests below should error/are not currently handled by this pass.
  "Combinational loop through a combinational memory read port" should "throw an exception" in {
    val input = """circuit hasloops :
                  |  module hasloops :
                  |    input clk : Clock
                  |    input a : UInt<1>
                  |    input b : UInt<1>
                  |    output c : UInt<1>
                  |    output d : UInt<1>
                  |    wire y : UInt<1>
                  |    wire z : UInt<1>
                  |    c <= b
                  |    mem m :
                  |      data-type => UInt<1>
                  |      depth => 2
                  |      read-latency => 0
                  |      write-latency => 1
                  |      reader => r
                  |      read-under-write => undefined
                  |    m.r.clk <= clk
                  |    m.r.addr <= y
                  |    m.r.en <= UInt(1)
                  |    z <= m.r.data
                  |    y <= z
                  |    d <= z
                  |""".stripMargin

    intercept[CheckCombLoops.CombLoopException] {
      compile(parse(input), Seq(EnableFixFalseCombLoops))
    }
  }

}
