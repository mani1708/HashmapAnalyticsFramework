package com.hashmap.haf.execution

import org.apache.ignite.internal.IgnitionEx
import org.apache.ignite.{Ignite, Ignition}
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.{SpringApplication, SpringBootConfiguration}
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.feign.EnableFeignClients
import org.springframework.context.annotation.{Bean, ComponentScan}
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootConfiguration
@EnableDiscoveryClient
@EnableFeignClients
@EnableAutoConfiguration
@ComponentScan(Array("com.hashmap.haf"))
@EnableJpaRepositories(Array("com.hashmap.haf.repository", "com.hashmap.haf.workflow.dao"))
@EntityScan(Array("com.hashmap.haf.entities", "com.hashmap.haf.workflow.entity"))
class WorkflowExecutorApplication{

  @Value("${functions.ignite.config}")
  var igniteConfigPath: String = _

  @Bean(destroyMethod = "close")
  def ignite(): Ignite = {
    val igConfig = Thread.currentThread().getContextClassLoader.getResource(igniteConfigPath)
    val configuration = IgnitionEx.loadConfiguration(igConfig).get1()
    configuration.setClientMode(true)
    Ignition.start(configuration)
  }
}

object WorkflowExecutorApplication extends App {
  private val SPRING_CONFIG_NAME_KEY = "--spring.config.name"
  private val DEFAULT_SPRING_CONFIG_PARAM = SPRING_CONFIG_NAME_KEY + "=" + "workflow-executor"

  SpringApplication.run(classOf[WorkflowExecutorApplication], updateArguments(args): _*)

  private def updateArguments(args: Array[String]): List[String] ={
    val argsAsList = args.toList
    argsAsList.find(_.startsWith(SPRING_CONFIG_NAME_KEY)) match {
      case Some(_) => argsAsList
      case _ => DEFAULT_SPRING_CONFIG_PARAM :: argsAsList
    }
  }
}