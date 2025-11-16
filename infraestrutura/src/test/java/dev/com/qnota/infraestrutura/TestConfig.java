package dev.com.qnota.infraestrutura;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuração mínima do Spring Boot apenas para testar a infraestrutura.
 * Não depende do apresentacao-backend para evitar dependência circular.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
	"dev.com.qnota.infraestrutura",
	"dev.com.qnota.dominio",
	"dev.com.qnota.aplicacao"
})
@EntityScan(basePackages = "dev.com.qnota.infraestrutura.persistencia.jpa")
@EnableJpaRepositories(basePackages = "dev.com.qnota.infraestrutura.persistencia.jpa")
public class TestConfig {
}




