package com.chengsoft.view;

import com.chengsoft.controller.ApartmentController;
import com.chengsoft.model.Apartment;
import com.chengsoft.model.Community;
import com.chengsoft.service.ApartmentSearchService;
import com.rometools.rome.feed.rss.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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
@Component
public class ApartmentRssFeedView extends AbstractRssFeedView {

    private static final String CHANNEL_TITLE = "Avalon Somerville One Bedroom Apartments";
    private static final String CHANNEL_DESCRIPTION = "Feed of One Bedroom Apartments";
    private static final String APARTMENT_DETAIL_BASE_URL = "http://www.avaloncommunities.com/massachusetts/somerville-apartments/ava-somerville/apartment/{apartmentCode}";
    public static final String MOVE_IN_DATE = "moveInDate";

    @Autowired private ApartmentSearchService apartmentSearchService;

    @Value("${application.base-url}")
    private String baseUrl;

    @Override
    protected Channel newFeed() {
        Channel channel = new Channel("rss_2.0");
        channel.setLink(baseUrl + ApartmentController.APARTMENT_FEED);
        channel.setTitle(CHANNEL_TITLE);
        channel.setDescription(CHANNEL_DESCRIPTION);
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

        item.setPubDate(Date.from(apartment.getDateFound().atStartOfDay(ZoneId.of("America/New_York")).toInstant()));
        return item;
    }

    private Description createDescription(Apartment apartment) {
        Description description = new Description();
        description.setType(Content.HTML);

        String link = UriComponentsBuilder.fromHttpUrl(APARTMENT_DETAIL_BASE_URL)
                .buildAndExpand(apartment.getApartmentCode())
                .toUriString();

        description.setValue(String.format("<a href='%s'>View Details</a>",
                link));
        return description;
    }

}
