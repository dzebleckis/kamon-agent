package kamon.spring.instrumentation.config

//import org.springframework.boot.SpringApplication
//import org.springframework.boot.autoconfigure.{ EnableAutoConfiguration, SpringBootApplication }
//import org.springframework.boot.context.embedded.{ ConfigurableEmbeddedServletContainer, EmbeddedServletContainerCustomizer }
import org.springframework.context.annotation.{ ComponentScan, Configuration }
import org.springframework.web.servlet.config.annotation.{ EnableWebMvc, WebMvcConfigurerAdapter }

// @SpringBootApplication

//@Configuration
//@ComponentScan
//@EnableAutoConfiguration
//class SpringRestApplication
//object SpringRestApplication {
//
//  //var port: Int = _
//
//  def main(args: Array[String]) {
//
//    SpringApplication.run(classOf[SpringRestApplication], args: _*)
//  }
//
//  //def start(args: Array[String] = Array.empty, port: Int = 8000): ConfigurableApplicationContext = {
//  //  this.port = port
//  //  SpringApplication.run(classOf[SpringRestApplication], args: _*)
//  //}
//
//  //@Bean
//  //def containerCustomizer(): EmbeddedServletContainerCustomizer =
//  //  new EmbeddedServletContainerCustomizer() {
//  //    override def customize(container: ConfigurableEmbeddedServletContainer): Unit = container.setPort(port)
//  //  }
//
//}

@Configuration
@ComponentScan(basePackages = Array("kamon.spring.instrumentation"))
class SpringRestApplication
object SpringRestApplication {

}

@Configuration
@EnableWebMvc
class WebMvcConfig extends WebMvcConfigurerAdapter {

  //  override def addResourceHandlers(registry: ResourceHandlerRegistry) = {
  //    registry.addResourceHandler("images/**").addResourceLocations("images/")
  //  }

}
