package com.soltworks.sds.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import com.nhncorp.lucy.security.xss.XssPreventer;
import com.soltworks.sds.aria.ARIAUtil;
import com.soltworks.sds.commons.CommCode;
import com.soltworks.sds.commons.CommFile;
import com.soltworks.sds.commons.CommPaging;
import com.soltworks.sds.dto.ContentsDto;
import com.soltworks.sds.dto.FrontDto;
import com.soltworks.sds.dto.FrontNoticeDto;
import com.soltworks.sds.dto.PageDto;
import com.soltworks.sds.service.ContentService;
import com.soltworks.sds.service.FrontNoticeService;
import com.soltworks.sds.service.FrontService;

@Controller
public class FrontController {

    private static final Logger logger = LoggerFactory.getLogger(FrontController.class);

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private Calendar today = Calendar.getInstance();

    private int pagingCnt = 5;
    private int recordCnt = 12;

    @Value("#{config['URL_IMAGE_PATH']}")
    private String imagePath;

    @Value("#{config['URL_DOWNLOAD_PATH']}")
    private String downloadPath;

    @Value("#{config['IG_SEARCH']}")
    private String IGSearch;

    @Resource
    MappingJacksonJsonView ajaxJsonView;

    @Autowired
    private FrontService frontService;

    @Autowired
    private ContentService contentService;

    @Autowired
    private FrontNoticeService noticeService;

    private String URL = "redirect:/";


