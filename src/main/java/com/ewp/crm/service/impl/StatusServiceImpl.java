package com.ewp.crm.service.impl;

import com.ewp.crm.exceptions.status.StatusExistsException;
import com.ewp.crm.models.SortedStatuses;
import com.ewp.crm.models.SortedStatuses.SortingType;
import com.ewp.crm.models.Status;
import com.ewp.crm.models.User;
import com.ewp.crm.repository.interfaces.SortedStatusesRepository;
import com.ewp.crm.repository.interfaces.StatusDAO;
import com.ewp.crm.service.interfaces.ClientService;
import com.ewp.crm.service.interfaces.ProjectPropertiesService;
import com.ewp.crm.service.interfaces.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class StatusServiceImpl implements StatusService {
	private final StatusDAO statusDAO;
	private ClientService clientService;
	private final ProjectPropertiesService propertiesService;
	private final SortedStatusesRepository sortedStatusesRepository;


	private static Logger logger = LoggerFactory.getLogger(StatusServiceImpl.class);

	@Autowired
	public StatusServiceImpl(StatusDAO statusDAO, ProjectPropertiesService propertiesService, SortedStatusesRepository sortedStatusesRepository) {
		this.statusDAO = statusDAO;
		this.propertiesService = propertiesService;
		this.sortedStatusesRepository = sortedStatusesRepository;
	}

	@Autowired
	private void setStatusService(ClientService clientService) {
		this.clientService = clientService;
	}

	//Для юзера из сессии смотрим для какого статуса какая нужна сортировка (и нужна ли)
	@Override
	public List<Status> getStatusesWithSortedClients(@AuthenticationPrincipal User userFromSession) {
		List<Status> statuses = getAll();
		SortedStatuses sorted;
		for (Status status : statuses) {
			sorted = new SortedStatuses(status, userFromSession);
			if (status.getSortedStatuses().size() != 0 && status.getSortedStatuses().contains(sorted)) {
				SortedStatuses finalSorted = sorted;
				SortingType sortingType = status.getSortedStatuses().stream().filter(data -> Objects.equals(data, finalSorted)).findFirst().get().getSortingType();
				status.setClients(clientService.getOrderedClientsInStatus(status, sortingType, userFromSession));
			}
		}
		return statuses;
	}

	@Override
	public List<Status> getAll() {
		return statusDAO.getAllByOrderByIdAsc();
	}

	@Override
	public Optional<Status> get(Long id) {
		return statusDAO.findById(id);
	}

	@Override
	public Optional<Status> get(String name) {
		return Optional.ofNullable(statusDAO.getStatusByName(name));
	}

	@Override
	public Optional<Status> getFirstStatusForClient() {
		return statusDAO.findById(propertiesService.getOrCreate().getNewClientStatus());
	}

	@Override
	public Optional<Status> getRepeatedStatusForClient() {
		return statusDAO.findById(propertiesService.getOrCreate().getRepeatedDefaultStatusId());
	}

	@Override
	public Optional<Status> getStatusByName(String name) {
		return Optional.ofNullable(statusDAO.getStatusByName(name));
	}

	@Override
	public void add(Status status) {
		logger.info("{} adding of a new status...", StatusServiceImpl.class.getName());
		checkStatusUnique(status);
		Long position = statusDAO.findMaxPosition() + 1L;
		status.setPosition(position);
		statusDAO.saveAndFlush(status);
		logger.info("{} status added successfully...", StatusServiceImpl.class.getName());
	}

	@Override
	public void addInit(Status status) {
		checkStatusUnique(status);
		logger.info("{} adding of a new status...", StatusServiceImpl.class.getName());
		statusDAO.saveAndFlush(status);
		logger.info("{} status added successfully...", StatusServiceImpl.class.getName());
	}

	@Override
	public void update(Status status) {
		logger.info("{} updating of the status...", StatusServiceImpl.class.getName());
		checkStatusUnique(status);
		statusDAO.saveAndFlush(status);
		logger.info("{} status updated successfully...", StatusServiceImpl.class.getName());
	}

	private void checkStatusUnique(Status status) {
		Status statusFromDB = statusDAO.getStatusByName(status.getName());
		if (statusFromDB != null && !statusFromDB.equals(status)) {
			throw new StatusExistsException("Статус с таким названием уже существует");
		}
	}

	private void checkStatusId(Long id) {
		if (id == 1L) {
			throw new StatusExistsException("Статус с индексом \"1\" нельзя удалить");
		}
		Optional<Status> optional = statusDAO.findById(id);
		Status statusFromDB = null;
		if (optional.isPresent()) {
			statusFromDB = optional.get();
		}
		if (statusFromDB.getName().equals("deleted")) {
			throw new StatusExistsException("Статус deleted нельзя удалить");
		}
	}

	private void transferStatusClientsBeforeDelete(Status status) {
		if (status.getClients() != null) {
			Status defaultStatus = statusDAO.getStatusByName("deleted");
			defaultStatus.getClients().addAll(status.getClients());
			status.getClients().clear();
			statusDAO.saveAndFlush(status);
		}
	}

	@Override
	public void delete(Status status) {
		logger.info("{} deleting of the status...", StatusServiceImpl.class.getName());
		delete(status.getId());
		logger.info("{} status deleted successfully...", StatusServiceImpl.class.getName());
	}

	@Override
	public void delete(Long id) {
		logger.info("{} deleting of the status...", StatusServiceImpl.class.getName());
		checkStatusId(id);
		Optional<Status> optional = statusDAO.findById(id);
		Status statusFromDB = null;
		if (optional.isPresent()) {
			statusFromDB = optional.get();
		}
		transferStatusClientsBeforeDelete(statusFromDB);
		statusDAO.delete(statusFromDB);
		logger.info("{} status deleted successfully...", StatusServiceImpl.class.getName());
	}

	@Override
	public Optional<Long> findMaxPosition() {
		return Optional.ofNullable(statusDAO.findMaxPosition());
	}

	@Override
	public List<Status> getAllStatusesForStudents() {
		return statusDAO.getAllStatusesForStudents();
	}

	@Override
	public void setNewOrderForChosenStatusForCurrentUser(SortingType newOrder, Long statusId, User currentUser) {
		if (get(statusId).isPresent()) {
			SortedStatuses sortedStatus = new SortedStatuses(get(statusId).get(), currentUser);
			sortedStatus.setSortingType(newOrder);
			sortedStatusesRepository.save(sortedStatus);
		}
	}
}
