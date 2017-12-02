package com.github.dnvriend.ops

import scalaz.{ @@, Tag }

trait AnyOps {
  implicit def ToUnwrapAnyOps[A, TAG](that: A @@ TAG): ToUnwrapAnyOps[A, TAG] = new ToUnwrapAnyOps[A, TAG](that)
  implicit def ToWrapAnyOps[A](that: A): ToWrapAnyOps[A] = new ToWrapAnyOps[A](that)
}
class ToUnwrapAnyOps[A, TAG](that: A @@ TAG) {
  def unwrap: A = Tag.unwrap(that)
}
class ToWrapAnyOps[A](that: A) {
  def wrap[TAG]: A @@ TAG = Tag[A, TAG](that)
}

