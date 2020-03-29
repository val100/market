package market.controller.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import javax.servlet.Filter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = BackendController.class)
public class BackendControllerTest {

	private MockMvc mockMvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@BeforeEach
	public void beforeEach() {
		BackendController controller = new BackendController();
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.addFilters(springSecurityFilterChain)
			.setViewResolvers(new InternalResourceViewResolver("/WEB-INF/view/", ".jsp"))
			.build();
	}

	@Test
	public void index() throws Exception {
		mockMvc.perform(get("/admin"))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/index"));

		mockMvc.perform(get("/admin/"))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/index"));

		mockMvc.perform(get("/admin/index"))
			.andExpect(status().isOk())
			.andExpect(view().name("admin/index"));
	}
}
