package com.chengsoft;

import com.chengsoft.model.Apartment;
import com.chengsoft.service.ApartmentSearchService;
import com.google.common.collect.Sets;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.LocalDate;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApartmentAlertsApplication.class)
@WebAppConfiguration
@Ignore
public class ApartmentAlertsApplicationTests {

	final static Logger logger = LoggerFactory.getLogger(ApartmentAlertsApplicationTests.class);

	@Autowired
	private ApartmentSearchService apartmentSearchService;

	@Test
	public void contextLoads() {
		Set<Apartment> apartmentsA = apartmentSearchService.lookForNewApartments(LocalDate.of(2016, 6, 1));
		Set<Apartment> apartmentsB = apartmentSearchService.lookForNewApartments(LocalDate.of(2016, 8, 1));

		logger.info(Sets.difference(apartmentsA, apartmentsB).toString());
	}

}
