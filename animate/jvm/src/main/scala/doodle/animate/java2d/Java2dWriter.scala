/*
 * Copyright 2015 noelwelsh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package doodle
package animate
package java2d

import cats.Monoid
import cats.effect.IO
import doodle.algebra.Image
import doodle.effect.Writer.Gif
import doodle.java2d.Drawing
import doodle.java2d.algebra.Algebra
import doodle.java2d.effect.Frame
import java.io.File
import javax.imageio.{IIOImage,ImageIO,ImageWriter}
import javax.imageio.stream.FileImageOutputStream
// import monix.eval.Task
// import monix.execution.Scheduler
import monix.reactive.Observable
// import scala.concurrent.ExecutionContext
// import scala.concurrent.duration._

object Java2dWriter extends Writer[Algebra, Drawing, Frame, Gif] {
  val imageWriter: IO[ImageWriter] =
    IO { ImageIO.getImageWritersByFormatName("gif").next() }

  def writeIterable[A, Alg >: Algebra](file: File,
                                       description: Frame,
                                       frames: Iterable[Image[Alg, Drawing, A]])(
    implicit m: Monoid[A]): IO[A] = {
    for {
      iw <- imageWriter
      _ = iw.setOutput(new FileImageOutputStream(file))
      _ = iw.prepareWriteSequence(null)
      a <- frames.foldLeft(IO(m.empty)){ (ioa, image) =>
        for {
          a  <- ioa
          result <- doodle.java2d.effect.Java2dWriter.renderBufferedImage(description, image)
          (bi, a2) = result
          _ = iw.writeToSequence(new IIOImage(bi, null, null), null)
        } yield m.combine(a, a2)
      }
      _ = iw.endWriteSequence()
      _ = iw.dispose()
    } yield a
  }

  def writeObservable[A, Alg >: Algebra](file: File,
                                         description: Frame,
                                         frames: Observable[Image[Alg, Drawing, A]])(implicit m: Monoid[A]) =
    ???
}
