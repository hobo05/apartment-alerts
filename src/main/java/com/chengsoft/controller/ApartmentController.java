package com.chengsoft.controller;

import com.chengsoft.view.ApartmentRssFeedView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

import static com.chengsoft.view.ApartmentRssFeedView.MOVE_IN_DATES;

/**
 * Created by tcheng on 4/24/16.
 */
@Controller
public class ApartmentController {

    public static final String APARTMENT_FEED = "/apartment-feed/";

    @Autowired
    ApartmentRssFeedView apartmentRssFeedView;

    @RequestMapping(value = APARTMENT_FEED, produces = "application/*")
    public ApartmentRssFeedView getFeed(
            @RequestParam(MOVE_IN_DATES) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) List<LocalDate> moveInDates,
            Model model) {

        model.addAttribute(MOVE_IN_DATES, moveInDates);

        return apartmentRssFeedView;
    }

}
