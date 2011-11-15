/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
package com.jcraft.jzlib

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.{Test, Before}
import org.junit.Assert._
import org.hamcrest.CoreMatchers._

import java.io.{ByteArrayOutputStream => BAOS, ByteArrayInputStream => BAIS}
import java.io._

import JZlib._

@RunWith(classOf[JUnit4])
class DeflaterInflaterStreamTest {

  @Before
  def setUp = {
  }

  @Test
  def one_by_one = {
    val data1 = randombuf(1024)
    implicit val buf = new Array[Byte](1)

    val baos = new BAOS

    val gos = new DeflaterOutputStream(baos)
    data1 -> gos
    gos.close

    val baos2 = new BAOS
    new InflaterInputStream(new BAIS(baos.toByteArray)) -> baos2
    val data2 = baos2.toByteArray 

    assertThat(data2.length, is(data1.length))
    assertThat(data2, is(data1))
  }

  @Test
  def read_write_with_buf = {

    (1 to 100 by 3).foreach { i =>

      implicit val buf = new Array[Byte](i)

      val data1 = randombuf(10240)

      val baos = new BAOS

      val gos = new DeflaterOutputStream(baos)
      data1 -> gos
      gos.close

      val baos2 = new BAOS
      new InflaterInputStream(new BAIS(baos.toByteArray)) -> baos2
      val data2 = baos2.toByteArray

      assertThat(data2.length, is(data1.length))
      assertThat(data2, is(data1))
    }
  }

  private implicit def readIS(is: InputStream) = new {
      def ->(out: OutputStream)(implicit buf: Array[Byte]) = {
      Stream.continually(is.read(buf)).
                         takeWhile(-1 !=).foreach(i => out.write(buf, 0, i))
      is.close
    }
  }

  private implicit def readArray(is: Array[Byte]) = new {
      def ->(out: OutputStream)(implicit buf: Array[Byte]) = {
        new BAIS(is) -> (out)
    }
  }

  private def randombuf(n: Int) = (0 to n).map{_ =>
    scala.util.Random.nextLong.asInstanceOf[Byte] 
  }.toArray
}
