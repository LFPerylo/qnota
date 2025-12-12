package dev.com.qnota;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig {

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		
		// Permitir requisições do frontend usando padrões (necessário quando allowCredentials é true)
		config.addAllowedOriginPattern("http://localhost:*");
		config.addAllowedOriginPattern("http://127.0.0.1:*");
		
		// Permitir todos os métodos HTTP
		config.addAllowedMethod("*");
		
		// Permitir todos os headers
		config.addAllowedHeader("*");
		
		// Permitir credenciais (cookies, etc)
		config.setAllowCredentials(true);
		
		// Expor headers customizados se necessário
		config.addExposedHeader("*");
		
		// Aplicar a configuração a todos os endpoints
		source.registerCorsConfiguration("/**", config);
		
		return new CorsFilter(source);
	}
}

