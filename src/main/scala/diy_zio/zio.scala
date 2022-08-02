package jaackotorus
package diy_zio
import scala.util.Try

object zio:
  final class ZIO[-R, +E, +A](val run: R => Either[E, A]):
    def flatMap[R1 <: R, E1 >: E, B](a2zb: A => ZIO[R1, E1, B]): ZIO[R1, E1, B] =
      ZIO { r => run(r).fold(ZIO.fail, a2zb).run(r) }
    def map[B](a2b: A => B): ZIO[R, E, B] =
      ZIO { r => run(r).map(a2b) }
    def catchAll[R1 <: R, E1, A1 >: A](handler: E => ZIO[R1, E1, A1]): ZIO[R1, E1, A1] =
      ZIO { r => run(r).fold(handler, ZIO.succeed).run(r) }
    def mapError[E1](handler: E => E1): ZIO[R, E1, A] =
      ZIO { r => run(r).left.map(handler) }
    def provide(r: => R): ZIO[Any, E, A] =
      ZIO { _ => run(r) }

  object ZIO:
    def succeed[A](a: => A): ZIO[Any, Nothing, A] =
      ZIO { _ => Right(a) }
    def fail[E](e: => E): ZIO[Any, E, Nothing] =
      ZIO { _ => Left(e) }
    def effect[A](a: => A): ZIO[Any, Throwable, A] =
      ZIO { _ => Try(a).toEither }
    def fromFunction[R, A](run: R => A): ZIO[R, Nothing, A] =
      ZIO { r => Right(run(r)) }
    def identity[R]: ZIO[R, Nothing, R] =
      ZIO.fromFunction(Predef.identity)
    inline def environment[R]: ZIO[R, Nothing, R] =
      identity
    inline def access[R]: AccessPartiallyApplied[R] =
      AccessPartiallyApplied()

    final class AccessPartiallyApplied[R]():
      def apply[A](f: R => A): ZIO[R, Nothing, A] =
        environment.map(f)

    inline def accessM[R]: AccessMPartiallyApplied[R] =
      AccessMPartiallyApplied()

    final class AccessMPartiallyApplied[R]():
      def apply[E, A](f: R => ZIO[R, E, A]): ZIO[R, E, A] =
        environment.flatMap(f)

  object console:
    def putStrLn(line: => String) =
      ZIO.succeed(println(line))

    def getStrLn() =
      ZIO.succeed(scala.io.StdIn.readLine())

  object Runtime:
    object default:
      def unsafeRunSync[R, E, A](zio: => ZIO[ZEnv, E, A]): Either[E, A] =
        zio.run(())

type ZEnv = Unit
