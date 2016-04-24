package com.chengsoft;

import com.chengsoft.model.Apartment;
import com.chengsoft.model.Community;
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
import rx.Observable;
import rx.util.async.Async;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ApartmentAlertsApplication.class)
@WebAppConfiguration
@Ignore
public class ApartmentAlertsApplicationTests {

    final static Logger logger = LoggerFactory.getLogger(ApartmentAlertsApplicationTests.class);

    @Autowired
    private ApartmentSearchService apartmentSearchService;

    @Test
    public void testObservable() throws InterruptedException {
//		CountDownLatch latch = new CountDownLatch(2);

        List<Apartment> apartments = Observable.from(Community.values())
                .flatMap(c -> Async.start(() -> apartmentSearchService.lookForNewApartments(LocalDate.of(2016, 6, 1), c)))
                .toList()
                .toBlocking()
                .first()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        System.out.println(apartments.size());

        apartments = Observable.from(Community.values())
                .flatMap(c -> Async.start(() -> apartmentSearchService.lookForNewApartments(LocalDate.of(2016, 5, 1), c)))
                .toList()
                .toBlocking()
                .first()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        System.out.println(apartments.size());

        apartments = Observable.from(Community.values())
                .flatMap(c -> Async.start(() -> apartmentSearchService.lookForNewApartments(LocalDate.of(2016, 5, 1), c)))
                .toList()
                .toBlocking()
                .first()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        System.out.println(apartments.size());
//		.subscribe(apartments -> apartments.forEach(a -> logger.info("{} - {}", a.getCommunity(), a)));

//		latch.await();
    }

    @Test
    public void contextLoads() {
        Set<Apartment> apartmentsA = apartmentSearchService.lookForNewApartments(LocalDate.of(2016, 6, 1), Community.SOMERVILLE);
        Set<Apartment> apartmentsB = apartmentSearchService.lookForNewApartments(LocalDate.of(2016, 8, 1), Community.SOMERVILLE);

        logger.info(Sets.difference(apartmentsA, apartmentsB).toString());
    }

}
