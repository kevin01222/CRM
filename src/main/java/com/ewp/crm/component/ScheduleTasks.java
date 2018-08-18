package com.ewp.crm.component;

import com.ewp.crm.exceptions.member.NotFoundMemberList;
import com.ewp.crm.service.impl.ReportService;
import com.ewp.crm.service.email.MailSendService;
import com.ewp.crm.service.impl.FacebookServiceImpl;
import com.ewp.crm.service.impl.VKService;
import com.ewp.crm.service.interfaces.SMSService;
import com.ewp.crm.exceptions.parse.ParseClientException;
import com.ewp.crm.exceptions.util.FBAccessTokenException;
import com.ewp.crm.exceptions.util.VKAccessTokenException;
import com.ewp.crm.models.*;
import com.ewp.crm.service.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@EnableScheduling
public class ScheduleTasks {

	private final VKService vkService;

	private final ClientService clientService;

	private final StatusService statusService;

	private final SocialNetworkService socialNetworkService;

	private final SocialNetworkTypeService socialNetworkTypeService;

	private final SMSService smsService;

	private final SMSInfoService smsInfoService;

	private final SendNotificationService sendNotificationService;

	private final ClientHistoryService clientHistoryService;

	private FacebookServiceImpl facebookService;

	private final VkTrackedClubService vkTrackedClubService;

	private final VkMemberService vkMemberService;

	private final YoutubeService youtubeService;

	private final YoutubeClientService youtubeClientService;

	private final MailSendService mailSendService;

	private final ReportService reportService;

	private static Logger logger = LoggerFactory.getLogger(ScheduleTasks.class);

	@Autowired
	public ScheduleTasks(VKService vkService, ClientService clientService, StatusService statusService, SocialNetworkService socialNetworkService, SocialNetworkTypeService socialNetworkTypeService, SMSService smsService, SMSInfoService smsInfoService, SendNotificationService sendNotificationService, ClientHistoryService clientHistoryService, VkTrackedClubService vkTrackedClubService, VkMemberService vkMemberService, FacebookServiceImpl facebookService, YoutubeService youtubeService, YoutubeClientService youtubeClientService, ReportService reportService, MailSendService mailSendService) {
		this.vkService = vkService;
		this.clientService = clientService;
		this.statusService = statusService;
		this.socialNetworkService = socialNetworkService;
		this.socialNetworkTypeService = socialNetworkTypeService;
		this.smsService = smsService;
		this.smsInfoService = smsInfoService;
		this.sendNotificationService = sendNotificationService;
		this.clientHistoryService = clientHistoryService;
		this.facebookService = facebookService;
		this.vkTrackedClubService = vkTrackedClubService;
		this.vkMemberService = vkMemberService;
		this.youtubeService = youtubeService;
		this.youtubeClientService = youtubeClientService;
		this.reportService = reportService;
		this.mailSendService = mailSendService;
	}

	private void addClient(Client newClient) {
		Status newClientsStatus = statusService.getFirstStatusForClient();
		newClient.setStatus(newClientsStatus);
		newClient.setState(Client.State.NEW);
		newClient.getSocialNetworks().get(0).setSocialNetworkType(socialNetworkTypeService.getByTypeName("vk"));
		newClient.addHistory(clientHistoryService.createHistory("vk"));
		clientService.addClient(newClient);
		logger.info("New client with id{} has added from VK", newClient.getId());
	}

	private void updateClient(Client newClient) {
		SocialNetwork socialNetwork = newClient.getSocialNetworks().get(0);
		Client updateClient = socialNetworkService.getSocialNetworkByLink(socialNetwork.getLink()).getClient();
		updateClient.setPhoneNumber(newClient.getPhoneNumber());
		updateClient.setEmail(newClient.getEmail());
		updateClient.setAge(newClient.getAge());
		updateClient.setSex(newClient.getSex());
		clientService.updateClient(updateClient);
		logger.info("Client with id{} has updated from VK", updateClient.getId());
	}

	@Scheduled(fixedRate = 6_000)
	private void handleRequestsFromVk() {
		try {
			Optional<List<String>> newMassages = vkService.getNewMassages();
			if (newMassages.isPresent()) {
				for (String message : newMassages.get()) {
					try {
						Client newClient = vkService.parseClientFromMessage(message);
						SocialNetwork socialNetwork = newClient.getSocialNetworks().get(0);
						if (Optional.ofNullable(socialNetworkService.getSocialNetworkByLink(socialNetwork.getLink())).isPresent()) {
							updateClient(newClient);
						} else {
							addClient(newClient);
						}
					} catch (ParseClientException e) {
						logger.error(e.getMessage());
					}
				}
			}
		} catch (VKAccessTokenException ex) {
			logger.error(ex.getMessage());
		}
	}

