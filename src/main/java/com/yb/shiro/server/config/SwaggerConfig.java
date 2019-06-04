package com.yb.shiro.server.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * @author biaoyang
 * Date: 2019/4/25 0025
 * Description:
 */
@Profile({"dev", "test"})
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {

    @Autowired
    private ApplicationPermitConfig permitConfig;
    @Value("${spring.application.name}")
    private String title;

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(title)
                .description(title)
                .contact(new Contact("BiaoYang", "https://swagger.io/", "BiaoYang@163.com"))
                .version("1.0.0")
                .build();
    }

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(new ArrayList<ApiKey>() {{
                    add(new ApiKey("TOKEN用户认证", "Authorization", "header"));
                }})
                .securityContexts(new ArrayList<SecurityContext>() {{
                    add(securityContext());
                }})
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build()
                .directModelSubstitute(LocalDateTime.class, String.class)
                .directModelSubstitute(Timestamp.class, Long.class);
    }


    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(path -> !StringUtils.equalsAny(path, permitConfig.getPermitUrls()))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        return new ArrayList<SecurityReference>() {{
            // reference 要和 ApiKey 名字一致
            add(new SecurityReference("TOKEN用户认证", authorizationScopes));
        }};
    }


    @Bean
    SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .clientId("test-app-client-id")
                .clientSecret("test-app-client-secret")
                .realm("test-app-realm")
                .appName("test-app")
                .scopeSeparator(",")
                .additionalQueryStringParams(null)
                .useBasicAuthenticationWithAccessCodeGrant(false)
                .build();
    }

    @Bean
    public UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                // 底部 models 默认展开深度 0不展开, 默认展开太长了
                .defaultModelsExpandDepth(0)
                // 接口文档中实体的默认展开深度
                .defaultModelExpandDepth(1)
                .defaultModelRendering(ModelRendering.EXAMPLE)
                // 显示请求的响应时间
                .displayRequestDuration(true)
                .docExpansion(DocExpansion.NONE)
                // tag过滤
                .filter(false)
                .maxDisplayedTags(null)
                .operationsSorter(OperationsSorter.ALPHA)
                .showExtensions(false)
                .tagsSorter(TagsSorter.ALPHA)
                .validatorUrl(null)
                .build();
    }


}
