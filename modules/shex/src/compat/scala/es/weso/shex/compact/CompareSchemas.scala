package es.weso.shex.compact

import java.io.File

import cats._
import cats.data._
import com.typesafe.config.{Config, ConfigFactory}
import es.weso.json.JsonTest
import es.weso.shex._
import es.weso.shex.implicits.encoderShEx._
import es.weso.shex.implicits.decoderShEx._
import es.weso.shex.implicits.eqShEx._
import es.weso.shex.implicits.showShEx._
import es.weso.utils.FileUtils._
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.{EitherValues, FunSpec, Matchers}

import scala.io._
import scala.util.{Failure, Success}

class CompareSchemas extends FunSpec with JsonTest with Matchers with EitherValues {

  val conf: Config = ConfigFactory.load()
  val schemasFolder = conf.getString("schemasFolder")

  val ignoreFiles = List("coverage")

  def getCompactFiles(schemasDir: String): List[File] = {
    getFilesFromFolderWithExt(schemasDir, "shex", ignoreFiles)
  }

  describe("Parsing Schemas from ShEx") {
    for(file <- getCompactFiles(schemasFolder)) {
      it(s"Should read Schema from file ${file.getName}") {
        val str = Source.fromFile(file)("UTF-8").mkString
        Schema.fromString(str,"SHEXC",None) match {
          case Success(schema) => {
            val (name,ext) = splitExtension(file.getName)
            val jsonFile = schemasFolder + "/"+ name + ".json"
            val jsonStr = Source.fromFile(jsonFile)("UTF-8").mkString
            decode[Schema](jsonStr) match {
              case Left(err) => fail(s"Error parsing $jsonFile: $err")
              case Right(expectedSchema) =>
                if (Eq[Schema].eqv(schema,expectedSchema)) {
                 info("Jsons are equal")
                } else {
               fail(s"Json's are different. Parsed:\n${schema}\n-----Expected:\n${expectedSchema}")
            }
          }}
          case Failure(err) => fail(s"Parsing error: $err")
        }
      }
    }
  }
}
