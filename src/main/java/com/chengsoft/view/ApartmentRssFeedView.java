package com.chengsoft.view;

import com.chengsoft.controller.ApartmentController;
import com.chengsoft.model.Apartment;
import com.chengsoft.model.Community;
import com.chengsoft.service.ApartmentSearchService;
import com.rometools.rome.feed.rss.*;
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

import static java.util.Comparator.comparing;

/**
 * Created by tcheng on 4/24/16.
 */
public class ApartmentRssFeedView extends AbstractRssFeedView {

    private static final String CHANNEL_TITLE = "Avalon One Bedroom Apartments";
    private static final String CHANNEL_DESCRIPTION = "Feed of One Bedroom Apartments";
    public static final String MOVE_IN_DATE = "moveInDate";

    @Autowired
    private ApartmentSearchService apartmentSearchService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Value("${application.base-url}")
    private String baseUrl;

    @Override
    protected Channel newFeed() {
        Channel channel = new Channel("rss_2.0");

        String moveInDate = httpServletRequest.getParameter(MOVE_IN_DATE);
        String channelLink = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(ApartmentController.APARTMENT_FEED)
                .queryParam(MOVE_IN_DATE, moveInDate)
                .build().toUriString();

        channel.setLink(channelLink);
        channel.setTitle(CHANNEL_TITLE + " " + moveInDate);
        channel.setDescription(CHANNEL_DESCRIPTION + " with move in date ~" + moveInDate);
        apartmentSearchService.getOneMostRecent().ifPresent(a ->
                channel.setPubDate(Date.from(a.getDateFound().atStartOfDay(ZoneId.of("America/New_York")).toInstant())));
        return channel;
    }

    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model,
                                        HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse) throws Exception {
        LocalDate moveInDate = (LocalDate) model.get(MOVE_IN_DATE);

        return Observable.from(Community.values())
                .flatMap(c -> Async.start(() -> apartmentSearchService.lookForNewApartments(moveInDate, c)))
                .toList()
                .toBlocking()
                .first()
                .stream()
                .flatMap(Collection::stream)
                .sorted(comparing(Apartment::getCommunity)
                        .thenComparing(a -> a.getPricing().getAvailableDate()))
                .map(this::createItem)
                .collect(Collectors.toList());
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
