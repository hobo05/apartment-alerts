package com.chengsoft.controller;

import com.chengsoft.view.ApartmentRssFeedView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by tcheng on 4/24/16.
 */
@Controller
public class ApartmentController {

    @Autowired
    ApartmentRssFeedView apartmentRssFeedView;

    @RequestMapping(value = "/feed/", produces = "application/*")
    public ApartmentRssFeedView getFeed() {
        return apartmentRssFeedView;
    }

}
