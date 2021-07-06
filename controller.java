package com.soltworks.svz.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import com.soltworks.svz.dto.ContentsDto;
import com.soltworks.svz.service.ContentService;

@Controller
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ContentService contentService;

    @Resource
    private MappingJacksonJsonView ajaxJsonView;
    @Value(#{config['pagingCnt']})
    private int pagingCnt;

    @Value(#{config['SDS_REQUEST']})
    private String sdsRequest;

    @Value(#{config['SERVICE_URL']})
    private String SERVICE_URL;

    @RequestMapping(value=robots.txt)
    @ResponseBody
    public ModelAndView robotsText() {
        logger.info(URL  robotsText);
        ModelAndView mav = new ModelAndView(frt.robotsText);

        return mav;
    }

    @RequestMapping(value=igSearch)
    @ResponseBody
    public ModelAndView igSearchPage() {
        logger.info(URL  igSearch);
        ModelAndView mav = new ModelAndView(frt.igSearchPage);

        return mav;
    }

    @RequestMapping(value=igSearchsearchResult)
    public ModelAndView igSearchResult(HttpServletRequest request, @RequestParam(value=search_value, defaultValue=-)String searchValue) {
        logger.info(URL  igSearchresultPage);
        ModelAndView mav = new ModelAndView(frt.igSearchResult);
        mav.addObject(searchValue, searchValue);
        request.setAttribute(searchValue, searchValue);

        return mav;
    }

    @RequestMapping(value=igSearchsdsSearch, method = RequestMethod.GET)
    public ModelAndView searchReq(@RequestParam(value=search_value, defaultValue=-)String searchValue) throws IOException {
        logger.info(URL  igSearchsearchReq);
        HashMapString, Object map = new HashMap();
        현장배송 검색 시작
        StringBuffer sb = new StringBuffer();
        String sdsUrl = sdsRequest+searchResponsesearchRep;

        HttpPost post = new HttpPost(sdsUrl);
        ListNameValuePair urlParameters = new ArrayList();
        urlParameters.add(new BasicNameValuePair(searchValue, searchValue));
        post.setEntity(new UrlEncodedFormEntity(urlParameters,UTF-8));

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(post);
        sb.append(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
        response.close();
        httpClient.close();

        String sdsList = sb.toString().replaceAll(, ');
                현장배송 검색 끝


                VR전용관 검색 시작
                ArrayListContentsDto svzList = new ArrayListContentsDto();
        ContentsDto dto = new ContentsDto();

        dto.setSearchKeyword(searchValue);
        dto.setKeySearch(-);
        dto.setSearchType(igPage);
        svzList = contentService.contentSearchSelect(dto);
        VR전용관 검색 끝

        map.put(sdsList, sdsList);
        map.put(searchValue, searchValue);
        map.put(svzList, svzList);

        return new ModelAndView(ajaxJsonView, map);
        return mav;
    }

    @SuppressWarnings(unchecked)
    @RequestMapping(value = safetyAPIvrContList, method = RequestMethod.GET)
    public String safetyAPI(HttpServletRequest request
            ,@RequestParam(value = search_value, defaultValue = -) String searchValue
            ,@RequestParam(value = cate_sq, defaultValue = 0) int cateSq) {
        ArrayListContentsDto resultList = new ArrayListContentsDto();
        JSONArray jsonArray = new JSONArray();
        ContentsDto dto = new ContentsDto();

        dto.setSearchKeyword(searchValue);
        dto.setCateSq(cateSq);
        dto.setKeySearch(-);

        if(searchValue.equals(-)) {
            resultList = contentService.contentListSelect(dto);
        } else {
            resultList = contentService.contentSearchSelect(dto);
        }

        for(int index = 0; index  resultList.size(); index++) {
            ContentsDto result = resultList.get(index);
            JSONObject json = new JSONObject();
            json.put(contSq, result.getContSq());
            json.put(contTitle, result.getContTitle());
            json.put(likeCount, result.getLikeCount());
            json.put(cateSq, result.getCateSq());
            if(result.getContDesc() == null) {
                result.setContDesc(-);
            }
            json.put(contDesc, result.getContDesc());
            json.put(contThum, SERVICE_URL+ resourcescontents + result.getContThum());
            json.put(updateKind, result.getUpdateKind());
            json.put(contCnt, result.getContCnt());
            json.put(orderNum, result.getOrderNum());
            jsonArray.add(json);
        }

        String result = jsonArray.toJSONString().replaceAll(, );
        request.setAttribute(result, result);

        return safetyJsonvrContents;
    }

    @SuppressWarnings(unchecked)
    @RequestMapping(value = safetyAPIvrContListdetail, method = RequestMethod.GET)
    public String safetyAPIDetail(HttpServletRequest request
            ,@RequestParam(value = cont_sq, defaultValue = 0) int contSq) {
        JSONArray jsonArray = new JSONArray();
        ContentsDto dto = new ContentsDto();
        dto.setContSq(contSq);
        dto = contentService.contentDetailInfoSelect(dto);

        JSONObject json = new JSONObject();
        json.put(contSq, dto.getContSq());
        json.put(contTitle, dto.getContTitle());
        json.put(likeCount, dto.getLikeCount());
        json.put(createDate, dto.getCreateDate());
        json.put(contURL, dto.getContURL());
        if(dto.getContDesc() == null) {
            dto.setContDesc(-);
        }
        json.put(contDesc, dto.getContDesc());
        json.put(contThum, SERVICE_URL+ resourcescontents + dto.getContThum());
        json.put(updateKind, dto.getUpdateKind());
        json.put(contCnt, dto.getContCnt());
        json.put(keyWord, dto.getKeyWord());
        jsonArray.add(json);

        String result = jsonArray.toJSONString().replaceAll(, );
        request.setAttribute(result, result);
        return safetyJsonvrContents;
    }

}





