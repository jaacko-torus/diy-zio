package jaackotorus
package diy_zio.diy
import scala.annotation.targetName
import scala.reflect.ClassTag
import scala.util.Try

object zio:
  final class ZIO[-R, +E, +A](val run: R => Either[E, A]):
    def flatMap[R1 <: R, E1 >: E, B](a2zb: A => ZIO[R1, E1, B]): ZIO[R1, E1, B] =
      ZIO { r => run(r).fold(ZIO.fail, a2zb).run(r) }

    def zip[R1 <: R, E1 >: E, B](that: ZIO[R1, E1, B]): ZIO[R1, E1, (A, B)] =
      for
        a <- this
        b <- that
      yield a -> b

    def map[B](a2b: A => B): ZIO[R, E, B] =
      ZIO { r => run(r).map(a2b) }

    def catchAll[R1 <: R, E1, A1 >: A](handler: E => ZIO[R1, E1, A1]): ZIO[R1, E1, A1] =
      ZIO { r => run(r).fold(handler, ZIO.succeed).run(r) }

    def mapError[E1](handler: E => E1): ZIO[R, E1, A] =
      ZIO { r => run(r).left.map(handler) }

    def provide(r: => R): ZIO[Any, E, A] =
      ZIO { _ => run(r) }

    def provideSome[R1](f: R1 => R): ZIO[R1, E, A] =
      ZIO.accessM(r1 => provide(f(r1)))

    // CUSTOM
    def provideCustom[R1: ClassTag](r1: => R1)(using
        Has[ZEnv] & Has[R1] => R,
    ): ZIO[Has[ZEnv], E, A] =
      provideCustomLayer(Has(r1))

    // CUSTOM
    def provideCustomLayer[R1 <: Has[?]](r1: => R1)(using
        Has[ZEnv] & R1 => R,
    ): ZIO[Has[ZEnv], E, A] =
      provideSome[Has[ZEnv]](_.`union`(r1).asInstanceOf[R])

    def provideLayer[R1, E1 >: E](layer: ZLayer[R1, E1, R]): ZIO[R1, E1, A] =
      layer.zio.flatMap((r => provide(r)))

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
    type Console = Has[Console.Service]

    object Console:
      trait Service:
        def putStrLn(line: => String): ZIO[Any, Nothing, Unit]
        def getStrLn(): ZIO[Any, Nothing, Unit]

      lazy val live: ZLayer[Any, Nothing, Console] =
        ZLayer.succeed(make)

      lazy val make: Service = new:
        def putStrLn(line: => String) =
          ZIO.succeed(println(line))
        def getStrLn() =
          ZIO.succeed(scala.io.StdIn.readLine())

    def putStrLn(line: => String): ZIO[Console, Nothing, Unit] =
      ZIO.accessM(_.get.putStrLn(line))

    def getStrLn(): ZIO[Console, Nothing, Unit] =
      ZIO.accessM(_.get.getStrLn())

  object Runtime:
    object default:
      def unsafeRunSync[E, A](zio: => ZIO[ZEnv, E, A]): Either[E, A] =
        zio.provideLayer((ZEnv.live)).run(())

  type ZEnv = console.Console
  object ZEnv:
    lazy val live: ZLayer[Any, Nothing, ZEnv] =
      console.Console.live

  final class Has[A] private (private val map: Map[String, Any])

  object Has:
    def apply[A](a: A)(using tag: ClassTag[A]): Has[A] =
      new Has(Map(tag.toString -> a))

    extension [A <: Has[?]](a: A)
      def `union`[B <: Has[?]](b: B): A & B = new Has(a.map ++ b.map).asInstanceOf[A & B]

      infix inline def ++[B <: Has[?]](b: B): A & B = a `union` b

      def get[S](using view: A => Has[S])(using tag: ClassTag[S]): S =
        a.map(tag.toString).asInstanceOf[S]

  final class ZLayer[-R, +E, +A](val zio: ZIO[R, E, A]):
    inline def flatMap[R1 <: R, E1 >: E, B](a2zb: A => ZLayer[R1, E1, B]): ZLayer[R1, E1, B] =
      ZLayer(this.zio.flatMap(a => a2zb(a).zio))

    inline def zip[R1 <: R, E1 >: E, B](that: ZLayer[R1, E1, B]): ZLayer[R1, E1, (A, B)] =
      ZLayer(this.zio.zip(that.zio))

    inline def map[B](a2b: A => B): ZLayer[R, E, B] =
      ZLayer(this.zio.map(a2b))

    inline def provide(r: => R): ZLayer[Any, E, A] =
      ZLayer(this.zio.provide(r))

    inline def provideSome[R1](f: R1 => R): ZLayer[R1, E, A] =
      ZLayer(this.zio.provideSome(f))

  object ZLayer:
    def succeed[A: ClassTag](a: => A): ZLayer[Any, Nothing, Has[A]] =
      ZLayer(ZIO.succeed(Has(a)))

    def fromService[R <: Has[S], S: ClassTag, A: ClassTag](f: S => A): ZLayer[R, Nothing, Has[A]] =
      ZLayer(ZIO.fromFunction { r => Has(f(r.get[S])) })

    def fromServices[R <: Has[S1] & Has[S2], S1: ClassTag, S2: ClassTag, A: ClassTag](
        f: (S1, S2) => A,
    ): ZLayer[R, Nothing, Has[A]] =
      ZLayer(ZIO.fromFunction { r => Has(f(r.get[S1], r.get[S2])) })
