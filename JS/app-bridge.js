/**
 * Created by John on 16/6/3.
 */
/*global api devicetype*/
/*eslint no-unused-vars: ["error", { "varsIgnorePattern": "bridge" }]*/
var bridge = (function(window) {
    'use strict';
    //var devicetype = api.devicetype;//api.getQueryString('devicetype');
    if (typeof(api) === 'undefined') {
        console.error('warning,please load api.js first!');
        return false;
    }
    var pageBaseParam = {
        Url: '', //跳转链接
        IsTabBar: false, //是否是切换tab方式的跳转
        Content: {}, //参数
        TabBarIndex: 0, //tab序号
        ServicesInfo: {}, //IOS专有 
        ClassInfo: {}, //类名
        GA: api.getLocalStorage('GA'), //GA统计
        IsNav: true //是否显示导航栏
    };
    var bridge = {};
    /*
     * 打开页面模块
     */
    //回到首页
    bridge.openIndex = function() {
        var pageParam = deepClone(pageBaseParam);
        if (!api.isApp()) {
            window.location = 'https://demo.com';
        } else {
            if (devicetype === 'ios') {
                pageParam.ClassInfo = { 'ClassName': 'HomeVC', 'cocoaPodName': 'HomeModule', 'isXib': false };
                pageParam.IsTabBar = true;
            } else if (devicetype === 'android') {
                pageParam.ClassInfo = { 'ClassName': 'com.demo.app.activity.fragment.MainActivityFragment' };
                pageParam.IsTabBar = true;
            }
            api.invoke('open.page', encodeURI(JSON.stringify(pageParam)));
        }
    };
    /*
     *APP端打开一个链接
     *URl:打开的链接地址
     *isNav: 是否显示上面原生导航栏（可选参数，默认为显示导航栏），
     *isShare:是否显示分享按钮（可选参数, 0 不显示 其他显示）
     *贺彪 2016-08-31
     */
    bridge.openActivity = function(url, isShare) {
        var pageParam = deepClone(pageBaseParam);
        if (!api.isApp()) {} else {
            if (devicetype === 'ios') {
                let share = isShare || '1';
                if (share === '0') {
                    pageParam.Content = { 'isShare': '0' };
                }
                pageParam.Url = url;
                pageParam.IsNav = true;
            } else if (devicetype === 'android') {
                pageParam.Url = url;
                pageParam.IsNav = true;
            }
            api.invoke('open.page', encodeURI(JSON.stringify(pageParam)));
        }
    };
    /**
     * 打开会员等级
     * memlevel：级别
     */
    bridge.openMemberLevel = function(memlevel) {
        var pageParam = deepClone(pageBaseParam);
        var gaInfo = '->会员等级页';
        if (api.isApp()) {
            if (devicetype === 'ios') {
                pageParam.ClassInfo = { 'ClassName': 'MemberLevelVC', 'isStoryBoard': '0', storyBoardIdentifier: '', 'cocoaPodName': 'PersonCenterModule', 'isXib': false };
                pageParam.ServicesInfo = { 'serviceClass': 'LoginService', 'serviceName': '', serviceMethod: 'initWithService:' };
                pageParam.GA += gaInfo;
            } else if (devicetype === 'android') {
                var hybridversion = api.getQueryString('hybridversion');
                if (hybridversion >= 1) {
                    pageParam.ClassInfo = { 'ClassName': 'com.demo.app.mvp.module.login.MemberLevelActivity' };
                } else {
                    pageParam.ClassInfo = { 'ClassName': 'com.demo.app.activity.MemberLevelActivity' };
                }
                pageParam.Content = { 'userLevel': memlevel };
                pageParam.GA += gaInfo;
            }
            api.invoke('open.page', encodeURI(JSON.stringify(pageParam)));
        }
    };
    /*
     * 获取通讯录
     *
     * */
    bridge.contactList = function() {
        // var pageParam = deepClone(pageBaseParam);
        if (api.isApp()) {
            // pageParam = {};
            api.invoke('action.openAddress', '');
        }
    };
    /*
     *  调用组件模块
     */
    //添加到我的收藏
    bridge.addSubjectToFavorite = function() {
        if (api.isApp()) {
            api.invoke('action.collect', '');
        }
    };
    /**
     * 取消收藏
     * favoriteid:收藏Id
     */
    bridge.cancelSubjectFavorite = function(favoriteid) {
        if (api.isApp()) {
            var param = {
                favoriteid: favoriteid
            };
            api.invoke('action.cancelfavorite', encodeURI(JSON.stringify(param)));
        }
    };
    /* 分享
     * title:标题
     * content:简介
     * imageUrl:图片Url
     * pageUrl:页面Url
     * */
    bridge.shareSubject = function(title, content, imageUrl, pageUrl) {
        var div = document.createElement('div');
        div.innerHTML = title;
        title = div.innerText || div.textContent;
        div.innerHTML = content;
        content = div.innerText || div.textContent;
        div.setAttribute('style', 'display:none;');
        div.setAttribute('height', '0px');
        div.setAttribute('width', '0px');
        div.setAttribute('frameborder', '0');
        document.body.appendChild(div);
        div.parentNode.removeChild(div);
        var param = {
            Title: title,
            Content: content,
            ImageUrl: imageUrl,
            Url: pageUrl,
            GACategory: ''
        };
        div = null;
        if (api.isApp()) {
            api.invoke('action.share', encodeURI(JSON.stringify(param)));
        }
    };
    /**
     * 复制
     * code: 内容
     */
    bridge.copy = function(code) {
        var copyParam = {
            Content: code
        };
        if (api.isApp()) {
            api.invoke('action.copy', encodeURI(JSON.stringify(copyParam)));
        }
    };
    /**
     * 保存图片
     * strurl:图片路径
     */
    bridge.saveAlbum = function(strurl) {
        var copyParam = {
            Content: strurl
        };
        if (api.isApp()) {
            api.invoke('action.saveAlbum', encodeURI(JSON.stringify(copyParam)));
        }
    };
    /*
     * 返回页面模块
     * url： 返回地址
     * islastpage： 返回上一页
     * isrootpage： 返回一级页面
     * calltype： 跳转类型1返回，0默认返回
     */
    bridge.goBack = function(url, islastpage, isrootpage, calltype) {
        if (calltype == 1) {
            var backParam = {
                ClassInfo: {},
                GA: api.getLocalStorage('GA')
            };
            if (api.isApp()) {
                if (devicetype == 'ios') {
                    backParam.ClassInfo = { 'ClassName': '', 'isLastPage': islastpage, 'isRootPage': isrootpage };
                } else if (devicetype == 'android') {
                    backParam.ClassInfo = { 'ClassName': '', 'isLastPage': islastpage, 'isRootPage': isrootpage };
                }
                api.invoke('go.back', encodeURI(JSON.stringify(backParam)));
            } else {
                window.location.href = url;
            }
        } else {
            window.location.href = url;
        }
    };
    /**
     *  H5 打开App直播功能
     *  defaultUrl : 默认的跳转url
     *  videoId : 视频ID
     */
    bridge.openLive = function(defaultUrl, videoId) {
        var param = {};
        if (!api.isApp()) {
            window.location = defaultUrl;
        } else {
            if (devicetype === 'ios') {
                param.videoId = videoId;
            } else if (devicetype === 'android') {
                param.videoId = videoId;
            }
            api.invoke('open.live', encodeURI(JSON.stringify(param)));
        }
    };
    //app产品详情页打开浏览历史
    bridge.openHistory = function() {
        if (api.isApp()) {
            api.invoke('action.openHistory', '');
        }
    };
    return bridge;
})(window, document);
var deepClone = function(obj) {
    var str, newobj = obj.constructor === Array ? [] : {};
    if (typeof obj !== 'object') {
        return;
    } else if (window.JSON) {
        str = JSON.stringify(obj), //系列化对象
            newobj = JSON.parse(str); //还原
    } else {
        for (var i in obj) {
            newobj[i] = typeof obj[i] === 'object' ? deepClone(obj[i]) : obj[i];
        }
    }
    return newobj;
};