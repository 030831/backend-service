package com.hyunjun.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.sql.init.mode=never"
})
@ActiveProfiles("test")
class BackendServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
