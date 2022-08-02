package jaackotorus
package diy_zio
import scala.util.Try

object zio:
  final class ZIO[-R, +E, +A](val run: R => Either[E, A]):
    def flatMap[R1 <: R, E1 >: E, B](a2zioOfB: A => ZIO[R1, E1, B]): ZIO[R1, E1, B] = ZIO { r =>
      val `e/a` = run(r)
      val `e/zioOfB` = `e/a` match
        case Right(a) => a2zioOfB(a)
        case Left(e)  => ZIO.fail(e)
      val `e/b` = `e/zioOfB`.run(r)
      `e/b`
    }
    def map[B](a2b: A => B): ZIO[R, E, B] = ZIO { r =>
      val `e/a` = run(r)
      val `e/b` = `e/a` match
        case Right(a) => Right(a2b(a))
        case Left(e)  => Left(e)
      `e/b`
    }
    def catchAll[R1 <: R, E2, A1 >: A](handler: E => ZIO[R1, E2, A1]): ZIO[R1, E2, A1] = ZIO { r =>
      val `e/a` = run(r)
      val `e2/zioOfA1` = `e/a` match
        case Right(a1) => ZIO.succeed(a1)
        case Left(e)   => handler(e)
      val `e2/a1` = `e2/zioOfA1`.run(r)
      `e2/a1`
    }
    def mapError[E2](handler: E => E2): ZIO[R, E2, A] = ZIO { (r) =>
      val `e/a` = run(r)
      val `e2/a` = `e/a` match
        case Right(a) => Right(a)
        case Left(e)  => Left(handler(e))
      `e2/a`
    }
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
