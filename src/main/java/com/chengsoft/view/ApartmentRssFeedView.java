package com.chengsoft.view;

import com.chengsoft.controller.ApartmentController;
import com.chengsoft.model.Apartment;
import com.chengsoft.model.Community;
import com.chengsoft.service.ApartmentSearchService;
import com.rometools.rome.feed.rss.*;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;
import org.springframework.web.util.UriComponentsBuilder;
import rx.Observable;
import rx.util.async.Async;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

/**
 * Created by tcheng on 4/24/16.
 */
public class ApartmentRssFeedView extends AbstractRssFeedView {

    private static final String CHANNEL_TITLE = "Avalon One Bedroom Apartments";
    private static final String CHANNEL_DESCRIPTION = "Feed of one bedroom apartments";
    public static final String MOVE_IN_DATES = "moveInDates";

    @Autowired
    private ApartmentSearchService apartmentSearchService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Value("${application.base-url}")
    private String baseUrl;

    @Override
    protected Channel newFeed() {
        Channel channel = new Channel("rss_2.0");

        String moveInDates = httpServletRequest.getParameter(MOVE_IN_DATES);
        String months = Stream.of(moveInDates.split(","))
                .map(String::trim)
                .map(LocalDate::parse)
                .map(LocalDate::getMonth)
                .map(Object::toString)
                .map(WordUtils::capitalizeFully)
                .collect(Collectors.joining(", "));

        String channelLink = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(ApartmentController.APARTMENT_FEED)
                .queryParam(MOVE_IN_DATES, moveInDates)
                .build().toUriString();

        channel.setLink(channelLink);

        channel.setTitle(CHANNEL_TITLE + " For " + months);
        channel.setDescription(CHANNEL_DESCRIPTION + " with move in dates of around " + months);
        apartmentSearchService.getOneMostRecent().ifPresent(a ->
                channel.setPubDate(Date.from(a.getDateFound().atStartOfDay(ZoneId.of("America/New_York")).toInstant())));
        return channel;
    }

    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model,
                                        HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse) throws Exception {
        List<LocalDate> moveInDates = (List<LocalDate>) model.get(MOVE_IN_DATES);

        return Observable.from(Community.values())
                .flatMap(c -> Observable.from(moveInDates)
                        .flatMap(date -> Async.start(() -> apartmentSearchService.lookForNewApartments(date, c))))
                .toList()
                .toBlocking()
                .first()
                .stream()
                .flatMap(Collection::stream)
                .sorted(comparing(Apartment::getCommunity)
                        .thenComparing(a -> a.getPricing().getAvailableDate()))
                .distinct() // remove duplicates that span across different move-in dates
                .map(this::createItem)
                .collect(toList());
    }

    private Item createItem(Apartment apartment) {
        Item item = new Item();
        item.setTitle(String.format("%s - %s - $%d - #%d",
                apartment.getCommunity(),
                apartment.getPricing().getAvailableDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                apartment.getPricing().getEffectiveRent(),
                apartment.getApartmentNumber()));
        item.setDescription(createDescription(apartment));

        // Create guid from apartment code
        Guid guid = new Guid();
        guid.setValue(apartment.getApartmentCode());
        item.setGuid(guid);

        item.setLink(createLink(apartment));
        item.setPubDate(Date.from(apartment.getDateFound().atStartOfDay(ZoneId.of("America/New_York")).toInstant()));
        return item;
    }

    private Description createDescription(Apartment apartment) {
        Description description = new Description();
        description.setType(Content.HTML);

        String link = createLink(apartment);

        description.setValue(String.format("<a href='%s'>View Details</a>",
                link));
        return description;
    }

    private String createLink(Apartment apartment) {
        return UriComponentsBuilder.fromHttpUrl(apartment.getCommunity().getBaseUrl())
                .pathSegment("apartment")
                .pathSegment(apartment.getApartmentCode())
                .build()
                .toUriString();
    }

}