	@Scheduled(fixedRate = 60_000)
	private void findNewMembersAndSendFirstMessage() {
		List<VkTrackedClub> vkTrackedClubList = vkTrackedClubService.getAll();
		List<VkMember> lastMemberList = vkMemberService.getAll();
		for (VkTrackedClub vkTrackedClub: vkTrackedClubList) {
			ArrayList<VkMember> freshMemberList = vkService.getAllVKMembers(vkTrackedClub.getGroupId(), 0L)
														   .orElseThrow(NotFoundMemberList::new);
			int countNewMembers = 0;
			for (VkMember vkMember : freshMemberList) {
				if(!lastMemberList.contains(vkMember)){
					vkService.sendMessageById(vkMember.getVkId(), vkService.getFirstContactMessage());
					vkMemberService.add(vkMember);
					countNewMembers++;
				}
			}
			if (countNewMembers > 0) {
				logger.info("{} new VK members has signed in {} club", countNewMembers, vkTrackedClub.getGroupName());
			}
		}
	}

	@Scheduled(fixedRate = 6_000)
	private void handleRequestsFromVkCommunityMessages() {
		Optional<List<Long>> newUsers = vkService.getUsersIdFromCommunityMessages();
		if (newUsers.isPresent()) {
			for (Long id : newUsers.get()) {
				Optional<Client> newClient = vkService.getClientFromVkId(id);
				if (newClient.isPresent()) {
					SocialNetwork socialNetwork = newClient.get().getSocialNetworks().get(0);
					if (!(Optional.ofNullable(socialNetworkService.getSocialNetworkByLink(socialNetwork.getLink())).isPresent())) {
						addClient(newClient.get());
					}
				}
			}
		}
	}

	@Scheduled(fixedRate = 6_000)
	private void checkClientActivationDate() {
		for (Client client : clientService.getChangeActiveClients()) {
			client.setPostponeDate(null);
			sendNotificationService.sendNotificationType(client.getClientDescriptionComment(),client, client.getOwnerUser(), Notification.Type.POSTPONE);
			clientService.updateClient(client);
		}
	}


	@Scheduled(fixedRate = 600_000)
	private void addFacebookMessageToDatabase() {
		try {
			facebookService.getFacebookMessages();
		} catch (FBAccessTokenException e) {
			logger.error("Facebook access token has not got", e);
		}
	}


	@Scheduled(fixedRate = 600_000)
	private void checkSMSMessages() {
		logger.info("start checking sms statuses");
		List<SMSInfo> queueSMS = smsInfoService.getBySMSIsChecked(false);
		for (SMSInfo sms : queueSMS) {
			String status = smsService.getStatusMessage(sms.getSmsId());
			if (!status.equals("queued")) {
				if (status.equals("delivered")) {
					sms.setDeliveryStatus("доставлено");
				} else {
					String deliveryStatus = determineStatusOfResponse(status);
					sendNotificationService.sendNotificationType(deliveryStatus, sms.getClient(), sms.getUser(), Notification.Type.SMS);
					sms.setDeliveryStatus(deliveryStatus);
				}
				sms.setChecked(true);
				smsInfoService.update(sms);
			}
		}
	}

	@Scheduled(cron = "0 0 10 01 * ?")
	private void buildAndSendReport() {
		mailSendService.sendNotificationMessageYourself(reportService.buildReportOfLastMonth());
	}

	private String determineStatusOfResponse(String status) {
		String info;
		switch (status) {
			case "delivery error":
				info = "Номер заблокирован или вне зоны";
				break;
			case "invalid mobile phone" :
				info = "Неправильный формат номера";
				break;
			case "incorrect id":
				info = "Неверный id сообщения";
				break;
			default:
				info = "Неизвестная ошибка";
		}
		return info;
	}

	@Scheduled(fixedRate = 60_000)
	private void handleYoutubeLiveStreams() {
		if (!youtubeService.checkLiveStreamStatus()) {
			youtubeService.handleYoutubeLiveChatMessages();
		} else {
			Optional<List<YoutubeClient>> youtubeClient = Optional.of(youtubeClientService.findAll());
			if (youtubeClient.isPresent()) {
				for (YoutubeClient client : youtubeClient.get()) {
					Optional<Client> newClient = vkService.getClientFromYoutubeLiveStreamByName(client.getFullName());
					if (newClient.isPresent()) {
						SocialNetwork socialNetwork = newClient.get().getSocialNetworks().get(0);
						if (!(Optional.ofNullable(socialNetworkService.getSocialNetworkByLink(socialNetwork.getLink())).isPresent())) {
							addClient(newClient.get());
						}
					}
				}
			}
		}
	}
}