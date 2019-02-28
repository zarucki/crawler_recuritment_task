package transform

import java.io.PrintWriter

import cats.data.EitherT
import cats.effect.Effect
import cats.implicits._
import fs2.{Stream => FStream}

trait FileWriter[F[_]] {
  def writeToFile(fileName: String, contentToWrite: String)(implicit F: Effect[F]): EitherT[F, Throwable, Unit]
}

class PrintWriterFileWriter[F[_]] extends FileWriter[F] {
  override def writeToFile(fileName: String, contentToWrite: String)(
      implicit F: Effect[F]
  ): EitherT[F, Throwable, Unit] = {
    val acquirePrinter = F.pure(Either.catchNonFatal(new PrintWriter(fileName)))

    def writeAction(pw: Either[Throwable, PrintWriter]): FStream[F, Either[Throwable, Unit]] = {
      FStream.eval(F.pure(pw.map(_.println(contentToWrite))))
    }
    def releaseAction(pw: Either[Throwable, PrintWriter]): F[Unit] = {
      F.pure[Unit](pw.map(_.close()))
    }

    EitherT(
      FStream
        .bracket(acquirePrinter)(use = writeAction, release = releaseAction)
        .compile
        .foldMonoid
    )
  }
}
