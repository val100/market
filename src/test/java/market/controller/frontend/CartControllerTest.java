package market.controller.frontend;

import market.MarketProperties;
import market.domain.*;
import market.dto.CartDTO;
import market.dto.assembler.CartDtoAssembler;
import market.interceptors.SessionCartInterceptor;
import market.service.CartService;
import market.service.ProductService;
import market.util.FixturesFactory;
import market.util.TestUtil;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import sun.security.acl.PrincipalImpl;

import java.security.Principal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CartController.class)
public class CartControllerTest {
	private final MarketProperties marketProperties = new MarketProperties(400);
	private final CartDtoAssembler cartDtoAssembler = new CartDtoAssembler();

	@MockBean
	private CartService cartService;
	@MockBean
	private ProductService productService;

	@Captor
	private ArgumentCaptor<Boolean> booleanCaptor;

	private MockMvc mockMvc;
	private Principal principal;

	private Cart cart;
	private CartDTO emptyCart;
	private UserAccount account;
	private Product product;

	@BeforeEach
	public void beforeEach() {
		CartController controller = new CartController(cartService, productService, marketProperties);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
			.addInterceptors(new SessionCartInterceptor())
			.setViewResolvers(new InternalResourceViewResolver("/WEB-INF/view/", ".jsp"))
			.build();

		account = FixturesFactory.account().setEmail("user").build();
		cart = new Cart(account);
		emptyCart = cartDtoAssembler.toModel(cart);
		Region region = FixturesFactory.region().build();
		Distillery distillery = FixturesFactory.distillery(region).build();
		product = FixturesFactory.product(distillery).build();

		principal = new PrincipalImpl(account.getEmail());
	}

	@Test
	public void getUserCart() throws Exception {
		given(cartService.getCartOrCreate(account.getEmail()))
			.willReturn(cart);

		mockMvc.perform(
			get("/cart")
				.principal(principal))
			.andExpect(status().isOk())
			.andExpect(view().name("cart"))
			.andExpect(model().attributeExists("cart"))
			.andExpect(model().attributeExists("productsById"))
			.andExpect(model().attribute("deliveryCost", is(marketProperties.getDeliveryCost())));
	}

	@Test
	public void clearUserCart() throws Exception {
		given(cartService.clearCart(account.getEmail()))
			.willReturn(cart);

		mockMvc.perform(
			post("/cart/clear")
				.principal(principal))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/cart"))
			.andExpect(model().attribute("cart", equalTo(emptyCart)));
	}

	@Test
	public void updateUserCartByForm_Ok() throws Exception {
		int quantity = 2;
		cart.update(product, quantity);
		given(cartService.addToCart(account.getEmail(), product.getId(), quantity))
			.willReturn(cart);

		mockMvc.perform(
			put("/cart")
				.param("productId", Long.toString(product.getId()))
				.param("quantity", Integer.toString(quantity))
				.principal(principal))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/cart"))
			.andExpect(model().hasNoErrors())
			.andExpect(model().attribute("cart", equalTo(cartDtoAssembler.toModel(cart))));
	}

	@Test
	public void updateUserCartByForm_UnknownProduct() throws Exception {
		int quantity = 2;
		given(cartService.addToCart(eq(account.getEmail()), anyLong(), eq(quantity)))
			.willReturn(cart);

		mockMvc.perform(
			put("/cart")
				.principal(principal)
				.param("productId", Long.toString(Long.MAX_VALUE))
				.param("quantity", Integer.toString(quantity)))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/cart"))
			.andExpect(model().hasNoErrors())
			.andExpect(model().attribute("cart", equalTo(emptyCart)));
	}

	@Ignore // todo: till exceptions handling remastered
	@Test
	public void updateUserCartByAjax() throws Exception {
		int quantity = 2;
		CartItem cartItem = cart.update(product, quantity);
		given(cartService.addToCart(account.getEmail(), product.getId(), quantity))
			.willReturn(cart);

		mockMvc.perform(
			put("/cart")
				.principal(principal)
				.contentType(TestUtil.APPLICATION_JSON_UTF8)
				.accept(TestUtil.APPLICATION_JSON_UTF8)
				.content(TestUtil.convertObjectToJsonBytes(cartDtoAssembler.toCartItemDto(cartItem))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.cartItems", hasSize(1)))
			.andExpect(jsonPath("$.cartItems[0].productId", equalTo((int)product.getId()))) // todo: shall work with long value
			.andExpect(jsonPath("$.cartItems[0].quantity", equalTo(quantity)));
	}

	@Test
	public void setUserDelivery() throws Exception {
		cart.setDeliveryIncluded(false);
		given(cartService.setDelivery(eq(account.getEmail()), booleanCaptor.capture()))
			.willReturn(cart);

		mockMvc.perform(
			put("/cart/delivery/false")
				.principal(principal))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.deliveryIncluded", is(false)));
		assertThat(booleanCaptor.getValue(), is(false));

		cart.setDeliveryIncluded(true);

		mockMvc.perform(
			put("/cart/delivery/true")
				.principal(principal))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.deliveryIncluded", is(true)));
		assertThat(booleanCaptor.getValue(), is(true));
	}
}
