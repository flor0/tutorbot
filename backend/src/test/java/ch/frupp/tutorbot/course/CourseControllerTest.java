package ch.frupp.tutorbot.course;

import ch.frupp.tutorbot.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CourseControllerTest {

    private MockMvc mockMvc;
    private CourseService courseService;

    @BeforeEach
    void setup() {
        courseService = Mockito.mock(CourseService.class);
        CourseController controller = new CourseController(courseService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void findAllByUserId_returnsCourses() throws Exception {
        User principal = new User();
        principal.setId(123);

        Course c1 = new Course(123, "Course A");
        c1.setId("1");
        Course c2 = new Course(123, "Course B");
        c2.setId("2");

        Mockito.when(courseService.findByUserId(123)).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/courses").principal(new UsernamePasswordAuthenticationToken(principal, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Course A"))
                .andExpect(jsonPath("$[1].name").value("Course B"));
    }

    @Test
    void createCourse_savesAndReturns() throws Exception {
        User principal = new User();
        principal.setId(123);

        Course returned = new Course(123, "New Course");
        returned.setId("99");

        Mockito.when(courseService.save(Mockito.any(Course.class))).thenReturn(returned);

        String json = "{\"name\":\"New Course\"}";

        mockMvc.perform(post("/api/courses/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .principal(new UsernamePasswordAuthenticationToken(principal, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Course"))
                .andExpect(jsonPath("$.id").value("99"));
    }
}
