package com.github.okapies.paas

import scala.concurrent.{ExecutionContext, Future}
import scala.sys.process.{BasicIO, Process, ProcessBuilder, ProcessIO, ProcessLogger}

trait ProcessAsyncImplicits {
  implicit class ProcessBuilderOps(pb: ProcessBuilder) extends AnyRef {
    def ?(implicit ec: ExecutionContext) = runAsync(pb, connectInput = false)
    def ?(io: ProcessIO)(implicit ec: ExecutionContext) = runAsync(pb, io)
    def ?(log: ProcessLogger)(implicit ec: ExecutionContext) = runAsync(pb, log, connectInput = false)
    def ?<(implicit ec: ExecutionContext) = runAsync(pb, connectInput = true)
    def ?<(log: ProcessLogger)(implicit ec: ExecutionContext) = runAsync(pb, log, connectInput = true)

    def ??(implicit ec: ExecutionContext) = slurp(pb, None, withIn = false)
    def ??(log: ProcessLogger)(implicit ec: ExecutionContext) = slurp(pb, Some(log), withIn = false)
    def ??<(implicit ec: ExecutionContext) = slurp(pb, None, withIn = true)
    def ??<(log: ProcessLogger)(implicit ec: ExecutionContext) = slurp(pb, Some(log), withIn = true)
  }

  private[this] def runAsync(
      pb: ProcessBuilder,
      connectInput: Boolean)(implicit ec: ExecutionContext): Future[Int] =
    Future(pb.run(connectInput).exitValue())

  private[this] def runAsync(
      pb: ProcessBuilder,
      io: ProcessIO)(implicit ec: ExecutionContext): Future[Int] =
    Future(pb.run(io).exitValue())

  private[this] def runAsync(
      pb: ProcessBuilder,
      log: ProcessLogger,
      connectInput: Boolean)(implicit ec: ExecutionContext): Future[Int] =
    Future(log buffer pb.run(log, connectInput).exitValue())

  private[this] def slurp(
      pb: ProcessBuilder,
      log: Option[ProcessLogger],
      withIn: Boolean)(implicit ec: ExecutionContext): Future[String] = {
    val buffer = new StringBuffer
    (pb ? BasicIO(withIn, buffer, log)).map {
      case 0 => buffer.toString
      case code => scala.sys.error("Nonzero exit value: " + code)
    }
  }
}
