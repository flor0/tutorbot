package ch.frupp.tutorbot.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setup() {
        userService = Mockito.mock(UserService.class);
        UserController controller = new UserController();
        // inject mocked service via reflection because controller uses field injection
        try {
            java.lang.reflect.Field f = UserController.class.getDeclaredField("userService");
            f.setAccessible(true);
            f.set(controller, userService);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testEndpoint_returnsPlaceholder() throws Exception {
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("Placeholder test"));
    }

    @Test
    void register_callsService_andReturnsUser() throws Exception {
        User user = new User();
        user.setId(7);
        user.setUsername("bob");

        Mockito.when(userService.register(Mockito.any())).thenReturn(user);

        String json = "{\"username\":\"bob\",\"email\":\"b@b.com\",\"password\":\"pass\",\"role\":\"USER\"}";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\":7,\"username\":\"bob\"}"));
    }
}
