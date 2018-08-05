package goa.util

object RicherString {

  implicit final class RicherStringImplicitClass(private val orig: String) extends AnyVal {

    def isBlank: Boolean = {
      orig == null || orig.trim.isEmpty
    }

    def nonBlank: Boolean = !isBlank
  }

}
