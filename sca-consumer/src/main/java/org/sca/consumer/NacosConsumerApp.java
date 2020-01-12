package org.sca.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NacosConsumerApp {

    @RestController
    public class NacosController{

        @Autowired
        private LoadBalancerClient loadBalancerClient;
        @Autowired
        private RestTemplate restTemplate;
       
        @Value("${spring.application.name}")
        private String appName;

        @GetMapping("/echo/app-name")
        public String echoAppName(){
        	//注意两种使用负载均衡的方式
            //使用 LoadBalanceClient 和 RestTemolate 结合的方式来访问
            //ServiceInstance serviceInstance = loadBalancerClient.choose("nacos-provider");
            //String url = String.format("http://%s:%s/echo/%s",serviceInstance.getHost(),serviceInstance.getPort(),appName);
            String url = String.format("http://%s/echo/%s","nacos-provider",appName);//注意不需要绑定短裤，和@LoadBalanced注解配合不会报错

            System.out.println("request url:"+url);
            return restTemplate.getForObject(url,String.class);
        }
        
        @Autowired
        Client client;
        
        @GetMapping("/echo/app-name1")
        public String echoAppName1(){
        	String result = client.echo("didi");
            return "Return : " + result;
        }
        
        
        @Autowired
        private WebClient.Builder webClientBuilder;

        @GetMapping("/echo/app-name2")
        public Mono<String> test() {
            Mono<String> result = webClientBuilder.build()
                    .get()
                    .uri("http://nacos-provider/echo/haha")
                    .retrieve()
                    .bodyToMono(String.class);
            return result;
        }

    }
    
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
    
    @FeignClient("nacos-provider")
    interface Client {
    	//多种传递参数的方式，比如实体怎么传，怎么测试
    	@GetMapping("/echo/{string}")
        String echo(@PathVariable("string") String string);
    	
//        @GetMapping("/echo")
//        String hello(@RequestParam(name = "name") String name);

    }

    //实例化 RestTemplate 实例
    @Bean
    @LoadBalanced//(如果是名称的远程要开启，不然要关闭,既url是ip地址的话会爆错) https://www.jianshu.com/p/3c98580759aa
    public RestTemplate restTemplate(){

        return new RestTemplate();
    }

    public static void main(String[] args) {

        SpringApplication.run(NacosConsumerApp.class,args);
    }
}
