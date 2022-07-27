package com.test.java.controller;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.test.java.common.DataMap;
import com.test.java.common.RequestResolver;
import com.test.java.service.ServiceGateway;
import com.test.java.service.ServiceMethod;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	

	@Resource(name = "serviceGateway")
	private ServiceGateway serviceGateway;
	
	
	@RequestMapping(value = "/")
	public ModelAndView manaList(RequestResolver resolver, Locale locale, Model model ) throws Exception {
		
		List<DataMap> boardList = null;
		DataMap searchMap = resolver.getMap();
		
		boardList = serviceGateway.getList(new ServiceMethod("board.selectList", searchMap));
		
		int totalCnt = (Integer) serviceGateway.getOne(new ServiceMethod("board.selectListCount", searchMap));
		searchMap.put("totalCnt", totalCnt);
		System.out.println(searchMap);
		
		ModelAndView mv = new ModelAndView("layout");				
		mv.addObject("viewPath", "manaList");
		mv.addObject("searchMap", searchMap);		
		mv.addObject("boardList", boardList);
		
		return mv;
	}
	
	
	@RequestMapping(value = "/mana/manaDetail.do")
	public ModelAndView manaDetail(RequestResolver resolver, Model model) throws Exception {
		
		DataMap searchMap = resolver.getMap();
		DataMap manaDetail = (DataMap) serviceGateway.getOne(new ServiceMethod("board.select", searchMap));
		
		ModelAndView mv = new ModelAndView("/mana/manaDetail");				
		mv.addObject("manaDetail", manaDetail);
		
		return mv;
	}
	
	
	@RequestMapping(value = "/mana/manaCreate.do")
	public ModelAndView manaCreate(RequestResolver resolver, Model model) throws Exception {
		
		DataMap searchMap = resolver.getMap();
		DataMap manaCreate = (DataMap) serviceGateway.getOne(new ServiceMethod("board.select", searchMap));
		
		ModelAndView mv = new ModelAndView("/mana/manaCreate");				
		mv.addObject("manaCreate", manaCreate);
		
		return mv;
	}
	
}
