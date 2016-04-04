package kamon.spring.instrumentation.config

import org.springframework.context.annotation.{ ComponentScan, Configuration }
import org.springframework.web.servlet.config.annotation.{ EnableWebMvc, WebMvcConfigurerAdapter }

@Configuration
@ComponentScan(basePackages = Array("kamon.spring.instrumentation"))
class SpringRestApplication
object SpringRestApplication

@Configuration
@EnableWebMvc
class WebMvcConfig extends WebMvcConfigurerAdapter
