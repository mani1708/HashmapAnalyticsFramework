package com.hashmap.haf.functions.summarize

import com.hashmap.haf.annotations.IgniteFunction
import com.hashmap.haf.datastore.impl.{IgniteSparkDFStore, SparkDFOptions}
import com.hashmap.haf.functions.constants.TaskConfigurationConstants._
import com.hashmap.haf.functions.services.ServiceFunction
import org.apache.ignite.Ignite
import org.apache.ignite.resources.IgniteInstanceResource
import org.apache.ignite.services.ServiceContext
import org.apache.spark.sql
import org.apache.spark.sql.ignite.IgniteSparkSession
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import scala.concurrent.Future
import scala.util.{Failure, Success}

@IgniteFunction(functionClazz = "SummarizeSparkTask", service = "summarizeService",
  configs = Array())
class SparkSummarizeService extends ServiceFunction{

  var appName = ""
  @IgniteInstanceResource
  var ignite: Ignite = _


  override def run(inputKey: String, outputKey: String, functionArguments: Map[String, String], configurations: Map[String, String]): String = {
    println("Executing Spark Summarize.....")
    val (sparkAppName: String, sparkMaster: String, tableParameters: String) = getConfigurations(configurations)

    val igniteSession = IgniteSparkSession.builder()
      .appName(sparkAppName)
      .master(sparkMaster)
      //.config("spark.executor.instances", "2")
      //Only additional option to refer to Ignite cluster.
      //.igniteConfig("/path/to/ignite/config.xml")
      .getOrCreate()

    val spark = igniteSession.newSession()
    /*val spark: SparkSession = SparkSession
      .builder()
      .appName(sparkAppName)
      .master(sparkMaster)
      .getOrCreate()
*/

    val cache = IgniteSparkDFStore

    val df = cache.get(SparkDFOptions(spark, inputKey, tableParameters))
    val newDs: DataFrame = processInputDF(df)
    cache.set(newDs, SparkDFOptions(spark, outputKey, tableParameters))
    println("Setting to output cache .......showing only 10 rows of it")
    newDs.show(10)
    println("Closing spark context")
    spark.close()
    println("Successfully executed Summarize")
    "successful"
  }

  private def processInputDF(df: DataFrame) = {
    val metadata = MetadataHandler.get(df)
    val newMetaData = CommonUtils.getAllSummarizeOperations.foldLeft(metadata)((meta, op) => op(meta, df)).build()
    val newDs = MetadataHandler.set(df, newMetaData)
    newDs
  }

  private def getConfigurations(configurations: Map[String, String]) = {
    val sparkAppName = configurations.getOrElse(SPARK_APP_NAME, throw new IllegalArgumentException(SPARK_APP_NAME + " not provided in configurations"))
    val sparkMaster = configurations.getOrElse(SPARK_MASTER, throw new IllegalArgumentException(SPARK_MASTER + " not provided in configurations"))
    val tableParameters = configurations.getOrElse(TABLE_PARAMETERS, "template=partitioned")
    (sparkAppName, sparkMaster, tableParameters)
  }

  override def cancel(ctx: ServiceContext) = println("Cancelled")

  override def init(ctx: ServiceContext) = {
    appName = ctx.name()
  }

  override def execute(ctx: ServiceContext) = println("Executing")
}


object SummarizeOperations{
  def count = (metadata: MetadataBuilder, df: DataFrame) => {
    metadata.putLong("total_records", df.count())
  }
}

object SummarizeOperationsOnEachColumn{


  def numericalUnaryOperations = (metadata: MetadataBuilder, df: DataFrame) => {
    import org.apache.spark.sql.functions.{col, max, mean, min}

    type AggregateFunctions = Column => Column
    val operations: List[AggregateFunctions] = List(min, max, mean)
    val fields = CommonUtils.filterNumericColumns(df)
    val fieldNames = fields.map(field => field.name)
    val allExprTups = (for(fName <- fieldNames; nAgg <- operations) yield nAgg(col(fName))).toSeq


    val newDf = df.agg(allExprTups.head, allExprTups.tail:_*)

    newDf.first()
      .getValuesMap(newDf.schema.fieldNames)
      .foldLeft(metadata)((meta, tup2) => meta.putString(tup2._1, tup2._2.toString))
  }

  def commonUnaryOperations = (metadata: MetadataBuilder, df: DataFrame) => {
    import org.apache.spark.sql.functions.{approx_count_distinct, col}

    type AggregateFunctions = Column => Column
    val operations: Seq[AggregateFunctions] = List(approx_count_distinct)
    val fields = CommonUtils.filterNumericColumns(df)
    val fieldNames = fields.map(field => field.name)
    val allExprTups = (for(fName <- fieldNames; nAgg <- operations) yield nAgg(col(fName))).toSeq


    val newDf = df.agg(allExprTups.head, allExprTups.tail:_*)

    newDf.first()
      .getValuesMap(newDf.schema.fieldNames)
      .foldLeft(metadata)((meta, tup2) => meta.putString(tup2._1, tup2._2.toString))
  }


}

object CommonUtils {
  type SummaryFunction = (MetadataBuilder, sql.DataFrame) => MetadataBuilder

  def filterNumericColumns(df: DataFrame) = {
    df.schema.fields
      .filter(x => x.dataType == IntegerType || x.dataType == DoubleType || x.dataType == LongType)
  }

  val getAllSummarizeOperations: Seq[SummaryFunction] = {
    import SummarizeOperations._
    List(count)
    //todo fix numeric unary operations - its giving null pointer
    //List(numericalUnaryOperations, count)
  }

}


object MetadataHandler {
  def get(df: DataFrame) = {
    val meta = new sql.types.MetadataBuilder()
    df.schema.filter(field => field.name == "app_metadata").headOption match {
      case Some(field) => meta.withMetadata(field.metadata)
      case _ => meta
    }
  }

  def set(df: DataFrame, newMetadata: Metadata) = {
    val newColumn = df.col(df.schema.fieldNames.apply(0)).as("app_metadata", newMetadata)
    df.withColumn("app_metadata", newColumn)
  }

}