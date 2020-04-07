package bleak

import org.specs2.mutable.Specification

class HeadersSpec extends Specification {

  "Headers.add" should {
    "add" in {
      Headers.empty
        .add("foo", "bar")
        .get("foo") should_=== Some("bar")
    }

    "add iterable" in {
      Headers.empty
        .add("foo", Seq("bar", "bar1"))
        .getAll("foo") should_== Seq("bar", "bar1")
    }
  }

  "Headers.remove" should {
    "remove" in {
      Headers.empty
        .add("foo", "bar")
        .remove("foo")
        .get("foo") should_=== None
    }
  }

}
