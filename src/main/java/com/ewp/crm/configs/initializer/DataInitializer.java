package com.ewp.crm.configs.initializer;

import com.ewp.crm.configs.inteface.VKConfig;
import com.ewp.crm.exceptions.member.NotFoundMemberList;
import com.ewp.crm.models.*;
import com.ewp.crm.service.impl.VKService;
import com.ewp.crm.service.interfaces.*;
import com.github.javafaker.Faker;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.util.*;

public class DataInitializer {

	@Autowired
	private VkTrackedClubService vkTrackedClubService;

	@Autowired
	private VKConfig vkConfig;

	@Autowired
	private VkMemberService vkMemberService;

	@Autowired
	private VKService vkService;

	@Autowired
	private StatusService statusService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private MessageTemplateService MessageTemplateService;

	@Autowired
	private SocialNetworkTypeService socialNetworkTypeService;

	@Autowired
	private ClientHistoryService clientHistoryService;

	@Autowired
	private ReportsStatusService reportsStatusService;

	@Autowired
	private StudentService studentService;

	@Autowired
	private StudentStatusService studentStatusService;

	private void init() {

		// DEFAULT STATUS AND FIRST STATUS FOR RELEASE
		Status defaultStatus = new Status("deleted", true, 5L);
		Status status0 = new Status("New clients", false, 1L);

		Role roleAdmin = new Role("ADMIN");
		Role roleOwner = new Role("OWNER");
		Role roleUser = new Role("USER");
		roleService.add(roleAdmin);
		roleService.add(roleUser);
		roleService.add(roleOwner);

		SocialNetworkType VK = new SocialNetworkType("vk");
		SocialNetworkType FACEBOOK = new SocialNetworkType("facebook");
		SocialNetworkType UNKNOWN = new SocialNetworkType("unknown");
		socialNetworkTypeService.add(VK);
		socialNetworkTypeService.add(FACEBOOK);
		socialNetworkTypeService.add(UNKNOWN);

		User admin = new User(
				"Stanislav",
				"Sorokin",
				"88062334088",
				"admin@mail.ru",
				"admin",
				null, Client.Sex.MALE.toString(),
				"Moscow",
				"Russia",
				Arrays.asList(roleService.getRoleByName("USER"), roleService.getRoleByName("ADMIN"),
						roleService.getRoleByName("OWNER")),
				true,
				true);
		admin.setAutoAnswer("Admin: Предлагаем вам пройти обучение на нашем сайте");
		userService.add(admin);

		User user1 = new User("Ivan", "Ivanov", "79123456789", "user1@mail.ru",
				"user", null, Client.Sex.MALE.toString(), "Minsk", "Belarus", Collections.singletonList(roleService.getRoleByName("USER")), true, false);
		userService.add(user1);

		User user2 = new User("Petr", "Petrov", "89118465234", "user2@mail.ru",
				"user", null, Client.Sex.MALE.toString(), "Tver", "Russia", Collections.singletonList(roleService.getRoleByName("USER")), true, true);
		userService.add(user2);

		String templateText4 = "<!DOCTYPE html>\n" +
				"<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:th=\"http://www.thymeleaf.org\">\n" +
				"<head></head>\n" +
				"<body>\n" +
				"<p>Добрый день, %fullName%</p>\n" +
				"<p>Напоминаем, что сегодня %dateOfSkypeCall% с Вами состоится беседа по Skype.</p>\n" +
				"<p>С наилучшими пожеланиями, команда JavaMentor</p>\n" +
				"<img src=\"https://sun9-9.userapi.com/c841334/v841334855/6acfb/_syiwM0RH0I.jpg\"/>\n" +
				"</body>\n" +
				"</html>";

		String templateText3 = "<!DOCTYPE html>\n" +
				"<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:th=\"http://www.thymeleaf.org\">\n" +
				"<head></head>\n" +
				"<body>\n" +
				"<p>Добрый день, %fullName%</p>\n" +
				"<p>Мы не смогли до Вас дозвониться.</p>\n" +
				"<p>Пожалуйста, свяжитесь с нами</p>\n" +
				"<p>С наилучшими пожеланиями, команда JavaMentor</p>\n" +
				"<img src=\"https://sun9-9.userapi.com/c841334/v841334855/6acfb/_syiwM0RH0I.jpg\"/>\n" +
				"</body>\n" +
				"</html>";
		String templateText2 = "<!DOCTYPE html>\n" +
				"<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:th=\"http://www.thymeleaf.org\">\n" +
				"<head></head>\n" +
				"<body>\n" +
				"<p>Добрый день, %fullName%</p>\n" +
				"<p>Напоминаем, что вам необходимо оплатить обучение за следующий месяц.</p>\n" +
				"<p>С наилучшими пожеланиями, команда JavaMentor</p>\n" +
				"<img src=\"https://sun9-9.userapi.com/c841334/v841334855/6acfb/_syiwM0RH0I.jpg\"/>\n" +
				"</body>\n" +
				"</html>";
		String templateText1 = "<!DOCTYPE html>\n" +
				"<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:th=\"http://www.thymeleaf.org\">\n" +
				"<head></head>\n" +
				"<body>\n" +
				"<p>%bodyText%</p>\n" +
				"</body>\n" +
				"</html>";

		String otherText4 = "Добрый день, %fullName%!\n Напоминаем, что сегодня %dateOfSkypeCall% с Вами состоится беседа по Skype.\n"
				+ "С наилучшими пожеланиями, команда JavaMentor";
		String otherText3 = "Добрый день, %fullName%!\n Mы не смогли до Вас дозвониться.\n" +
				"Пожалуйста, свяжитесь с нами.\n" + "С наилучшими пожеланиями, команда JavaMentor";
		String otherText2 = "Добрый день, %fullName%!\nНапоминаем, что вам необходимо оплатить обучение за следующий месяц.\n" +
				"С наилучшими пожеланиями, команда JavaMentor";
		String otherText1 = "%bodyText%";

		MessageTemplate MessageTemplate4 = new MessageTemplate("Беседа по Skype", templateText4, otherText4);
		MessageTemplate MessageTemplate3 = new MessageTemplate("Не дозвонился", templateText3, otherText3);
		MessageTemplate MessageTemplate2 = new MessageTemplate("Оплата за обучение", templateText2, otherText2);
		MessageTemplate MessageTemplate1 = new MessageTemplate("После разговора", templateText1, otherText1);
		MessageTemplateService.add(MessageTemplate1);
		MessageTemplateService.add(MessageTemplate2);
		MessageTemplateService.add(MessageTemplate3);
		MessageTemplateService.add(MessageTemplate4);

		Status status1 = new Status("trialLearnStatus", false, 2L);
		Status status2 = new Status("inLearningStatus", false, 3L);
		Status status3 = new Status("pauseLearnStatus", false, 4L);
		Status status4 = new Status("endLearningStatus", false, 5L);
		Status status5 = new Status("dropOut Status", false, 6L);

		Client client1 = new Client("Юрий", "Долгоруков", "79999992288", "u.dolg@mail.ru", (byte) 21, Client.Sex.MALE, "Тула", "Россия", Client.State.FINISHED, new Date(Calendar.getInstance().getTimeInMillis() - 100000000));
		Client client2 = new Client("Вадим", "Бойко", "89687745632", "vboyko@mail.ru", (byte) 33, Client.Sex.MALE, "Тула", "Россия", Client.State.LEARNING, new Date(Calendar.getInstance().getTimeInMillis() - 200000000));
		Client client3 = new Client("Александра", "Соловьева", "78300029530", "a.solo@mail.ru", (byte) 53, Client.Sex.FEMALE, "Тула", "Россия", Client.State.LEARNING, new Date(Calendar.getInstance().getTimeInMillis() - 300000000));
		Client client4 = new Client("Иван", "Федоров", "78650824705", "i.fiod@mail.ru", (byte) 20, Client.Sex.MALE, "Тула", "Россия", Client.State.NEW, new Date(Calendar.getInstance().getTimeInMillis() - 400000000));
		client1.addSMSInfo(new SMSInfo(123456789L, "SMS Message to client 1", admin));
		client2.addSMSInfo(new SMSInfo(12345678L, "SMS Message to client 2", admin));
		client3.addSMSInfo(new SMSInfo(1234567L, "SMS Message to client 3", admin));
		client4.addSMSInfo(new SMSInfo(123456L, "SMS Message to client 4", admin));
		client1.addHistory(clientHistoryService.createHistory("инициализации crm"));
		client2.addHistory(clientHistoryService.createHistory("инициализации crm"));
		client3.addHistory(clientHistoryService.createHistory("инициализации crm"));
		client4.addHistory(clientHistoryService.createHistory("инициализации crm"));
		client1.setSocialNetworks(Arrays.asList(new SocialNetwork("https://vk.com/id", socialNetworkTypeService.getByTypeName("vk")),
				new SocialNetwork("https://fb.com/id", socialNetworkTypeService.getByTypeName("facebook"))));
		client2.setSocialNetworks(Arrays.asList(new SocialNetwork("https://vk.com/id", socialNetworkTypeService.getByTypeName("vk")),
				new SocialNetwork("https://fb.com/id", socialNetworkTypeService.getByTypeName("facebook"))));
		client3.setSocialNetworks(Arrays.asList(new SocialNetwork("https://vk.com/id", socialNetworkTypeService.getByTypeName("vk")),
				new SocialNetwork("https://fb.com/id", socialNetworkTypeService.getByTypeName("facebook"))));
		client4.setSocialNetworks(Arrays.asList(new SocialNetwork("https://vk.com/id", socialNetworkTypeService.getByTypeName("vk")),
				new SocialNetwork("https://fb.com/id", socialNetworkTypeService.getByTypeName("facebook"))));
		client1.setJobs(Arrays.asList(new Job("javaMentor", "developer"), new Job("Microsoft", "Junior developer")));

		vkTrackedClubService.add(new VkTrackedClub(Long.parseLong(vkConfig.getClubId()) * (-1),
				vkConfig.getCommunityToken(),
				"JavaMentorTest",
				Long.parseLong(vkConfig.getApplicationId())));
		List<VkTrackedClub> vkTrackedClubs = vkTrackedClubService.getAll();
		for (VkTrackedClub vkTrackedClub : vkTrackedClubs) {
			List<VkMember> memberList = vkService.getAllVKMembers(vkTrackedClub.getGroupId(), 0L)
					.orElseThrow(NotFoundMemberList::new);
			vkMemberService.addAllMembers(memberList);
		}
		clientService.addClient(client1);
		clientService.addClient(client2);
		clientService.addClient(client3);
		clientService.addClient(client4);
		status0.addClient(clientService.getClientByEmail("u.dolg@mail.ru"));
		status1.addClient(clientService.getClientByEmail("i.fiod@mail.ru"));
		status2.addClient(clientService.getClientByEmail("vboyko@mail.ru"));
		status3.addClient(clientService.getClientByEmail("a.solo@mail.ru"));
		statusService.addInit(status0);
		statusService.addInit(status1);
		statusService.addInit(status2);
		statusService.addInit(status3);
		statusService.addInit(status4);
		statusService.addInit(status5);
		statusService.addInit(defaultStatus);

		StudentStatus trialStatus = studentStatusService.add(new StudentStatus("Trial student"));
		StudentStatus learningStatus = studentStatusService.add(new StudentStatus("Learning student"));
		StudentStatus pauseStatus = studentStatusService.add(new StudentStatus("Paused student"));

		DateTime currentDate = new DateTime();
		Student trialStudent = new Student(clientService.getClientByEmail("i.fiod@mail.ru"), currentDate.plusDays(3).toDate(), currentDate.plusDays(3).toDate(), 1200000L, 800000L, 400000L, trialStatus, "Trial started");
		Student learningStudent = new Student(clientService.getClientByEmail("vboyko@mail.ru"), currentDate.toDate(), currentDate.plusDays(30).toDate(), 1200000L, 800000L, 400000L, learningStatus, "Learning fast");
		Student pauseStudent = new Student(clientService.getClientByEmail("a.solo@mail.ru"), currentDate.toDate(), currentDate.plusDays(14).toDate(), 1200000L, 1200000L, 0L, pauseStatus, "Gone to vacation for 14 days");
		studentService.add(trialStudent);
		studentService.add(learningStudent);
		studentService.add(pauseStudent);

		//TODO удалить после теста

		Faker faker = new Faker();
		List<Client> list = new LinkedList<>();
		for (int i = 0; i < 20; i++) {
			Client client = new Client(faker.name().firstName(), faker.name().lastName(), faker.phoneNumber().phoneNumber(), "teststatususer" + i + "@gmail.com", (byte) 20, Client.Sex.MALE, statusService.get("trialLearnStatus"));
			client.addHistory(clientHistoryService.createHistory("инициализация crm"));
			list.add(client);
		}
		clientService.addBatchClients(list);
		list.clear();

		for (int i = 0; i < 50; i++) {
			Client client = new Client(faker.name().firstName(), faker.name().lastName(), faker.phoneNumber().phoneNumber(), "testclient" + i + "@gmail.com", (byte) 20, Client.Sex.MALE, statusService.get("endLearningStatus"));
			client.addHistory(clientHistoryService.createHistory("инициализация crm"));
			list.add(client);
		}
		clientService.addBatchClients(list);
		reportsStatusService.add(new ReportsStatus(6, 5, 3, 4, 2));
	}

}
