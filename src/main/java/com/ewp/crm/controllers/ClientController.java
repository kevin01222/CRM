package com.ewp.crm.controllers;

import com.ewp.crm.models.*;
import com.ewp.crm.service.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;


@Controller
public class ClientController {

	private static Logger logger = LoggerFactory.getLogger(ClientController.class);

	private final StatusService statusService;

	private final ClientService clientService;

	private final UserService userService;

	private final MessageTemplateService MessageTemplateService;

	private final SocialNetworkTypeService socialNetworkTypeService;

	private final NotificationService notificationService;

	private final RoleService roleService;

	@Autowired
	public ClientController(StatusService statusService, ClientService clientService, UserService userService,
	                        MessageTemplateService MessageTemplateService, SocialNetworkTypeService socialNetworkTypeService, NotificationService notificationService, ClientHistoryService clientHistoryService, RoleService roleService) {
		this.statusService = statusService;
		this.clientService = clientService;
		this.userService = userService;
		this.MessageTemplateService = MessageTemplateService;
		this.socialNetworkTypeService = socialNetworkTypeService;
		this.notificationService = notificationService;
		this.roleService = roleService;
	}

	@PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
	@RequestMapping(value = "/client", method = RequestMethod.GET)
	public ModelAndView getAll() {
		User userFromSession = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<Status> statuses;
		ModelAndView modelAndView = new ModelAndView("main-client-table");
		//TODO Сделать ещё адекватней
		if (userFromSession.getRole().contains(roleService.getByRoleName("ADMIN"))) {
			statuses = statusService.getAll();
		} else {
			statuses = statusService.getStatusesWithClientsForUser(userFromSession);
		}
		modelAndView.addObject("user", userFromSession);
		modelAndView.addObject("statuses", statuses);
		modelAndView.addObject("users", userService.getAll());
		modelAndView.addObject("notifications", notificationService.getByUserToNotify(userFromSession));
		modelAndView.addObject("notifications_type_sms", notificationService.getByUserToNotifyAndType(userFromSession, Notification.Type.SMS));
		modelAndView.addObject("notifications_type_comment", notificationService.getByUserToNotifyAndType(userFromSession, Notification.Type.COMMENT));
		modelAndView.addObject("notifications_type_postpone", notificationService.getByUserToNotifyAndType(userFromSession, Notification.Type.POSTPONE));
		modelAndView.addObject("emailTmpl", MessageTemplateService.getall());
		return modelAndView;
	}

	@PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
	@RequestMapping(value = "/client/allClients", method = RequestMethod.GET)
	public ModelAndView allClientsPage() {
		User userFromSession = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ModelAndView modelAndView = new ModelAndView("all-clients-table");
		modelAndView.addObject("allClients", clientService.getAllClients());
		modelAndView.addObject("statuses", statusService.getAll());
		modelAndView.addObject("socialNetworkTypes", socialNetworkTypeService.getAll());
		modelAndView.addObject("notifications", notificationService.getByUserToNotify(userFromSession));
		return modelAndView;
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@RequestMapping(value = "/admin/client/clientInfo/{id}", method = RequestMethod.GET)
	public ModelAndView clientInfo(@PathVariable Long id) {
		User userFromSession = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ModelAndView modelAndView = new ModelAndView("client-info");
		modelAndView.addObject("client", clientService.getClientByID(id));
		modelAndView.addObject("states", Client.State.values());
		modelAndView.addObject("socialMarkers", socialNetworkTypeService.getAll());
		modelAndView.addObject("user", userFromSession);
		modelAndView.addObject("notifications", notificationService.getByUserToNotify(userFromSession));

		return modelAndView;
	}

	@PreAuthorize("hasAuthority('ADMIN')")
	@RequestMapping(value = "/admin/client/add/{statusName}", method = RequestMethod.GET)
	public ModelAndView addClient(@PathVariable String statusName) {
		User userFromSession = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		ModelAndView modelAndView = new ModelAndView("add-client");
		modelAndView.addObject("status", statusService.get(statusName));
		modelAndView.addObject("states", Client.State.values());
		modelAndView.addObject("socialMarkers", socialNetworkTypeService.getAll());
		modelAndView.addObject("user", userFromSession);
		modelAndView.addObject("notifications", notificationService.getByUserToNotify(userFromSession));
		return modelAndView;
	}

	@PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
	@RequestMapping(value = "/phone", method = RequestMethod.GET)
	public ModelAndView getPhone() {
		return new ModelAndView("webrtrc");
	}
}
