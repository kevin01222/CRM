package com.ewp.crm.controllers.rest;

import com.ewp.crm.models.ClientHistory;
import com.ewp.crm.service.interfaces.ClientHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client/history/rest")
public class ClientHistoryRestController {

	private static final Logger logger = LoggerFactory.getLogger(ClientHistoryRestController.class);

	private final ClientHistoryService clientHistoryService;

	@Value("${project.pagination.page-size.client-history}")
	private int pageSize;

	@Autowired
	public ClientHistoryRestController(ClientHistoryService clientHistoryService) {
		this.clientHistoryService = clientHistoryService;
	}

	@GetMapping("/getHistory/{clientId}")
	public ResponseEntity getClientHistory(@PathVariable("clientId") long id, @RequestParam("page")int page) {
		logger.debug("Start executing query getClientHistory(" + id + ", " + page + ")");
		long start = System.currentTimeMillis();
		Pageable pageable = new PageRequest(page, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC,"id")));
		List<ClientHistory> clientHistory = clientHistoryService.getAllClientById(id, pageable);
		long end = System.currentTimeMillis();
		logger.debug("Query getClientHistory(" + id + ", " + page + ") takes " + (end - start) + "ms");
		if (clientHistory == null || clientHistory.isEmpty()) {
			logger.info("no more history for client id " + id);
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(clientHistory);
	}

}
