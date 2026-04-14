package ch.frupp.tutorbot.course.topic;

import ch.frupp.tutorbot.course.Course;
import ch.frupp.tutorbot.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TopicControllerTest {

    private MockMvc mockMvc;
    private TopicService topicService;

    @BeforeEach
    void setup() {
        topicService = Mockito.mock(TopicService.class);
        TopicController controller = new TopicController(topicService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listTopics_returnsTopics() throws Exception {
        User principal = new User();
        principal.setId(123);

        Course course = new Course();
        course.setId(1);
        course.setUser(principal);

        Topic t1 = new Topic();
        t1.setId(1);
        t1.setCourse(course);
        t1.setName("Integrals");
        t1.setSummary("Integration basics");

        Topic t2 = new Topic();
        t2.setId(2);
        t2.setCourse(course);
        t2.setName("Derivatives");
        t2.setSummary("Derivative basics");

        Mockito.when(topicService.listTopicsForUserAndCourse(principal, 1)).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/courses/1/topics").principal(new UsernamePasswordAuthenticationToken(principal, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Integrals"))
                .andExpect(jsonPath("$[1].name").value("Derivatives"));
    }

    @Test
    void createTopic_returnsCreatedTopic() throws Exception {
        User principal = new User();
        principal.setId(123);

        Course course = new Course();
        course.setId(1);
        course.setUser(principal);

        Topic returned = new Topic();
        returned.setId(99);
        returned.setCourse(course);
        returned.setName("Limits");
        returned.setSummary("Limits summary");

        Mockito.when(topicService.createTopicForUser(Mockito.eq(principal), Mockito.any(TopicDto.class), Mockito.eq(1)))
                .thenReturn(returned);

        String json = "{\"name\":\"Limits\", \"summary\":\"Limits summary\"}";

        mockMvc.perform(post("/api/courses/1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .principal(new UsernamePasswordAuthenticationToken(principal, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.name").value("Limits"));
    }

    @Test
    void deleteTopic_returnsNoContent() throws Exception {
        User principal = new User();
        principal.setId(123);

        Mockito.doNothing().when(topicService).deleteTopicForUser(principal, 1);

        mockMvc.perform(delete("/api/courses/1/topics/1").principal(new UsernamePasswordAuthenticationToken(principal, null)))
                .andExpect(status().isNoContent());
    }
}
