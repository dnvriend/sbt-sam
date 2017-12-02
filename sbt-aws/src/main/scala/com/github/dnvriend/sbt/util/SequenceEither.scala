// Copyright 2017 Dennis Vriend
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.github.dnvriend.sbt.util

object SequenceEither {
  def sequence[A, B](xs: List[Either[A, B]]): Either[List[A], List[B]] = {
    def loop(remaining: List[Either[A, B]], processedLeft: List[A], processedRight: List[B]): Either[List[A], List[B]] = remaining match {
      case Nil if processedLeft.nonEmpty  => Left(processedLeft)
      case Nil if processedRight.nonEmpty => Right(processedRight)
      case Left(head) +: tail             => loop(tail, processedLeft :+ head, processedRight)
      case Right(head) +: tail            => loop(tail, processedLeft, processedRight :+ head)
    }

    loop(xs, List.empty[A], List.empty[B])
  }
}