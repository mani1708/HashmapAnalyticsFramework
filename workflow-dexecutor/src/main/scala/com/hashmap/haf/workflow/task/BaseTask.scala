package com.hashmap.haf.workflow.task

import java.util.UUID

import com.hashmap.haf.workflow.closure.SparkTaskClosure

import scala.xml.Elem

abstract class BaseTask[R](val name: String,
													 val id: UUID = UUID.randomUUID(),
													 val to: List[String] = List()) extends SparkTaskClosure[UUID, R]{

	override def getId: UUID = id

	def toXml: Elem
}
