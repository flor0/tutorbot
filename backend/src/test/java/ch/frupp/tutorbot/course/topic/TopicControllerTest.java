package ch.frupp.tutorbot.course.topic;

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

        Topic t1 = new Topic();
        t1.setId("t1");
        t1.setUserId(123);
        t1.setCourseId("c1");
        t1.setName("Integrals");
        t1.setSummary("Integration basics");

        Topic t2 = new Topic();
        t2.setId("t2");
        t2.setUserId(123);
        t2.setCourseId("c1");
        t2.setName("Derivatives");
        t2.setSummary("Derivative basics");

        Mockito.when(topicService.listTopicsForUserAndCourse(principal, "c1")).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/api/courses/c1/topics").principal(new UsernamePasswordAuthenticationToken(principal, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Integrals"))
                .andExpect(jsonPath("$[1].name").value("Derivatives"));
    }

    @Test
    void createTopic_returnsCreatedTopic() throws Exception {
        User principal = new User();
        principal.setId(123);

        Topic returned = new Topic();
        returned.setId("newId");
        returned.setUserId(123);
        returned.setCourseId("c1");
        returned.setName("Limits");
        returned.setSummary("Limits summary");

        Mockito.when(topicService.createTopicForUser(Mockito.eq(principal), Mockito.any(Topic.class), Mockito.eq("c1")))
                .thenReturn(returned);

        String json = "{\"name\":\"Limits\", \"summary\":\"Limits summary\"}";

        mockMvc.perform(post("/api/courses/c1/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .principal(new UsernamePasswordAuthenticationToken(principal, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("newId"))
                .andExpect(jsonPath("$.name").value("Limits"));
    }

    @Test
    void deleteTopic_returnsNoContent() throws Exception {
        User principal = new User();
        principal.setId(123);

        Mockito.doNothing().when(topicService).deleteTopicForUser(principal, "t1");

        mockMvc.perform(delete("/api/courses/c1/topics/t1").principal(new UsernamePasswordAuthenticationToken(principal, null)))
                .andExpect(status().isNoContent());
    }
}
