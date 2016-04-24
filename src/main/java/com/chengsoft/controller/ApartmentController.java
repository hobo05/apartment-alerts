package com.chengsoft.controller;

import com.chengsoft.view.ApartmentRssFeedView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * Created by tcheng on 4/24/16.
 */
@Controller
public class ApartmentController {

    @Autowired
    ApartmentRssFeedView apartmentRssFeedView;

    @RequestMapping(value = "/apartment-feed/", produces = "application/*")
    public ApartmentRssFeedView getFeed(
            @RequestParam("moveInDate") String moveInDate,
            Model model) {
        model.addAttribute(ApartmentRssFeedView.MOVE_IN_DATE, LocalDate.parse(moveInDate));
        return apartmentRssFeedView;
    }

}
