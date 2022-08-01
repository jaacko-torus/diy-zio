package jaackotorus
package diy_zio

object zio:
  type Thunk[A] = () => A

  final case class ZIO[A](thunk: Thunk[A]):
    def flatMap[B](a2zioOfB: A => ZIO[B]): ZIO[B] = ZIO.succeed {
      val a      = thunk()
      val zioOfB = a2zioOfB(a)
      val b      = zioOfB.thunk()
      b
    }
    def map[B](a2b: A => B): ZIO[B] = ZIO.succeed {
      val a = thunk()
      val b = a2b(a)
      b
    }
  end ZIO

  object ZIO:
    def succeed[A](a: => A): ZIO[A] =
      ZIO(() => a)

  object console:
    def putStrLn(line: => String) =
      ZIO.succeed(println(line))

    def getStrLn() =
      ZIO.succeed(scala.io.StdIn.readLine())

  object Runtime:
    object default:
      def unsafeRunSync[A](zio: => ZIO[A]): A =
        zio.thunk()
