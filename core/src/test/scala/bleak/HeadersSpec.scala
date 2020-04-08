package bleak

import org.specs2.mutable.Specification

class HeadersSpec extends Specification {

  "Headers.add" should {
    "add header into headers" in {
      Headers.empty
        .add("foo", "bar")
        .get("foo") should_=== Some("bar")
    }

    "add a batch of elements into headers" in {
      Headers.empty
        .add("foo", Seq("bar", "bar1"))
        .getAll("foo") should_== Seq("bar", "bar1")
    }
  }

  "Headers.remove" should {
    "remove element from empty headers" in {
      Headers.empty
        .remove("foo")
        .get("foo") should_=== None
    }

    "remove element by name" in {
      Headers.empty
        .add("foo", "bar")
        .remove("foo")
        .get("foo") should_=== None
    }
  }

  "Headers.getAll" should {
    "get all duplicate elements from headers" in {
      val headers = Headers("foo" -> "bar", "foo" -> "bar")
      headers.getAll("foo") should_== Iterable("bar", "bar")
    }

    "get all elements from headers" in {
      val headers = Headers("foo" -> "bar", "foo" -> "bar1")
      headers.getAll("foo") should_== Iterable("bar", "bar1")
    }
  }

  "Headers.get" should {
    "get one element" in {
      Headers("foo" -> "bar")
        .get("foo") should_=== Some("bar")
    }
  }

  "Headers.iterator" should {
    "return a iterator for headers" in {
      val headers = Headers("foo" -> "bar", "hello" -> "world")
      headers.iterator.to(Iterable) should_== Iterable("foo" -> "bar", "hello" -> "world")
    }
  }
}