    @RequestMapping(value = "/rolling", method = RequestMethod.GET)
    public ModelAndView intro(HttpSession session) {
        logger.info("URL : /rolling");
        ModelAndView mav = new ModelAndView("frt_rolling");
        return mav;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public void igFront(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        logger.info("URL : /igPage");
        try {
            response.sendRedirect(IGSearch);
        } catch (IOException e) {
            logger.info("예외 발생");
        }

    }

    @RequestMapping(value = "/main", method = RequestMethod.GET)
    public ModelAndView front(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        logger.info("URL : /main");
        ModelAndView mav = new ModelAndView("frt_main");  //      기존 :  /frt_contents/main.jsp
//		JasyptUtil jsay = new JasyptUtil();
//		jsay.main(null);
        FrontDto stats = new FrontDto();
        stats.setStPage("/");
        frontService.statisticsUpdate(stats);

        ArrayList<FrontDto> categoryList = new ArrayList<FrontDto>();
        ArrayList<FrontDto> categoryTwoDepthList = new ArrayList<FrontDto>();
        ArrayList<FrontDto> mostItemList = new ArrayList<FrontDto>();
        ArrayList<FrontDto> categoryAllList = new ArrayList<FrontDto>();
        ArrayList<FrontDto> categoryDepthList00 = new ArrayList<FrontDto>();
        ArrayList<FrontDto> categoryDepthList01 = new ArrayList<FrontDto>();
        ArrayList<FrontDto> categoryDepthList02 = new ArrayList<FrontDto>();
        ArrayList<FrontDto> categoryDepthList03 = new ArrayList<FrontDto>();
        ArrayList<FrontDto> bannerList = new ArrayList<FrontDto>();
        ArrayList<FrontNoticeDto> noticeList = new ArrayList<FrontNoticeDto>();
        ArrayList<FrontNoticeDto> customerList = new ArrayList<FrontNoticeDto>();
        FrontNoticeDto noticeDto = new FrontNoticeDto();
        FrontDto dto = new FrontDto();

        categoryList = frontService.getCategoryList();
        mostItemList = frontService.getMostItemList();
        categoryTwoDepthList = frontService.getCategoryTwoDepthList();

        noticeDto.setCount(2);
        noticeList = noticeService.getMainNoticeList(noticeDto);
        noticeDto.setType("main");
        customerList = noticeService.frtCustomerList(noticeDto);

        dto.setCategoryType("0");
        categoryAllList = frontService.getNewItemList(dto);
        dto.setCategoryType("1");
        categoryDepthList00 = frontService.getNewItemList(dto);
        dto.setCategoryType("2");
        categoryDepthList01 = frontService.getNewItemList(dto);
        dto.setCategoryType("3");
        categoryDepthList02 = frontService.getNewItemList(dto);
        dto.setCategoryType("4");
        categoryDepthList03 = frontService.getNewItemList(dto);

        //팝업
        FrontDto bannerDto = new FrontDto();
        Cookie cookies[] = request.getCookies();
        ArrayList<String> pmSeqArray = new ArrayList<String>();

        if(request.getCookies() != null) {
            for(int i = 0; i < cookies.length; i++) {
                Cookie obj = cookies[i];
                String pmSeq = (String) obj.getName();

                if(pmSeq.length() >= 7) {
                    if(pmSeq.substring(0, 6).equals("pmSeq_")) {
                        pmSeqArray.add(pmSeq.substring(6, pmSeq.length()));
                    }
                }
            }
        }
        bannerDto.setPmSeqArray(pmSeqArray);
        bannerList = frontService.getBanner(bannerDto);

        mav.addObject("categoryList", categoryList);
        mav.addObject("mostItemList", mostItemList);
        mav.addObject("categoryTwoDepthList", categoryTwoDepthList);
        mav.addObject("noticeList", noticeList);
        mav.addObject("customerList", customerList);
        mav.addObject("categoryAllList", categoryAllList);
        mav.addObject("categoryDepthList00", categoryDepthList00);
        mav.addObject("categoryDepthList01", categoryDepthList01);
        mav.addObject("categoryDepthList02", categoryDepthList02);
        mav.addObject("categoryDepthList03", categoryDepthList03);
        mav.addObject("banner", bannerList);
        return mav;
    }

    @RequestMapping(value = "product", method = RequestMethod.POST)
    public ModelAndView product(HttpSession session
            ,HttpServletRequest request
            ,@RequestParam(value="one_depth_search_val", defaultValue="-") String searchValue
            ,@RequestParam(value="one_depth_search_code", defaultValue="-") String searchCode
            ,@RequestParam(value="categoryCode", defaultValue="-") String categoryCode
    ) throws UnsupportedEncodingException {
        logger.info("URL : product");
        ModelAndView mav = new ModelAndView("frt_product");
        FrontDto stats = new FrontDto();

        stats.setStPage("product");
        frontService.statisticsUpdate(stats);
        searchValue = URLDecoder.decode(searchValue, "UTF-8");
        mav.addObject("searchValue", searchValue);
        mav.addObject("searchCode", searchCode);
        mav.addObject("categoryCode", categoryCode);

        if(!categoryCode.equals("-")) {
            mav.addObject("categoryStats", "true");
        }

        return mav;
    }

    @RequestMapping(value = "categoryProduct", method = RequestMethod.GET)
    public ModelAndView categoryProduct(HttpSession session
            ,HttpServletRequest request
            ,@RequestParam(value="now_page", defaultValue="0") String now_page
            ,@RequestParam(value="search_value", defaultValue="-") String searchValue
            ,@RequestParam(value="categoryCodeList", defaultValue="-") ArrayList<Object> categoryCodeList
            ,@RequestParam(value="orderByType", defaultValue="date") String orderByType
    ) {
        logger.info("URL : categoryProduct _ajax");
        ArrayList<FrontDto> itemList = new ArrayList<FrontDto>();
        ArrayList<Object> categoryList0 = new ArrayList<Object>();
        ArrayList<Object> categoryList1 = new ArrayList<Object>();
        ArrayList<Object> categoryList2 = new ArrayList<Object>();
        ArrayList<Object> categoryList3 = new ArrayList<Object>();
        FrontDto dto = new FrontDto();
        PageDto pageDto = new PageDto();

        String page	= now_page;
        int totalCount = 0;
        int nowPage = 1;
        int pageCount = 0;
        int topListSize = 0;

        if(page.equals("0")) {
            nowPage = 1;
        } else {
            nowPage = Integer.parseInt(page);
        }
        searchValue = XssPreventer.escape(searchValue);

        //		int count = 0;
        dto.setOrderByType(orderByType);
        dto.setSearchValue(searchValue);

        if(categoryCodeList.get(0).equals("-")) {
            dto.setCategoryCode("-");
        } else {
            dto.setCategoryCodeList(categoryCodeList);
            logger.info("categoryCode.size : " + categoryCodeList.size());
            for(int i =0; i <categoryCodeList.size(); i++) {
                logger.info("categoryCode : " + categoryCodeList.get(i).toString());
                logger.info("categoryCode : " + categoryCodeList.get(i).toString().substring(0,2));

                if(categoryCodeList.get(i).toString().substring(0,2).equals("00")) {
                    categoryList0.add(categoryCodeList.get(i).toString());
                } else if(categoryCodeList.get(i).toString().substring(0,2).equals("01")) {
                    categoryList1.add(categoryCodeList.get(i).toString());
                } else if(categoryCodeList.get(i).toString().substring(0,2).equals("02")) {
                    categoryList2.add(categoryCodeList.get(i).toString());
                } else if(categoryCodeList.get(i).toString().substring(0,2).equals("03")) {
                    categoryList3.add(categoryCodeList.get(i).toString());
                }
                //count += i;
            }
            dto.setCount(categoryCodeList.size());
            dto.setCategoryOCodeList(categoryCodeList);
            dto.setCategoryCodeList(categoryList0);
            dto.setCategoryCodeList1(categoryList1);
            dto.setCategoryCodeList2(categoryList2);
            dto.setCategoryCodeList3(categoryList3);
            //dto.setCategoryCodeListSize(categoryCodeList.size());
            dto.setCategoryCodeListSize(dto.getCategoryCodeList().size());
            dto.setCategoryCodeList1Size(dto.getCategoryCodeList1().size());
            dto.setCategoryCodeList2Size(dto.getCategoryCodeList2().size());
            dto.setCategoryCodeList3Size(dto.getCategoryCodeList3().size());
        }

        totalCount =  frontService.getSearchItemCount(dto);

        pageCount = totalCount / (recordCnt - topListSize) + 1;

        if(totalCount % (recordCnt - topListSize) == 0){
            pageCount = totalCount / (recordCnt - topListSize);
        }
        if(nowPage > pageCount) {
            nowPage = 1;
        }
        pageDto.setNowPage(nowPage);
        pageDto.setTotalCount(totalCount);

        CommPaging commPage = new CommPaging(pageDto, recordCnt - topListSize, pagingCnt - topListSize, "pageEvent");

        dto.setStartRow(commPage.getStartRow());
        dto.setEndRow(commPage.getEndRow());
        dto.setSearchType("-");

        itemList = frontService.getCategoryItemList(dto);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("itemList", itemList);
        map.put("totalCount" ,totalCount);
        searchValue = XssPreventer.escape(searchValue);
        map.put("searchValue" ,searchValue);
        map.put("pageTag", commPage.resultString());
        return new ModelAndView(ajaxJsonView, map);
    }

    @RequestMapping(value = "categoryTree", method = RequestMethod.GET)
    public ModelAndView categoryTree(HttpSession session) {
        logger.info("URL : categoryTree _ajax");
        Map<String, ArrayList<FrontDto>> map = new HashMap<String, ArrayList<FrontDto>>();
        map.put("result" ,frontService.getCategoryTreeList());

        return new ModelAndView(ajaxJsonView, map);
    }

    @RequestMapping(value = "searchItems", method = RequestMethod.GET)
    public ModelAndView searchItems(HttpSession session
            ,@RequestParam(value="search_value", defaultValue="-") String searchValue
            ,@RequestParam(value="search_code", defaultValue="-") String searchCode
            ,@RequestParam(value="orderByType", defaultValue="date") String orderByType
            ,@RequestParam(value="searchType", defaultValue="-") String searchType
    ) {
        logger.info("URL : searchItems _ajax");
        ArrayList<FrontDto> itemList = new ArrayList<FrontDto>();
        FrontDto dto = new FrontDto();
        PageDto pageDto = new PageDto();

        String page	= "0";
        int totalCount = 0;
        int nowPage = 1;
        int pageCount = 0;
        int topListSize = 0;

        if(page.equals("0")) {
            nowPage = 1;
        } else {
            nowPage = Integer.parseInt(page);
        }

        searchValue = XssPreventer.escape(searchValue);

        dto.setOrderByType(orderByType);
        dto.setSearchValue(searchValue);
        dto.setSearchCode(searchCode);

		/*
		if(searchCode.equals("all")) {
			itemList = frontService.getAllItemList(dto);
		} else {
			itemList = frontService.getOneDepthItemList(dto);
		}
		*/

        totalCount =  frontService.getSearchItemCount(dto);

        pageCount = totalCount / (recordCnt - topListSize) + 1;

        if(totalCount % (recordCnt - topListSize) == 0){
            pageCount = totalCount / (recordCnt - topListSize);
        }
        if(nowPage > pageCount) {
            nowPage = 1;
        }
        pageDto.setNowPage(nowPage);
        pageDto.setTotalCount(totalCount);

        CommPaging commPage = new CommPaging(pageDto, recordCnt - topListSize, pagingCnt - topListSize, "pageEvent");

        dto.setStartRow(commPage.getStartRow());
        dto.setEndRow(commPage.getEndRow());
        dto.setSearchType(searchType);

        itemList = frontService.getCategoryItemList(dto);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("itemList" ,itemList);
        map.put("totalCount" ,totalCount);
        searchValue = XssPreventer.escape(searchValue);
        map.put("searchValue" ,searchValue);
        map.put("searchCode" ,searchCode);
        map.put("pageTag", commPage.resultString());

        return new ModelAndView(ajaxJsonView, map);
    }

    @RequestMapping(value = "productView/{itemSeq}", method = RequestMethod.GET)
    public ModelAndView productView(HttpSession session ,HttpServletRequest request
            ,@PathVariable("itemSeq") int itemSeq
    ) {
        logger.info("URL : productView");
        ModelAndView mav = new ModelAndView("frt_product_View");

        FrontDto stats = new FrontDto();
        stats.setStPage("productView");

        frontService.statisticsUpdate(stats);

        FrontDto dto = new FrontDto();
        FrontDto itemDetail = new FrontDto();
        ArrayList<FrontDto> itemCategoryList = new ArrayList<FrontDto>();
        ArrayList<FrontDto> RelatedItemList = new ArrayList<FrontDto>();
        ArrayList<FrontDto> commentList = new ArrayList<FrontDto>();
        dto.setItemSeq(itemSeq);

        itemDetail = frontService.getItemDetailInfo(dto);
        itemCategoryList = frontService.getItemCategoList(dto);
        RelatedItemList = frontService.getRelatedItemList(dto);
        commentList = frontService.getCommentList(dto);

        mav.addObject("itemDetail", itemDetail);
        mav.addObject("itemCategoryList", itemCategoryList);
        mav.addObject("RelatedItemList", RelatedItemList);
        mav.addObject("commentList", commentList);
        mav.addObject("commentListSize", commentList.size());
        mav.addObject("downloadPath", downloadPath);
        return mav;
    }

    @RequestMapping(value = "downImgView", method = RequestMethod.POST)
    public ModelAndView downImgView(@RequestParam(value="down_path", defaultValue="") String downPath
            ,@RequestParam(value="down_img_name", defaultValue="") String downImgName
    ) {
        logger.info("URL : downImgView");
        ModelAndView mav = new ModelAndView("/frt_contents/pup_down_view");

        String extChk = "img";
        int pos = downImgName.lastIndexOf(".");
        String ext =  downImgName.substring(pos + 1);

        if(ext.equals("pdf")) {
            extChk = "pdf";
        }

        mav.addObject("extChk", extChk);
        mav.addObject("downPath", downPath);
        mav.addObject("downImgName", downImgName);
        logger.info(".." + downImgName);
        logger.info(".." + extChk);
        return mav;
    }

    @RequestMapping(value = "productDown", method = RequestMethod.GET)
    public void fileDownload(HttpServletRequest request, HttpServletResponse response
            ,@RequestParam(value="fileName", defaultValue="") String fileName
            ,@RequestParam(value="orgFileName", defaultValue="") String orgFileName
    ) {
        logger.info("URL : productDown");
        CommFile commFile = new CommFile();
        commFile.fileDownLoad(request, response, downloadPath, fileName, orgFileName);
    }

    @RequestMapping(value = "productFileDown", method = RequestMethod.POST)
    public void productFileDown(HttpServletRequest request, HttpServletResponse response
            ,@RequestParam(value="fileName", defaultValue="") String fileName
            ,@RequestParam(value="orgFileName", defaultValue="") String orgFileName
    ) {
        logger.info("URL : productFileDown");
        CommFile commFile = new CommFile();
        commFile.fileDownLoad(request, response, downloadPath, fileName, orgFileName);
    }

    @RequestMapping(value = "productCart", method = RequestMethod.POST)
    public ModelAndView productCart(HttpSession session
            ,@RequestParam(value="item_seq") String itemSeq
            ,@RequestParam(value="amount") String itemAmount
    ) {
        logger.info("URL : productCart _ajax");
        FrontDto dto = new FrontDto();
        ArrayList<FrontDto> cartList = new ArrayList<>();

        String auth = (String)session.getAttribute("SS_AUTHORITY");
        dto.setIndNo((String)session.getAttribute("SS_INDNO"));
        dto.setIndopenNo((String)session.getAttribute("SS_INDOPENNO"));
        dto.setInddisNo((String)session.getAttribute("SS_INDDISNO"));

        dto.setItemSeq(Integer.parseInt(itemSeq));
        dto.setItemAmount(Integer.parseInt(itemAmount));

        Map<String, Object> map = new HashMap<String, Object>();
        if(auth != null) {
            if(auth.equals("ROLE_B") || auth.equals("ROLE_G")) {
                int customerSeq = frontService.getCustomerSeq(dto);
                dto.setCustomerSeq(customerSeq);
                cartList = frontService.getUserItemCartList(dto);
            } else {
                dto.setSessionId(session.getId());
                cartList = frontService.getItemCartList(dto);
            }
            map.put("cartList", cartList);
        } else {
            map.put("cartList", null);
        }

        return new ModelAndView(ajaxJsonView ,map);
    }

    @RequestMapping(value = "getCustomer", method = RequestMethod.POST)
    public ModelAndView myIndNo(HttpSession session
            ,@RequestParam(value="indNo") String indNo
            ,@RequestParam(value="indopenNo") String indopenNo
            ,@RequestParam(value="inddisNo") String inddisNo
            ,@RequestParam(value="indNm", defaultValue="-") String indNm
            ,@RequestParam(value="postNo", defaultValue="-") String postNo
            ,@RequestParam(value="addr1", defaultValue="-") String addr1
            ,@RequestParam(value="addr2", defaultValue="-") String addr2
            ,@RequestParam(value="tel", defaultValue="-") String tel
            ,@RequestParam(value="ownerNm", defaultValue="-") String ownerNm
            ,@RequestParam(value="regEmpCnt", defaultValue="-") int regEmpCnt
            ,@RequestParam(value="scaleCd", defaultValue="-") String scaleCd
            ,@RequestParam(value="cateCd", defaultValue="-") String cateCd
            ,@RequestParam(value="cateCdInd", defaultValue="-") String cateCdInd
            ,@RequestParam(value="busnCon", defaultValue="-") String busnCon
            ,@RequestParam(value="busnItem", defaultValue="-") String busnItem
            ,@RequestParam(value="constNm", defaultValue="-") String constNm
            ,@RequestParam(value="constTypeCd", defaultValue="-") String constTypeCd
            ,@RequestParam(value="constSizeCd", defaultValue="-") String constSizeCd
            ,@RequestParam(value="constPostCd", defaultValue="-") String constPostCd
            ,@RequestParam(value="constAddr1", defaultValue="-") String constAddr1
            ,@RequestParam(value="constAddr2", defaultValue="-") String constAddr2
            ,@RequestParam(value="mngAgent", defaultValue="-") String mngAgent
    ) {
        logger.info("URL : getCustomer _ajax");

        logger.info("scaleCd : " + scaleCd);

        Map<String, Object> map = new HashMap<String, Object>();
        FrontDto dto = new FrontDto();
        FrontDto dto1 = new FrontDto();
        FrontDto dto2 = new FrontDto();
        dto.setIndNo(indNo);
        dto.setIndopenNo(indopenNo);
        dto.setInddisNo(inddisNo);

        dto1.setIndNo(indNo);
        dto1.setIndopenNo(indopenNo);
        dto1.setInddisNo(inddisNo);
        dto1.setIndNm(indNm);
        dto1.setPostNo(postNo);
        dto1.setAddr1(addr1);
        dto1.setAddr2(addr2);
        dto1.setTel(tel);
        dto1.setOwnerNm(ownerNm);
        dto1.setRegEmpCnt(regEmpCnt);
        dto1.setScaleCd(scaleCd);
        dto1.setCateCd(cateCd);
        dto1.setCateCdInd(cateCdInd);
        dto1.setBusnCon(busnCon);
        dto1.setBusnItem(busnItem);
        dto1.setConstNm(constNm);
        dto1.setConstTypeCd(constTypeCd);
        dto1.setConstSizeCd(constSizeCd);
        dto1.setConstPostCd(constPostCd);
        dto1.setConstAddr1(constAddr1);
        dto1.setConstAddr2(constAddr2);
        dto1.setMngAgent(mngAgent);

        dto2.setIndNo(indNo);
        dto2.setIndopenNo(indopenNo);
        dto2.setInddisNo(inddisNo);
        dto2.setIndNm(indNm);
        dto2.setPostNo(postNo);
        dto2.setAddr1(addr1);
        dto2.setAddr2(addr2);
        dto2.setTel(tel);
        dto2.setOwnerNm(ownerNm);
        dto2.setRegEmpCnt(regEmpCnt);
        dto2.setScaleCd(scaleCd);
        dto2.setCateCd(cateCd);
        dto2.setCateCdInd(cateCdInd);
        dto2.setBusnCon(busnCon);
        dto2.setBusnItem(busnItem);
        dto2.setConstNm(constNm);
        dto2.setConstTypeCd(constTypeCd);
        dto2.setConstSizeCd(constSizeCd);
        dto2.setConstPostCd(constPostCd);
        dto2.setConstAddr1(constAddr1);
        dto2.setConstAddr2(constAddr2);
        dto2.setMngAgent(mngAgent);

        dto = frontService.getcustomer(dto);

        if(dto != null) {				// 공단 or 유관기관

            logger.info("|| ROLE : " + dto.getAuthority());
//			logger.info("|| SDate : " + dto.getCustomerSDate());
//			logger.info("|| EDate : " + dto.getCustomerEDate());
            logger.info("|| status : " + dto.getStatusKind());

            if(dto.getStatusKind().equals("10")) {
				/*
				int sdate = Integer.parseInt(dto.getCustomerSDate());
				int edate = Integer.parseInt(dto.getCustomerEDate());
				int year = today.get(today.YEAR)*10000;
				int month = (today.get(today.MONTH)+1)*100;
				int day = today.get(today.DAY_OF_MONTH);
				int now = year+month+day;

				if(now >= sdate && now <= edate) {
					map.put("result", 1);		// 사용 ok
					session.setAttribute("SS_AUTHORITY", dto.getAuthority());
					addSession(session, dto1);
					map.put("dto", dto1);
				} else {
					map.put("result", 2);		// 사용기간 no
				}
				*/
                map.put("result", 1);		// 사용 ok
                session.setAttribute("SS_AUTHORITY", dto.getAuthority());
                addSession(session, dto1);
                map.put("dto", dto1);

            } else if(dto.getStatusKind().equals("30")) {
                map.put("result", 3);			// 비활성화
            } else {
                session.setAttribute("SS_AUTHORITY", "ROLE_U");
                addSession(session, dto2);
                map.put("dto", dto2);
                map.put("result", 1);				// 삭제 --> 일반
            }
        } else {
            session.setAttribute("SS_AUTHORITY", "ROLE_U");
            addSession(session, dto2);
            map.put("dto", dto2);
            map.put("result", 1);				// 일반
        }

        return new ModelAndView(ajaxJsonView ,map);
    }

    public void addSession(HttpSession session, FrontDto dto) {

        //business info 22개
        session.setAttribute("SS_INDNM", dto.getIndNm());
        session.setAttribute("SS_INDNO", dto.getIndNo());
        session.setAttribute("SS_INDOPENNO", dto.getIndopenNo());
        session.setAttribute("SS_INDDISNO", dto.getInddisNo());
        session.setAttribute("SS_POST_NO", dto.getPostNo());
        session.setAttribute("SS_ADDR1", dto.getAddr1());
        session.setAttribute("SS_ADDR2", dto.getAddr2());
        session.setAttribute("SS_TEL", dto.getTel());
        session.setAttribute("SS_OWNERNM", dto.getOwnerNm());
        session.setAttribute("SS_REGEMPCNT", dto.getRegEmpCnt());

        session.setAttribute("SS_SCALECD", dto.getScaleCd());
        session.setAttribute("SS_CATECD", dto.getCateCd());
        session.setAttribute("SS_CATECDIND", dto.getCateCdInd());

        session.setAttribute("SS_BUSNCON", dto.getBusnCon());
        session.setAttribute("SS_BUSNITEM", dto.getBusnItem());
        session.setAttribute("SS_CONSTNM", dto.getConstNm());
        session.setAttribute("SS_CONSTTYPECD", dto.getConstTypeCd());
        session.setAttribute("SS_CONSTSIZECD", dto.getConstSizeCd());
        session.setAttribute("SS_CONSTPOSTCD", dto.getConstPostCd());
        session.setAttribute("SS_CONSTADDR1", dto.getConstAddr1());
        session.setAttribute("SS_CONSTADDR2", dto.getConstAddr2());
        session.setAttribute("SS_MNGAGENT", dto.getMngAgent());

		/*
		logger.info("SS_INDNM : " + dto.getIndNm());
		logger.info("SS_INDNO : " + dto.getIndNo());
		logger.info("SS_INDOPENNO : " + dto.getIndopenNo());
		logger.info("SS_INDDISNO : " + dto.getInddisNo());
		logger.info("SS_POST_NO : " + dto.getPostNo());
		logger.info("SS_ADDR1 : " + dto.getAddr1());
		logger.info("SS_ADDR2 : " + dto.getAddr2());
		logger.info("SS_TEL : " + dto.getTel());
		logger.info("SS_OWNERNM : " + dto.getOwnerNm());
		logger.info("SS_REGEMPCNT : " + dto.getRegEmpCnt());

		logger.info("SS_SCALECD : " + dto.getScaleCd());
		logger.info("SS_CATECD : " + dto.getCateCd());
		logger.info("SS_CATECDIND : " + dto.getCateCdInd());

		logger.info("SS_BUSNCON : " + dto.getBusnCon());
		logger.info("SS_BUSNITEM : " + dto.getBusnItem());
		logger.info("SS_CONSTNM : " + dto.getConstNm());
		logger.info("SS_CONSTTYPECD : " + dto.getConstTypeCd());
		logger.info("SS_CONSTSIZECD : " + dto.getConstSizeCd());
		logger.info("SS_CONSTPOSTCD : " + dto.getConstPostCd());
		logger.info("SS_CONSTADDR1 : " + dto.getConstAddr1());
		logger.info("SS_CONSTADDR2 : " + dto.getConstAddr2());
		logger.info("SS_MNGAGENT : " + dto.getMngAgent());
		*/
    }

    @RequestMapping(value = "myProductCart", method = RequestMethod.GET)
    public ModelAndView myCartList(HttpSession session, HttpServletRequest request
    ) {
        logger.info("URL : myProductCart");
        ModelAndView mav = new ModelAndView("frt_cart");

        if(session.getAttribute("SS_AUTHORITY") == null) {
            mav.setViewName(URL);
            return mav;
        }

        FrontDto stats = new FrontDto();
        stats.setStPage("myProductCart");
        frontService.statisticsUpdate(stats);

        FrontDto dto = new FrontDto();
        ArrayList<FrontDto> cartList = new ArrayList<>();

        ArrayList<FrontDto> cartResultList = new ArrayList<>();
        ArrayList<FrontDto> cartBookList = new ArrayList<>();

        int ordersLimited = 0;

        String auth = (String)session.getAttribute("SS_AUTHORITY");
        dto.setIndNo((String)session.getAttribute("SS_INDNO"));
        dto.setIndopenNo((String)session.getAttribute("SS_INDOPENNO"));
        dto.setInddisNo((String)session.getAttribute("SS_INDDISNO"));

        if(auth.equals("ROLE_B") || auth.equals("ROLE_G")) {
            cartList = frontService.getUserCartList(dto);
            ordersLimited = frontService.getCustomerLimitOrder(dto);
        } else {
            dto.setSessionId(session.getId());
            cartList = frontService.getMyCartList(dto);
            ordersLimited = frontService.getCheckItemNum(dto);

            for(int i = 0; i < cartList.size(); i++) {
                if(cartList.get(i).getCategoryType().equals("book")) {
                    // 책자
                    cartBookList.add(cartList.get(i));
                } else {
                    // 나머지
                    cartResultList.add(cartList.get(i));
                }
            }
        }
       	/*
       	 * 배송쿠폰제
       	 *
		FrontDto cpnInfo = new FrontDto();
		cpnInfo.setIndNo((String)session.getAttribute("SS_INDNO"));
		cpnInfo.setIndopenNo((String)session.getAttribute("SS_INDOPENNO"));
		cpnInfo.setInddisNo((String)session.getAttribute("SS_INDDISNO"));
		String couponStats = frontService.getCouponStats(cpnInfo);
		if(couponStats != null) {
			mav.addObject("COUPONSTATS", couponStats);
		} else {
			mav.addObject("COUPONSTATS", 40);
		}
		*/

        mav.addObject("cartList", cartList);
        mav.addObject("cartSize" ,cartList.size());
        mav.addObject("ordersLimited", ordersLimited);
        return mav;
    }

    @RequestMapping(value = "myCartDelete", method = RequestMethod.GET)
    public ModelAndView myCartDelete(HttpSession session
            ,@RequestParam(value="item_cart_seq", defaultValue="-") String itemCartSeq
            ,@RequestParam(value="cartList", defaultValue="-") ArrayList<Object> cartList
    ) {
        logger.info("URL : myCartDelete _ajax");
        logger.info("cartList.size() : " + cartList.size());

        FrontDto dto = new FrontDto();

        dto.setItemCartSeq(Integer.parseInt(itemCartSeq));
        dto.setCartList(cartList);

        String auth = (String)session.getAttribute("SS_AUTHORITY");
        if(auth.equals("ROLE_B") || auth.equals("ROLE_G")) {
//			dto.setCustomerIndNo(customerIndNo);
            frontService.userCartDelete(dto);
        } else {
            dto.setSessionId(session.getId());
            frontService.myCartDelete(dto);
        }

        return new ModelAndView(ajaxJsonView);
    }

    @RequestMapping(value = "productOrder", method = RequestMethod.POST)
    public ModelAndView productOrder(HttpSession session
            ,@RequestParam(value="cartList", defaultValue = "-") ArrayList<Object> cartList
            ,@RequestParam(value="itemCountList", defaultValue = "-") ArrayList<Object> itemCountList
            ,@RequestParam(value="totalAmount", defaultValue = "-") String totalAmount
    ) {
        logger.info("URL : productOrder");
        ModelAndView mav = new ModelAndView("frt_request_delivery");

        if(session.getAttribute("SS_AUTHORITY") == null) {
            mav.setViewName(URL);
            return mav;
        }
        ArrayList<FrontDto> resultList = new ArrayList<FrontDto>();
        ArrayList<FrontDto> repList = new ArrayList<FrontDto>();

        FrontDto dto = new FrontDto();
        dto.setCartList(cartList);
        resultList = frontService.getOrderItemList(dto);
        repList = frontService.getRecipientList();

        for(int index = 0; index < resultList.size(); index++){
            for(int cindex = 0; cindex < cartList.size(); cindex++) {
                if(resultList.get(index).getItemSeq() == Integer.parseInt(cartList.get(cindex).toString())) {
                    resultList.get(index).setItemAmount(Integer.parseInt((String)itemCountList.get(cindex)));
                }
            }
        }
        int allCnt = 0;
        int bookCnt = 0;
        int totalA = 0;
        for(int i = 0; i < resultList.size(); i++) {

            if(resultList.get(i).getCategoryType().equals("all")) {
                allCnt += resultList.get(i).getItemAmount();
            } else if(resultList.get(i).getCategoryType().equals("book")){
                bookCnt += resultList.get(i).getItemAmount();
            }
        }

        if(allCnt != 0) {
            if(allCnt <= 30) {
                allCnt = 3000;
            } else {
                allCnt = (int)(Math.ceil((double)allCnt/(double)30)) * 3000;
            }
        }

        if(bookCnt != 0) {
            if(bookCnt <= 10) {
                bookCnt = 3000;
            } else {
                bookCnt = (int)(Math.ceil((double)bookCnt/(double)10)) * 3000;
            }
        }

		/*
		 * 배송쿠폰제
		 *
		FrontDto cpnInfo = new FrontDto();
		cpnInfo.setIndNo((String)session.getAttribute("SS_INDNO"));
		cpnInfo.setIndopenNo((String)session.getAttribute("SS_INDOPENNO"));
		cpnInfo.setInddisNo((String)session.getAttribute("SS_INDDISNO"));
		String couponStats = frontService.getCouponStats(cpnInfo);
		if(couponStats != null) {
			mav.addObject("COUPONSTATS", couponStats);
		} else {
			mav.addObject("COUPONSTATS", 40);
		}
		*/
        totalA = allCnt + bookCnt;

        mav.addObject("resultList" ,resultList);
        mav.addObject("repList" ,repList);
        mav.addObject("totalAmount" ,totalA);

        return mav;
    }

    @RequestMapping(value = "productOrderCheck", method = RequestMethod.POST)
    public ModelAndView productOrderCheck(HttpSession session
            ,@RequestParam(value="business_name", defaultValue = "-") String businessName
            ,@RequestParam(value="business_code", defaultValue = "-") String businessCode
            ,@RequestParam(value="business_dis_code", defaultValue = "-") String businessDisCode
            ,@RequestParam(value="business_open_code", defaultValue = "-") String businessOpenCode
            ,@RequestParam(value="buyer_name", defaultValue = "-") String buyerName
            ,@RequestParam(value="part_name", defaultValue = "-") String partName
            ,@RequestParam(value="buyer_phone", defaultValue = "-") String buyerPhone
            ,@RequestParam(value="buyer_addr", defaultValue = "-") String buyerAddr

            ,@RequestParam(value="buyer_post", defaultValue = "-") String buyerPost
            ,@RequestParam(value="buyer_pos", defaultValue = "-") String buyerPos
            ,@RequestParam(value="buyer_cate", defaultValue = "-") String buyerCate
            ,@RequestParam(value="buyer_scale", defaultValue = "-") String buyerScale
            ,@RequestParam(value="buyer_regemp_cnt", defaultValue = "-") int buyerRegEmpCnt
            ,@RequestParam(value="buyer_mngagent", defaultValue = "-") String buyerMngAgent
            ,@RequestParam(value="buyer_amount", defaultValue = "-") String buyerAmount
            ,@RequestParam(value="categoryType", defaultValue = "all") String categoryType
            ,@RequestParam(value="cartList", defaultValue = "-") ArrayList<Object> cartList
            ,@RequestParam(value="itemCountList", defaultValue = "-") ArrayList<Object> itemCountList
            ,@RequestParam(value="orderer_nm", defaultValue = "-") String ordererNm
            ,@RequestParam(value="orderer_cd", defaultValue = "-") String ordererCd
            ,@RequestParam(value="orderer_open_cd", defaultValue = "-") String ordererOpenCd
            ,@RequestParam(value="orderer_dis_cd", defaultValue = "-") String ordererDisCd
    ) {
        logger.info("URL : productOrderCheck");
        ArrayList<FrontDto> resultList = new ArrayList<FrontDto>();
        FrontDto dto = new FrontDto();

        businessName = XssPreventer.escape(businessName);
        partName = XssPreventer.escape(partName);
        buyerMngAgent = XssPreventer.escape(buyerMngAgent);

        CommCode code = new CommCode();
        buyerCate = code.cateCode(buyerCate);			// 업종상세코드
        buyerScale = code.scaleCode(buyerScale);		// 기업규모

        int temp = 0;
        int deliveryCharge = 0;
        for (int i = 0; i < itemCountList.size(); i++) {
            temp += Integer.parseInt((String) itemCountList.get(i));
        }

        int orderCaseCnt = 0;
        if(categoryType.equals("all")) {
            if(temp >= 30) {
                if(temp % 30 == 0) {
                    orderCaseCnt = temp/30;
                } else {
                    orderCaseCnt = temp/30 + 1;
                }
            } else {
                orderCaseCnt = 1;
            }
        } else { // book
            if(temp >= 10) {
                if(temp % 10 == 0) {
                    orderCaseCnt = temp/10;
                } else {
                    orderCaseCnt = temp/10 + 1;
                }
            } else {
                orderCaseCnt = 1;
            }
        }
        deliveryCharge = orderCaseCnt * 3000;

        try {
            if(buyerName != null) {
                dto.setBuyerName(ARIAUtil.ariaEncrypt(buyerName));
            }
            if(buyerPhone != null) {
                dto.setBuyerPhone(ARIAUtil.ariaEncrypt(buyerPhone));
            }
            if(buyerAddr != null) {
                dto.setBuyerAddr(ARIAUtil.ariaEncrypt(buyerAddr));
            }
        } catch (Exception e) {
            logger.info("Exception : " + e.toString());
        }

        String auth = (String)session.getAttribute("SS_AUTHORITY");

        dto.setOrdererNm(ordererNm);
        dto.setOrdererCd(ordererCd);
        dto.setOrdererDisCd(ordererDisCd);
        dto.setOrdererOpenCd(ordererOpenCd);

        dto.setBusinessName(businessName);
        dto.setBusinessCode(businessCode);
        dto.setBusinessDisCode(businessDisCode);
        dto.setBusinessOpenCode(businessOpenCode);
        dto.setPartName(partName);
        dto.setBuyerCate(buyerCate);
        dto.setBuyerPost(buyerPost);
        dto.setBuyerPos(buyerPos);
        dto.setBuyerScale(buyerScale);
        dto.setBuyerMngAgent(buyerMngAgent);
        dto.setBuyerRegEmpCnt(buyerRegEmpCnt);
        dto.setCartList(cartList);
        dto.setItemCountList(itemCountList);
        dto.setSessionId(session.getId());
//		dto.setBuyerAmount(buyerAmount);
        dto.setBuyerAmount(Integer.toString(deliveryCharge));
        dto.setCategoryType(categoryType);
        dto.setUserCd(auth);
        dto.setCaseCnt(orderCaseCnt);

        try {
            resultList = frontService.productOrderCheck(dto);
            if(resultList != null) throw new Exception();
        } catch (Exception e) {
            logger.info("Exception : " + e.toString());
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultList", resultList);

        return new ModelAndView(ajaxJsonView ,map);
    }

    @RequestMapping(value = "orderCompleted/{orderSeq}", method = RequestMethod.GET)
    public ModelAndView orderCompleted(HttpSession session
            ,@PathVariable("orderSeq") int orderSeq) {
        logger.info("URL : orderCompleted");
        ModelAndView mav = new ModelAndView("frt_application_finish");

        if(session.getAttribute("SS_AUTHORITY") == null) {
            mav.setViewName(URL);
            return mav;
        }

        logger.info("orderSeq : " + orderSeq);
        FrontDto dto = new FrontDto();
        FrontDto resultDto = new FrontDto();
        dto.setOrderSeq(orderSeq);

        resultDto = frontService.getOrderBuyerInfo(dto);

        try {
            // 암호화
            resultDto.setBuyerName(ARIAUtil.ariaDecrypt(resultDto.getBuyerName()));
            resultDto.setBuyerPhone(ARIAUtil.ariaDecrypt(resultDto.getBuyerPhone()));
            resultDto.setBuyerAddr(ARIAUtil.ariaDecrypt(resultDto.getBuyerAddr()));
        } catch (Exception e) {
            logger.info("Exception : " + e.toString());
        }
        mav.addObject("resultDto", resultDto);
        return mav;
    }

    @RequestMapping(value = "myBusiness", method = RequestMethod.GET)
    public ModelAndView myBusiness(HttpSession session) {
        logger.info("URL : myBusiness");
        ModelAndView mav = new ModelAndView("frt_my_business");
        return mav;
    }
    /*
        @RequestMapping(value = "myBusinessSearch/{indNo}") // , method = RequestMethod.GET
        public ModelAndView myBusinessSearch(HttpSession session
                                            ,@PathVariable("indNo") String indNo
                                            ) {
            logger.info("URL : myBusinessSearch _ajax");
            FrontDto dto = new FrontDto();
            ArrayList<FrontDto> resultList = new ArrayList<FrontDto>();
            dto.setIndNo(indNo);
            resultList = frontService.getBuyerOrderSeq(dto);

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("resultList", resultList);

            return new ModelAndView(ajaxJsonView ,map);
        }
    */
    @RequestMapping(value = "myBusinessInfo", method = RequestMethod.POST)
    public ModelAndView myBusinessInfo(HttpSession session
            ,@RequestParam(value="indNo", defaultValue="-") String indNo
            ,@RequestParam(value="indopenNo", defaultValue="-") String indopenNo
            ,@RequestParam(value="inddisNo", defaultValue="-") String inddisNo
    ) {
        logger.info("URL : myBusinessInfo _ajax");
        ModelAndView mav = new ModelAndView("frt_my_business_list");

        String auth = (String)session.getAttribute("SS_AUTHORITY");

        if(auth == null) {
            mav.setViewName(URL);
            return mav;
        }

        ArrayList<FrontDto> resultList = new ArrayList<FrontDto>();

        ArrayList<FrontDto> recommendList01 = new ArrayList<FrontDto>();
        ArrayList<FrontDto> recommendList02 = new ArrayList<FrontDto>();
        ArrayList<FrontDto> recommendList03 = new ArrayList<FrontDto>();
//		ArrayList<FrontDto> recommendList04 = new ArrayList<FrontDto>();

        FrontDto dto = new FrontDto();
        if(indNo.equals("-") || indNo.equals("") ||indNo == null) {
            dto.setIndNo((String)session.getAttribute("SS_INDNO"));
            dto.setIndopenNo((String)session.getAttribute("SS_INDOPENNO"));
            dto.setInddisNo((String)session.getAttribute("SS_INDDISNO"));
        } else {
            dto.setIndNo(indNo);
            dto.setIndopenNo(indopenNo);
            dto.setInddisNo(inddisNo);
        }

        String cate = "";
        String scale = "";
        String addr1 = "";
        String addr2 = "";
        String addr3 = "";
        String rAddr = "";
        String rAddr2 = "";

        CommCode code = new CommCode();
        scale = code.scaleCode((String)session.getAttribute("SS_SCALECD"));		// 기업규모
        try {
            addr1 = (String)session.getAttribute("SS_ADDR1");	// 주소
            rAddr = addr1.split(" ")[0]+" "+addr1.split(" ")[1];
            rAddr2 = addr1.split(" ")[0];
            addr1 = ARIAUtil.ariaEncrypt(rAddr);
            addr2 = ARIAUtil.ariaEncrypt(rAddr2);
            addr3 = ARIAUtil.ariaEncrypt("서울시");
        } catch (InvalidKeyException e) {
            logger.info("InvalidKeyException" + e.toString());
        } catch (UnsupportedEncodingException e) {
            logger.info("UnsupportedEncodingException" + e.toString());
        }
        cate = code.cateCode((String)session.getAttribute("SS_CATECD"));			// 업종상세코드

        dto.setBuyerScale(scale);
        dto.setAddr1(addr1);
        dto.setBuyerCate(cate);
//		dto.setMngAgent((String)session.getAttribute("SS_MNGAGENT"));		// 지사코드

        resultList = frontService.getMyBusinessItemList(dto);


        ArrayList<FrontDto> orderMappingList = new ArrayList<>();
        ArrayList<Integer> orSeqs = new ArrayList<Integer>();
        if(resultList.size() >= 1) {
            for(int i = 0; i < resultList.size(); i++) {
                orSeqs.add(resultList.get(i).getOrderSeq());
            }
            dto.setOrSeqList(orSeqs);
            orderMappingList = frontService.getOrderMappingList(dto);
        }



        dto.setCategoryType("1");		// 규모
        recommendList01 = frontService.getRecommentList(dto);

        dto.setCategoryType("2");		// 지역
        recommendList02 = frontService.getRecommentList(dto);

        if(recommendList02.size() < 4) {
            dto.setAddr1(addr2);
            dto.setCategoryType("2");
            recommendList02 = frontService.getRecommentList(dto);
            if(recommendList02.size() < 4) {
                dto.setAddr1(addr3);
                dto.setCategoryType("2");
                recommendList02 = frontService.getRecommentList(dto);
            } if(recommendList02.size() < 4) {		//
                dto.setCategoryType("0");
                recommendList02 = frontService.getRecommentList(dto);
            }
        }

        dto.setCategoryType("3");		// 업종
        recommendList03 = frontService.getRecommentList(dto);
//		dto.setCategoryType("4");
//		recommendList04 = frontService.getRecommentList(dto);

		/*
		 * 배송쿠폰제
		 *
		FrontDto dtoCuopon = new FrontDto();
		ArrayList<FrontDto> dtoCuoponList = new ArrayList<FrontDto>();
		String couponStats = "";
		dtoCuopon = frontService.getUserCoupon(dto);
		dtoCuoponList = frontService.getUserCouponList(dto);
		if(dtoCuopon != null) {
			couponStats = frontService.getCouponStats(dto);
			logger.info("COUPONSAVE : " + dtoCuopon.getCouCnt());
			logger.info("COUPONSTATS : " + couponStats);
			mav.addObject("COUPONSAVE", dtoCuopon.getCouCnt());
			mav.addObject("COUPONSTATS", couponStats);
		} else {
			logger.info("COUPONSAVE : 0");
			logger.info("COUPONSTATS : 40");
			mav.addObject("COUPONSAVE", 0);
			mav.addObject("COUPONSTATS", 40);
			mav.addObject("dtoCuoponListSize", 0);
		}
		if(dtoCuoponList != null) {
			mav.addObject("dtoCuoponList", dtoCuoponList);
		}
		*/
        mav.addObject("resultList", resultList);
        mav.addObject("orderMappingList", orderMappingList);
        mav.addObject("recommendList01", recommendList01);
        mav.addObject("recommendList02", recommendList02);
        mav.addObject("recommendList03", recommendList03);
//		mav.addObject("recommendList04", recommendList04);

        return mav;
    }

    @RequestMapping(value = "myBusinessItemDetail", method = RequestMethod.POST)
    public ModelAndView myBusinessItemDetail(HttpSession session
            ,@RequestParam(value="orderSeq", defaultValue = "-") String orderSeq
    ) {
        logger.info("URL : myBusinessItemDetail _ajax");
        ArrayList<FrontDto> resultList = new ArrayList<FrontDto>();
        FrontDto dto = new FrontDto();
        dto.setOrderSeq(Integer.parseInt(orderSeq));

        resultList = frontService.getMyBusinessItemDetail(dto);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultList", resultList);

        return new ModelAndView(ajaxJsonView ,map);
    }

    @RequestMapping(value = "itemRateInsert", method = RequestMethod.POST)
    public ModelAndView itemRateInsert(HttpSession session
            ,@RequestParam(value="orderSeq", defaultValue = "-") String orderSeq
            ,@RequestParam(value="itemSeqList", defaultValue = "-") ArrayList<Object> itemSeqList
            ,@RequestParam(value="itemCommentList", defaultValue = "-") ArrayList<String> itemCommentList
            ,@RequestParam(value="itemStarList", defaultValue = "-") ArrayList<Object> itemStarList
										/*
										,@RequestParam(value="itemStarList2", defaultValue = "0") ArrayList<Object> itemStarList2
										,@RequestParam(value="itemStarList3", defaultValue = "0") ArrayList<Object> itemStarList3
										,@RequestParam(value="itemStarList4", defaultValue = "0") ArrayList<Object> itemStarList4
										,@RequestParam(value="itemStarList5", defaultValue = "0") ArrayList<Object> itemStarList5
										*/
    ) {
        logger.info("URL : itemRateInsert _ajax");
        FrontDto dto = new FrontDto();

        ArrayList<String> reItemCommentList = new ArrayList<String>();

        for(int i = 0; i < itemCommentList.size(); i++) {
            reItemCommentList.add(XssPreventer.escape(itemCommentList.get(i)));
        }

        dto.setOrderSeq(Integer.parseInt(orderSeq));
        dto.setItemSeqList(itemSeqList);
        dto.setItemCommentList(itemCommentList);
        dto.setItemStarList(itemStarList);
		/*
		dto.setItemStarList2(itemStarList2);
		dto.setItemStarList3(itemStarList3);
		dto.setItemStarList4(itemStarList4);
		dto.setItemStarList5(itemStarList5);
		*/
        frontService.itemRateInsert(dto);

        return new ModelAndView(ajaxJsonView );
    }

    @RequestMapping(value = "question", method = RequestMethod.GET)
    public ModelAndView frt_service_faq(HttpSession session, HttpServletRequest request) {
        logger.info("URL : question");
        ModelAndView mav = new ModelAndView("frt_service_faq");

        FrontDto stats = new FrontDto();
        stats.setStPage("question");
        frontService.statisticsUpdate(stats);

        return mav;
    }

    @RequestMapping(value = "bannerCookie", method = RequestMethod.POST)
    public ModelAndView bannerCookie(HttpSession session, HttpServletResponse response, HttpServletRequest request
            ,@RequestParam(value="pmSeq", defaultValue="-")String pmSeq) {
        logger.info("URL : bannerCookie _ajax");

        Cookie pmCookie= new Cookie("pmSeq_"+pmSeq, pmSeq);
        response.addCookie(pmCookie);

        return new ModelAndView(ajaxJsonView);
    }

    @Transactional
    @RequestMapping(value = "orderCancel", method = RequestMethod.POST)
    public ModelAndView orderCancel(HttpSession session, HttpServletRequest request
            ,@RequestParam(value="seq", defaultValue = "none") String seq
    ) {
        logger.info("URL : orderCancel _ajax");
        Map<String, Object> map = new HashMap<String, Object>();

        FrontDto dto = new FrontDto();

        dto.setSeq(seq);
        frontService.orderCancel(dto);
        frontService.getOrderCancelList(dto);

		/*
		 * 배송쿠폰제
		 *
		dto.setIndNo((String)session.getAttribute("SS_INDNO"));
		dto.setIndopenNo((String)session.getAttribute("SS_INDOPENNO"));
		dto.setInddisNo((String)session.getAttribute("SS_INDDISNO"));
		dto = frontService.getUserCoupon(dto);
		if(dto != null) {
			dto.setIndNo((String)session.getAttribute("SS_INDNO"));
			dto.setIndopenNo((String)session.getAttribute("SS_INDOPENNO"));
			dto.setInddisNo((String)session.getAttribute("SS_INDDISNO"));
			frontService.couponCancel(dto);
		}
		*/
        return new ModelAndView(ajaxJsonView ,map);
    }

    @RequestMapping(value = "itemCheck", method = RequestMethod.GET)
    public ModelAndView itemCheck(HttpSession session, HttpServletRequest request
            ,@RequestParam(value="cartList", defaultValue="-") ArrayList<Object> cartList
            ,@RequestParam(value="itemCountList", defaultValue="-") ArrayList<Object> itemCountList
            ,@RequestParam(value="business_code", defaultValue="-") String businessCode
            ,@RequestParam(value="business_open_code", defaultValue="-") String businessOpenCode
            ,@RequestParam(value="business_dis_code", defaultValue="-") String businessDisCode
            ,@RequestParam(value="firstDate", defaultValue="-") String firstDate
            ,@RequestParam(value="lastDate", defaultValue="-") String lastDate
    ) {
        logger.info("URL : itemCheck _ajax");
        Map<String, Object> map = new HashMap<String, Object>();
        FrontDto dto = new FrontDto();
        dto.setCartList(cartList);
        dto.setItemCountList(itemCountList);
        dto.setBusinessCode(businessCode);
        dto.setIndNo(businessCode);
        dto.setBusinessOpenCode(businessOpenCode);
        dto.setIndopenNo(businessOpenCode);
        dto.setBusinessDisCode(businessDisCode);
        dto.setInddisNo(businessDisCode);
        dto.setFirstDate(firstDate);
        dto.setLastDate(lastDate);

        logger.info(""+cartList.get(0)); // 시퀀스
        logger.info(""+itemCountList.get(0)); // 갯수
        logger.info("businessCode : " + businessCode);
        logger.info("firstDate : " + firstDate);
        logger.info("lastDate : " + lastDate);

        // 수량 제한을 향후 어떻게할껀지 확인 요망
        String auth = (String)session.getAttribute("SS_AUTHORITY");
        int itemCount = frontService.getTotalItemCount(dto); 		// 한달동안 신청한수량.
        int checkNum = frontService.getCheckItemNum(dto); 		// 유저 총 신청가능한 수량.
        int itemSum = 0; // 신청수량.
        String check = ""; /* 	0 : 신청불가 .
										1 : 신청가능.
										2 : 신청은 가능하나 총합이 넘은경우 */
        logger.info("이번달 신청한 수량 : itemCount : " + itemCount);
        if(auth != null) {
            if(auth.equals("ROLE_B") || auth.equals("ROLE_G")) {
                int limitOrder = frontService.getCustomerLimitOrder(dto);	// 공단or유관기관 총 신청가능한 수량.
                logger.info("G.B 신청가능 수량 : limitOrder : " + limitOrder);
                if(itemCount >= limitOrder) {
                    check = "0";
                    checkNum = limitOrder;
                } else {
                    for(int index = 0; index < itemCountList.size(); index++) {
                        itemSum += Integer.parseInt((String) itemCountList.get(index));
                        logger.info("itemCount : " + itemCount + "  itemSum : " + itemSum);
                    }
                    if((itemSum+itemCount) <= limitOrder) {
                        check = "1";
                    } else {
                        check = "2";
                        checkNum = limitOrder;
                    }
                }

            } else {	// ROLE_U
                logger.info("U 신청가능 수량 : checkNum : " + checkNum);
                if(itemCount >= checkNum) {
                    check = "0";
                } else {
                    for(int index = 0; index < itemCountList.size(); index++) {
                        itemSum += Integer.parseInt((String) itemCountList.get(index));
                        logger.info("itemCount : " + itemCount + "  itemSum : " + itemSum);
                    }
                    if((itemSum+itemCount) <= checkNum) {
                        check ="1";
                    } else {
                        check = "2";
                    }
                }
            }
            logger.info(checkNum + " - " + itemCount + " = " + (checkNum-itemCount));
        } else {
            check = "9";
        }

        map.put("check", check);
        map.put("itemCount" , itemCount);
        map.put("checkNum" , checkNum);
        map.put("itemSum" ,itemSum);

        return new ModelAndView(ajaxJsonView ,map);
    }

    @RequestMapping(value = "loginPage", method = RequestMethod.GET)
    public ModelAndView loginPage() {
        logger.info("URL : loginPage");
        ModelAndView mav = new ModelAndView("frt_login");
        return mav;
    }

    @RequestMapping(value = "ratingCheck", method = RequestMethod.POST)
    public ModelAndView ratingCheck(HttpSession session) {
        logger.info("URL : ratingCheck _ajax");
        FrontDto dto = new FrontDto();

        dto.setIndNo((String)session.getAttribute("SS_INDNO"));
        dto.setIndopenNo((String)session.getAttribute("SS_INDOPENNO"));
        dto.setInddisNo((String)session.getAttribute("SS_INDDISNO"));

        int retingChk = 0;

        retingChk = frontService.getBusinessRatingCheck(dto);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("retingChk", retingChk);

        return new ModelAndView(ajaxJsonView ,map);
    }

    // 고객 주문 제품명
    @RequestMapping(value = "myOrderItemName", method = RequestMethod.POST)
    public ModelAndView myOrderItemName(@RequestParam(value="orSeq", defaultValue = "0") int orSeq) {
        logger.info("URL : myOrderItemName _ajax");
        String itemName = frontService.getByerOrderItemName(orSeq);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("itemName", itemName);
        return new ModelAndView(ajaxJsonView ,map);
    }

    @RequestMapping(value = "koshaMediaApiSelectCreateJuly", method = RequestMethod.GET)
    public ModelAndView koshaJsonReturn(HttpSession session
            ,HttpServletRequest request
            ,@RequestParam(value="one_depth_search_val", defaultValue="-") String searchValue
            ,@RequestParam(value="one_depth_search_code", defaultValue="-") String searchCode
            ,@RequestParam(value="categoryCode", defaultValue="-") String categoryCode
    ) throws UnsupportedEncodingException {
        logger.info("URL : koshaJsonReturn");
        ModelAndView mav = new ModelAndView("kosha_json_return_api");

        return mav;
    }



    ////////////////////////////////  vr 설문지     ////////////////////////////////

    @RequestMapping(value = "/frtSurSelect" ,method = RequestMethod.GET)
    public ModelAndView frtSurSelect() {
        logger.info("URL : frtSurSelect _ajax");
        ArrayList<ContentsDto> resultList = new ArrayList<ContentsDto>();
        HashMap<String, Object> map = new HashMap<String, Object>();
        resultList = contentService.frtSurSelect();
        map.put("resultList", resultList);

        return new ModelAndView(ajaxJsonView, map);
    }

    @RequestMapping(value = "/frtMultiSelect", method = RequestMethod.GET)
    public ModelAndView frtMultiSelect(HttpSession session, @RequestParam(value="vagSqArray",defaultValue="0") ArrayList<Object> vagSqArray) {
        logger.info("URL : frtMultiSelect _ajax");
        ArrayList<ContentsDto> resultList = new ArrayList<ContentsDto>();
        ContentsDto dto = new ContentsDto();
        HashMap<String, Object> map = new HashMap<String, Object>();
        dto.setVagSqArray(vagSqArray);
        resultList = contentService.frtMultiSelect(dto);


		/*
		 * 배송쿠폰제
		 *
		 쿠폰적립 start
		FrontDto cpnInfo = new FrontDto();
		cpnInfo.setIndNm((String)session.getAttribute("SS_INDNM"));
		cpnInfo.setIndNo((String)session.getAttribute("SS_INDNO"));
		cpnInfo.setIndopenNo((String)session.getAttribute("SS_INDOPENNO"));
		cpnInfo.setInddisNo((String)session.getAttribute("SS_INDDISNO"));
		int orSeq = frontService.maxOrSeq();
		String couponStats = frontService.getCouponStats(cpnInfo);
		cpnInfo.setCouType("SU");			// 설문조사 코드
		cpnInfo.setOrSeq(orSeq);
		if(couponStats != null) {
			// 쿠폰사용
			if(couponStats.equals("10")) {
				cpnInfo.setBuyerAmount(Integer.toString(0));
//				cpnInfo.setCouStats("30");
				frontService.updateCoupon(cpnInfo);					// 30
				cpnInfo.setCouOnnoff("40");
				frontService.insertCouponUse(cpnInfo);				// 30
				frontService.updateCouponStats(cpnInfo);			// 40
				frontService.updateCouponSumUse(cpnInfo);
			} else {
			// 쿠폰미사용
				frontService.insertCoupon(cpnInfo);					// 10
				frontService.updateCouponSumSave(cpnInfo);
				FrontDto dtoCuopon = new FrontDto();
				dtoCuopon = frontService.getUserCoupon(cpnInfo);
				if(dtoCuopon.getCouCnt() > 2) {
					cpnInfo.setCouOnnoff("10");
					frontService.updateCouponStats(cpnInfo);		// 10
				}
			}
		} else {
			// 최초적립
			frontService.insertCoupon(cpnInfo);						// 10
			cpnInfo.setCouOnnoff("40");
			frontService.insertCouponStats(cpnInfo);					// 40
		}
		 쿠폰적립 end
		*/

        map.put("resultList", resultList);

        return new ModelAndView(ajaxJsonView, map);
    }

    @RequestMapping(value = "/frtSurInsert", method = RequestMethod.GET)
    public ModelAndView frtSurInsert (@RequestParam(value = "chkArray", defaultValue = "0") String chkArray
            ,@RequestParam(value = "sAInfo", defaultValue = "-") String sAInfo
            ,@RequestParam(value = "surSq", defaultValue = "0") int surSq
            ,HttpSession session
            ,HttpServletRequest request
    ) throws ParseException{
        logger.info("URL : frtSurInsert _ajax");
        ContentsDto dto = new ContentsDto();

        XssPreventer.escape(sAInfo);

        dto.setChkArray(chkArray.replaceAll("&quot;", "\""));
        dto.setsAInfo(sAInfo.replaceAll("&quot;", "\""));
        dto.setSurSq(surSq);
        contentService.frtSurInsert(dto);

        session = request.getSession(true);
        String surveyInfo = "surveyInfo";
        session.setAttribute("surveyInfo", surveyInfo);

        return new ModelAndView(ajaxJsonView);
    }

    @RequestMapping(value = "/surveyPass", method = RequestMethod.GET)
    public ModelAndView frtPass (HttpSession session
            ,HttpServletRequest request)  throws ParseException{
        logger.error("URL : /survey_pppp");

        session = request.getSession(true);
        String surveyInfo = "surveyInfo";
        session.setAttribute("surveyInfo", surveyInfo);

        return new ModelAndView(ajaxJsonView);
    }

    @RequestMapping(value = "coupon_guide", method = RequestMethod.GET)
    public ModelAndView frt_coupon_guide(HttpSession session, HttpServletRequest request) {
        logger.info("URL : coupon_guide");
        ModelAndView mav = new ModelAndView("frt_coupon_guide");
		/*
		FrontDto stats = new FrontDto();
		stats.setStPage("coupon_guide");
       	frontService.statisticsUpdate(stats);
		*/
        return mav;
    }

    @RequestMapping(value = "robots.txt", method = RequestMethod.GET)
    public ModelAndView frt_robotsText(HttpSession session, HttpServletRequest request) {
        logger.info("URL : frt_robotsText");
        ModelAndView mav = new ModelAndView("frt_robots_text");
        return mav;
    }




}//class



