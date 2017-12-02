package com.github.dnvriend.fp

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NonFatal

trait Functor[F[_]] {
  def map[A, B](fa: F[A])(f: A => B): F[B]
}

trait Applicative[F[_]] extends Functor[F] {
  def pure[A](a: => A): F[A]
  def ap[A, B](fa: F[A])(f: F[A => B]): F[B]
  override def map[A, B](fa: F[A])(f: A => B): F[B] = {
    // to go to F[B]
    // we can just call 'ap', that returns an F[B]
    // ap accepts an fa, and an F[A => B]
    // we can create an F[A => B] from the 'f'
    // by 'lifting' the function 'f' into the context by means of 'pure'
    ap(fa)(pure(f)) // returns F[B]
  }
}

trait Monad[F[_]] extends Applicative[F] {
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  override def ap[A, B](fa: F[A])(f: F[A => B]): F[B] = {
    // we can create an F[B] by calling flatMap
    // by leveraging the A => F[B] contract of flatMap,
    // we can apply flatMap by calling it with 'f' which is 'F[A => B]'
    // which gives us access to 'A => B',
    // we can now call 'map' with 'fa', and giving it 'A => B'
    // which results in the 'A => F[B]' contract
    flatMap(f)(atob => map(fa)(atob))
  }

  override def map[A, B](fa: F[A])(f: A => B): F[B] = {
    // we can create an F[B] by calling flatMap
    // by leveraging the A => F[B] contract of flatMap,
    // we can apply flatMap by calling it with 'F[A]',
    // which gives us access to 'A'
    // we can now apply 'f-with-a', which gives us 'B',
    // if we lift B in the context, it gives us F[B],
    // which results in the 'A => F[B]' contract
    flatMap(fa)(a => pure(f(a)))
  }
}

object Monad {
  def apply[F[_]](implicit m: Monad[F]): Monad[F] = m

  implicit val OptionMonad: Monad[Option] = new Monad[Option] {
    override def pure[A](a: => A): Option[A] = Option.apply(a)

    override def flatMap[A, B](fa: Option[A])(f: A => Option[B]) = {
      fa.flatMap(f)
    }
  }
  implicit val ListMonad: Monad[List] = new Monad[List] {
    override def pure[A](a: => A): List[A] = List(a)

    override def flatMap[A, B](fa: List[A])(f: A => List[B]) = {
      fa.flatMap(f)
    }
  }

  implicit def FutureMonad(implicit ec: ExecutionContext): Monad[Future] =
    new Monad[Future] {
      override def pure[A](a: => A) = Future(a)
      override def flatMap[A, B](fa: Future[A])(f: A => Future[B]) = {
        fa.flatMap(f)
      }
    }

  implicit val MaybeMonad: Monad[Maybe] = new Monad[Maybe] {
    override def pure[A](a: => A): Maybe[A] = Maybe.just(a)
    override def flatMap[A, B](fa: Maybe[A])(f: A => Maybe[B]) = fa match {
      case Just(a) => f(a)
      case Empty() => Maybe.empty[B]
    }
  }
}

object Semigroup {
  def apply[A](implicit semi: Semigroup[A]): Semigroup[A] = semi

  implicit val StringSemi: Semigroup[String] = {
    (a1: String, a2: String) => a1 + a2
  }
}

trait Semigroup[A] {
  def append(a1: A, a2: A): A
}

object Monoid {
  def apply[A](implicit m: Monoid[A]): Monoid[A] = m
  implicit val StringMonoid: Monoid[String] = new Monoid[String] {
    override val zero = ""

    override def append(a1: String, a2: String): String = {
      Semigroup[String].append(a1, a2)
    }
  }
}

trait Monoid[A] extends Semigroup[A] {
  def zero: A
}

object Maybe {
  def empty[A]: Maybe[A] = Empty()

  def just[A](a: A): Maybe[A] = Just(a)

  def fromNullable[A](a: A): Maybe[A] = {
    if (null == a) empty else just(a)
  }

  def fromOption[A](fa: Option[A]): Maybe[A] = fa match {
    case Some(a) => Just(a)
    case None    => empty
  }

  def fromTryCatchNotFatal[T](a: => T): Maybe[T] = try {
    just(a)
  } catch {
    case NonFatal(t) => empty
  }

  def cata[A, B](fa: Maybe[A])(f: A => B, b: => B): B = fa match {
    case Just(a) => f(a)
    case Empty() => b
  }

  implicit def MaybeMonoid[A](implicit sa: Semigroup[A]): Monoid[Maybe[A]] = new Monoid[Maybe[A]] {
    override def zero = Maybe.empty

    override def append(fa1: Maybe[A], fa2: Maybe[A]) = {
      // fa1   | fa2   | effect
      // =======================
      // Just  | Just  | append sa
      // Just  | Empty | fa1
      // Empty | Just  | fa2
      // Empty | Empty | Maybe.empty
      Maybe.cata(fa1)(a1 => {
        Maybe.cata(fa2)(a2 => Maybe.just(sa.append(a1, a2)), fa1)
      }, Maybe.cata(fa2)(_ => fa2, Maybe.empty))
    }
  }
}

sealed trait Maybe[A]
final case class Just[A](a: A) extends Maybe[A]
final case class Empty[A]() extends Maybe[A]