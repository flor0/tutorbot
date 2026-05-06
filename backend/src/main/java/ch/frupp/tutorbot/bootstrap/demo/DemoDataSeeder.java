package ch.frupp.tutorbot.bootstrap.demo;

import ch.frupp.tutorbot.course.Course;
import ch.frupp.tutorbot.course.CourseRepository;
import ch.frupp.tutorbot.course.topic.Topic;
import ch.frupp.tutorbot.course.topic.TopicRepository;
import ch.frupp.tutorbot.user.User;
import ch.frupp.tutorbot.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Profile("demo-seed")
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {


	private static final String DEMO_USERNAME = "demo";
	private static final String DEMO_PASSWORD = "demo";
	private static final String DEMO_ROLE = "DEMO";
	private static final String COURSE_NAME = "Mathematics";
	private static final String TOPIC_NAME = "Analysis";
	private static final String TOPIC_SUMMARY = "Foundational analysis concepts for the demo account.";

	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final TopicRepository topicRepository;
	private final PasswordEncoder passwordEncoder;

	public DemoDataSeeder(UserRepository userRepository,
						  CourseRepository courseRepository,
						  TopicRepository topicRepository,
						  PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.courseRepository = courseRepository;
		this.topicRepository = topicRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) {

		// Assumption: If demo user exists, the other related data also exists,
		// on the other hand if no demo User exists, none of the other related data exists.

		// Skip if demo user exists
		// Always finds the demo User because its ROLE is always "DEMO".
		Optional<User> userOptional = userRepository.findByRole(DEMO_ROLE);
		if (userOptional.isPresent()) {
			return;
		}

		log.info("No existing DEMO User found. Seeding the database with the demo data...");

		// Create demo User
		User demoUser = userRepository.findByUsername(DEMO_USERNAME)
				.orElseGet(() -> {
					User user = User.builder()
							.username(DEMO_USERNAME)
							.password(passwordEncoder.encode(DEMO_PASSWORD))
							.role(DEMO_ROLE)
							.enabled(true)
							.build();
					return userRepository.save(user);
				});


		// Create demo Course
		Course demoCourse = Course.builder()
				.name(COURSE_NAME)
				.user(demoUser)
				.build();
		courseRepository.save(demoCourse);

		// Create demo Topic
		Topic demoTopic = Topic.builder()
				.name(TOPIC_NAME)
				.summary(TOPIC_SUMMARY)
				.course(demoCourse)
				.build();
		topicRepository.save(demoTopic);

	}
}
