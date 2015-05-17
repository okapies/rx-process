package com.github.okapies.rx.process

import java.io.BufferedInputStream
import java.nio.ByteBuffer
import java.nio.charset.{Charset, CodingErrorAction}

import org.scalatest._
import rx.Observable
import rx.functions.Func2

import scala.collection.JavaConverters._

class OperatorByteBufferToStringSpec extends FlatSpec with Matchers {

  "OperatorByteBufferToString" should "decode chunks of UTF-8 string split at any point" in {
    val charset = Charset.forName("UTF-8")

    val orig = "諸行無常_是生滅法_生滅滅已_寂滅爲樂" // TODO: appropriate string for conformance testing
    val bs = orig.getBytes(charset)
    val totalLen = bs.length

    for (chunkLen <- 1 to totalLen) {
      withClue(s"[chunk length = $chunkLen]") {
        val chunks = Observable.from(split(ByteBuffer.wrap(bs), 0 until totalLen by chunkLen).asJava)
        val op = new OperatorByteBufferToString(charset)
          .malformedInputAction(CodingErrorAction.REPLACE)
          .unmappableCharacterAction(CodingErrorAction.REPLACE)
          .replacement("\uFFFD")
        val decoded = chunks.lift[String](op).reduce(concat).toBlocking.single
        decoded shouldEqual orig
      }
    }
  }

  // see http://www.cl.cam.ac.uk/~mgk25/ucs/examples/UTF-8-test.txt
  it should "decode Markus Kuhn's capability and stress test file" in {
    val charset = Charset.forName("UTF-8")

    val is = new BufferedInputStream(getClass.getResource("/UTF-8-test.txt").openStream())
    val bs = Stream.continually(is.read).takeWhile(_ != -1).map(_.toByte).toArray
    is.close()
    val o = Observable.just(ByteBuffer.wrap(bs))

    val orig = new String(bs, charset) // String uses CodingErrorAction.REPLACE by default
    val op = new OperatorByteBufferToString(charset)
        .malformedInputAction(CodingErrorAction.REPLACE)
        .unmappableCharacterAction(CodingErrorAction.REPLACE)
        .replacement("\uFFFD")
    val decoded = o.lift[String](op).toBlocking.single
    decoded shouldEqual orig
  }

  private[this] def split(src: Array[Byte], range: Range): Seq[Array[Byte]] =
    split(src, range, src.length)

  private[this] def split(src: Array[Byte], range: Range, length: Int): Seq[Array[Byte]] =
    ((range takeWhile (_ < length)) :+ length).sliding(2).map { case Seq(from, until) =>
      val len = until - from
      val dst = new Array[Byte](len)
      Array.copy(src, from, dst, 0, len)
      dst
    }.toSeq

  private[this] def split(src: ByteBuffer, range: Range): Seq[ByteBuffer] =
    split(src, range, src.remaining())

  private[this] def split(src: ByteBuffer, range: Range, length: Int): Seq[ByteBuffer] =
    ((range takeWhile (_ < length)) :+ length).sliding(2).map { case Seq(from, until) =>
      val len = until - from
      val bs = src.slice()
      bs.limit(len)
      val dst = ByteBuffer.allocateDirect(len)
      dst.put(bs)
      dst.flip()
      src.position(src.position + len)
      dst
    }.toSeq

  private[this] val concat = new Func2[String, String, String] {
    override def call(a: String, b: String): String = a + b
  }

}
