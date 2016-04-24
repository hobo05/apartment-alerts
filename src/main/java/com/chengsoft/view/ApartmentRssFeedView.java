package com.chengsoft.view;

import com.chengsoft.model.Apartment;
import com.chengsoft.service.ApartmentSearchService;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Content;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tcheng on 4/24/16.
 */
@Component
public class ApartmentRssFeedView extends AbstractRssFeedView {

    private static final String CHANNEL_TITLE = "Avalon Somerville One Bedroom Apartments";
    private static final String CHANNEL_DESCRIPTION = "Feed of One Bedroom Apartments";
    private static final String APARTMENT_DETAIL_BASE_URL = "http://www.avaloncommunities.com/massachusetts/somerville-apartments/ava-somerville/apartment/{apartmentCode}";

    @Autowired private ApartmentSearchService apartmentSearchService;

    @Value("${application.base-url}")
    private String baseUrl;

    @Override
    protected Channel newFeed() {
        Channel channel = new Channel("rss_2.0");
        channel.setLink(baseUrl + "/feed/");
        channel.setTitle(CHANNEL_TITLE);
        channel.setDescription(CHANNEL_DESCRIPTION);

        // TODO most recent publish date
//        documentService.getOneMostRecent().ifPresent(d -> channel.setPubDate(d.getDatePublished()));
        return channel;
    }

    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model,
                                        HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse) throws Exception {
        return apartmentSearchService.lookForNewApartments(LocalDate.of(2016, 6, 1)).stream()
                .map(this::createItem)
                .collect(Collectors.toList());
    }

    private Item createItem(Apartment apartment) {
        Item item = new Item();
        item.setLink(baseUrl + apartment.getApartmentNumber());
        item.setTitle(String.format("$%d - #%d", apartment.getPricing().getEffectiveRent(), apartment.getApartmentNumber()));
        item.setDescription(createDescription(apartment));
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

    private String encodeUrl(String link) {
        try {
            return URLEncoder.encode(link, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error while encoding link: ", e);
        }
        return null;
    }

}
