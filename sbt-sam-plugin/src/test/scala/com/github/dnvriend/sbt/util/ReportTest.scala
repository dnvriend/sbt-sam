package com.github.dnvriend.sbt.util

import scalaz._
import scalaz.std.AllInstances._
import ShowInstances._
import com.github.dnvriend.test.TestSpec

object TestReport {
  implicit val show: Show[TestReport] = Show.shows(model => model.str)
}
final case class TestReport(str: String)

class ReportTest extends TestSpec {
  it should "create a report for a list of reports" in {
    Report.report(List(TestReport("a"), TestReport("b"), TestReport("c"))) shouldBe "a\nb\nc"
  }

  it should "create a report for a disjunction failure" in {
    Report.report(Disjunction.left[Throwable, List[String]](new RuntimeException("abc"))) shouldBe "Error, reason: abc"
  }

  it should "create a report for a disjunction success" in {
    Report.report(Disjunction.right[Throwable, List[TestReport]](List(TestReport("a"), TestReport("b"), TestReport("c")))) shouldBe "a\nb\nc"
  }
}
