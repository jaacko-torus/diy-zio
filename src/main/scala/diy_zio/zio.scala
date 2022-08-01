package jaackotorus
package diy_zio
import scala.util.Try

object zio:
  type Thunk[A] = () => A

  final case class ZIO[+E, +A](thunk: Thunk[Either[E, A]]):
    def flatMap[E1 >: E, B](a2zioOfB: A => ZIO[E1, B]): ZIO[E1, B] = ZIO { () =>
      val `e/a` = thunk()
      val `e/zioOfB` = `e/a` match
        case Right(a) => a2zioOfB(a)
        case Left(e)  => ZIO.fail(e)
      val `e/b` = `e/zioOfB`.thunk()
      `e/b`
    }
    def map[B](a2b: A => B): ZIO[E, B] = ZIO { () =>
      val `e/a` = thunk()
      val `e/b` = `e/a` match
        case Right(a) => Right(a2b(a))
        case Left(e)  => Left(e)
      `e/b`
    }
    def catchAll[E2, A1 >: A](handler: E => ZIO[E2, A1]): ZIO[E2, A1] = ZIO { () =>
      val `e/a` = thunk()
      val `e2/zioOfA1` = `e/a` match
        case Right(a1) => ZIO.succeed(a1)
        case Left(e)   => handler(e)
      val `e2/a1` = `e2/zioOfA1`.thunk()
      `e2/a1`
    }
    def mapError[E2](handler: E => E2): ZIO[E2, A] = ZIO { () =>
      val `e/a` = thunk()
      val `e2/a` = `e/a` match
        case Right(a) => Right(a)
        case Left(e)  => Left(handler(e))
      `e2/a`
    }

  object ZIO:
    def succeed[A](a: => A): ZIO[Nothing, A] =
      ZIO { () => Right(a) }
    def fail[E](e: => E): ZIO[E, Nothing] =
      ZIO { () => Left(e) }
    def effect[A](a: => A): ZIO[Throwable, A] =
      ZIO { () => Try(a).toEither }

  object console:
    def putStrLn(line: => String) =
      ZIO.succeed(println(line))

    def getStrLn() =
      ZIO.succeed(scala.io.StdIn.readLine())

  object Runtime:
    object default:
      def unsafeRunSync[E, A](zio: => ZIO[E, A]): Either[E, A] =
        zio.thunk()
