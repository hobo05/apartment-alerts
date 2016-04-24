package com.chengsoft.controller;

import com.chengsoft.view.ApartmentRssFeedView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

import static com.chengsoft.view.ApartmentRssFeedView.*;

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
            @RequestParam(MOVE_IN_DATE) String moveInDate,
            Model model) {
        model.addAttribute(MOVE_IN_DATE, LocalDate.parse(moveInDate));
        return apartmentRssFeedView;
    }

}
