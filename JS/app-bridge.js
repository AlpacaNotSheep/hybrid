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
            window.location = 'https://m.demo.com';
        } else {
            if (devicetype === 'ios') {
                pageParam.ClassInfo = { 'ClassName': 'UZHomeVC', 'cocoaPodName': 'UZHomeModule', 'isXib': false };
                pageParam.IsTabBar = true;
            } else if (devicetype === 'android') {
                pageParam.ClassInfo = { 'ClassName': 'com.demo.app.activity.fragment.MainActivityFragment' };
                pageParam.IsTabBar = true;
            }
            api.invoke('open.page', encodeURI(JSON.stringify(pageParam)));
        }
    };
    ///跳转到APP的用户中心
    bridge.openUser = function() {
        var pageParam = deepClone(pageBaseParam);
        if (!api.isApp()) {
            window.location = 'https://mhome.demo.com/Member/index.html';
        } else {
            if (devicetype === 'ios') {
                pageParam.ClassInfo = { 'ClassName': 'UZHomeVC', 'cocoaPodName': 'UZHomeModule', 'isXib': false };
                pageParam.TabBarIndex = 4;
                pageParam.IsTabBar = true;
            } else if (devicetype === 'android') {
                var appversion = api.getCookie('appversion');
                if (typeof(appversion) !== 'undefined' && appversion !== null && appversion >= '6.0.0') {
                    pageParam.ClassInfo = { 'ClassName': 'com.demo.app.mvp.module.home.mydemo.fragment.MydemoProFragment.class' };
                } else {
                    pageParam.ClassInfo = { 'ClassName': 'com.demo.app.activity.fragment.MainActivityFragment' };
                }
                pageParam.TabBarIndex = 4;
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
                pageParam.ClassInfo = { 'ClassName': 'UZMemberLevelVC', 'isStoryBoard': '0', storyBoardIdentifier: '', 'cocoaPodName': 'UZPersonCenterModule', 'isXib': false };
                pageParam.ServicesInfo = { 'serviceClass': 'UZLoginService', 'serviceName': '', serviceMethod: 'initWithService:' };
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
    //获取通讯录
    /*
     * 王超 2016-08-19
     *
     * */
    bridge.contactList = function() {
        // var pageParam = deepClone(pageBaseParam);
        if (api.isApp()) {
            // pageParam = {};
            api.invoke('action.openAddress', '');
        }
    };
    //产品详情
    /*
     * travelType 产品类型  1,2,3：跟团
     * productId  产品Id
     * travelType 对应数据库表demoTravelClassID
     * isHistoryPage 跳转过来的页面是不是历史记录页，默认不传这个值  为undefined
     *
     * */
    bridge.openProduct = function(travelType, productId, productType, isHistoryPage, productlink) {
        var pageParam = deepClone(pageBaseParam);
        if (typeof(isHistoryPage) === 'undefined') //如果不是浏览历史传过来的，则记录当前页面的链接
        {
            api.setCookie('backUrl', window.location.href); //key已从backurl更为backUrl，请勿覆盖！！！
        }
        var lineType = 0; //默认原来的跟团游标示，lineType 0原来跟团游 10新的跟团 20自由行 30 通用单项
        var travelTypeName = '跟团游'; //类型名称
        if (productType !== null && productType !== '' && typeof(productType) !== 'undefined') {
            if (travelType.toString() === '15' || travelType.toString() === '16' || travelType.toString() === '29' || travelType.toString() === '10') {
                lineType = 20;
                travelTypeName = '自由行';
            } else if ((travelType.toString() === '1' || travelType.toString() === '2' || travelType.toString() === '3') && (productType.toString() === '1' || productType.toString() === '2' || productType.toString() === '14')) {
                //新改版的跟团游标示
                lineType = 10;
                travelTypeName = '跟团游';
            } else if (productType.toString() === '17' || productType.toString() === '18' || productType.toString() === '19' || productType.toString() === '20' || productType.toString() === '21' || productType.toString() === '22' || productType.toString() === '23') {
                lineType = 30;
                travelTypeName = '通用单项';
            }
        }
        //GA信息
        var gaInfo = '->' + travelTypeName + '产品页';
        if (!api.isApp()) {
            //跟团url：https://m.demo.com/waptour-129959.html
            //自由行url：https://m.demo.com/trip/wap/129959.html
            var baseUrl = 'https://m.demo.com/';
            //默认链接到原来的跟团产品链接
            var endUrl = 'waptour-' + productId + '.html';
            //判断 source 判断app版本5.5.0 新旧
            var source = api.getQueryString('source');
            //不包含是m站，包含是旧版本app
            //判断是否有来源 如果没有来源取链接后面的文件夹名称
            var productlink_url;
            if (productlink) {
                productlink_url = productlink;
            } else {
                var productlink_ = window.location.pathname.split('/');
                productlink_url = productlink_[1];
                if (productlink_[1] === 'hybrid') {
                    productlink_url = productlink_[2];
                }
            }
            if (!source) {
                if (lineType === 20) {
                    //自由行产品详情链接
                    endUrl = 'trip/wap/' + productId + '.html';
                } else if (lineType === 10) {
                    //跟团游 链接新改版的产品详情页
                    endUrl = 'product/detail.html?productid=' + productId;
                } else if (lineType === 30) {
                    //通用单项项目
                    endUrl = 'singleproduct/detail.html?productid=' + productId;
                }
                if (endUrl.indexOf('?') > 0) {
                    window.location = baseUrl + endUrl + '&subject_focus_' + productlink_url;
                } else {
                    window.location = baseUrl + endUrl + '?subject_focus_' + productlink_url;
                }
            } else {
                if (lineType === 20) {
                    //自由行产品详情链接
                    endUrl = 'trip/wap/' + productId + '.html';
                    endUrl += '?/AppDetail/' + productId + '/' + productType;
                } else if (lineType === 10) {
                    //跟团游 链接新改版的产品详情页
                    endUrl += '?/AppDetail/' + productId + '/' + productType;
                } else {
                    endUrl += '?/AppDetail/' + productId + '/' + productType;
                }
                if (endUrl.indexOf('?') > 0) {
                    window.location = baseUrl + endUrl + '&subject_focus_' + productlink_url;
                } else {
                    window.location = baseUrl + endUrl + '?subject_focus_' + productlink_url;
                }
            }
        } else {
            var hybridversion = api.getQueryString('hybridversion');
            if (devicetype === 'ios') {
                if (hybridversion === '3') {
                    //550 版本 － 打开原生跟团产品详情
                    if (lineType === 10) {
                        pageParam.ClassInfo = { 'ClassName': 'UZProductDetailVC', 'cocoaPodName': 'UZProductDetailModule', 'isXib': false };
                        // pageParam.ServicesInfo = { 'serviceClass': 'UZProductService', 'serviceName': 'service', serviceMethod: '' };, 'isStoryBoard': '0'
                        pageParam.Content = { 'productId': productId };
                        pageParam.GA += gaInfo;
                    } else {
                        //550 版本 － 打开原生自由行产品详情
                        pageParam.ClassInfo = { 'ClassName': 'UZProductInfoVC', 'isStoryBoard': '1', 'storyBoardIdentifier': 'ProductInfoVC', 'cocoaPodName': 'UZProductDetailModule', 'isXib': false };
                        pageParam.Content = { 'productId': productId, 'demoProductClassId': travelTypeName };
                        pageParam.ServicesInfo = { 'serviceClass': 'UZProductService', 'serviceName': 'service', serviceMethod: '' };
                        pageParam.GA += gaInfo;
                    }
                } else {
                    // 550 之前的产品详情页
                    pageParam.ClassInfo = { 'ClassName': 'UZProductInfoVC', 'isStoryBoard': '1', 'storyBoardIdentifier': 'ProductInfoVC', 'cocoaPodName': 'UZProductDetailModule', 'isXib': false };
                    pageParam.Content = { 'productId': productId, 'demoProductClassId': travelTypeName };
                    pageParam.ServicesInfo = { 'serviceClass': 'UZProductService', 'serviceName': 'service', serviceMethod: '' };
                    pageParam.GA += gaInfo;
                }
            } else if (devicetype === 'android') {
                //跟团
                if (lineType === 10) {
                    //app 新架构 5.4.8
                    if (hybridversion === '1') {
                        pageParam.ClassInfo = { 'ClassName': 'com.demo.app.mvp.module.product.activity.ProductDetailUi540' };
                    }
                    //app 新架构 5.5.0
                    else if (hybridversion === '3') {
                        pageParam.ClassInfo = { 'ClassName': 'com.demo.app.mvp.module.hybrid.activity.ProductDetail548Activity' };
                    }
                    //5.4.8之前版本
                    else {
                        pageParam.ClassInfo = { 'ClassName': 'com.demo.app.activity.ProductDetailUi540' };
                    }
                    pageParam.Content = { 'ProductID': productId, 'demoTravelClass': travelTypeName };
                    pageParam.GA += gaInfo;
                }
                //非跟团
                else {
                    //app 新架构 5.4.8
                    if (hybridversion === '1') {
                        pageParam.ClassInfo = { 'ClassName': 'com.demo.app.mvp.module.product.activity.ProductDetailUi540' };
                    }
                    //app 新架构 5.5.0
                    else if (hybridversion === '3') {
                        pageParam.ClassInfo = { 'ClassName': 'com.demo.app.mvp.module.product.activity.ProductDetailUi540' };
                    }
                    //5.4.8之前版本
                    else {
                        pageParam.ClassInfo = { 'ClassName': 'com.demo.app.activity.ProductDetailUi540' };
                    }
                    pageParam.Content = { 'ProductID': productId, 'demoTravelClass': travelTypeName };
                    pageParam.GA += gaInfo;
                }
            }
            api.invoke('open.page', encodeURI(JSON.stringify(pageParam)));
        }
    };
    /**
     * 私人订制
     * udingzhitype：定制类型    0是优定制，1是U圈定制
     */
    bridge.openDingzhi = function(udingzhitype) {
        var pageParam = deepClone(pageBaseParam);
        var gaInfo = '->私人定制';
        if (api.isApp()) {
            if (devicetype === 'ios') {
                pageParam.ClassInfo = { 'ClassName': 'UZDingzhiYouVC', 'isStoryBoard': '0', 'storyBoardIdentifier': '', 'cocoaPodName': 'UZDingZhiYouModule', 'isXib': false };
                pageParam.ServicesInfo = { 'serviceClass': 'UZHomeService', 'serviceName': 'service' };
                pageParam.Content = { 'webUrl': 'https://mdingzhi.demo.com/hybrid/order/index.html?udingzhitype=' + udingzhitype };
                pageParam.GA += gaInfo;
            } else if (devicetype === 'android') {
                pageParam.IsNav = false;
                pageParam.Url = 'https://mdingzhi.demo.com/hybrid/order/index.html?udingzhitype=' + udingzhitype;
                pageParam.GA += gaInfo;
            }
            api.invoke('open.page', encodeURI(JSON.stringify(pageParam)));
        } else {
            window.location.href = 'https://mdingzhi.demo.com/order/index.html?udingzhitype=' + udingzhitype;
        }
    };
    //打开我的收藏
    bridge.openMyFavorites = function() {
        var pageParam = deepClone(pageBaseParam);
        var userId = api.getUserId();
        if (userId != null && typeof(userId) != 'undefined' && userId !== '' && userId != '0') {
            var gaInfo = '->跟团游产品详情页';
            if (api.isApp()) {
                if (devicetype === 'ios') {
                    pageParam.ClassInfo = { 'ClassName': 'UZMyAttentionVC', 'isStoryBoard': '0', storyBoardIdentifier: '', 'cocoaPodName': 'UZPersonCenterModule', 'isXib': false };
                    pageParam.ServicesInfo = { 'serviceClass': 'UZMyHomeService', 'serviceName': 'service', 'serviceMethod': '' };
                    pageParam.GA += gaInfo;
                } else if (devicetype === 'android') {
                    pageParam.ClassInfo = { 'ClassName': 'com.demo.app.mvp.module.home.mydemo.activity.MyCollectionsActivity' };
                }
                api.invoke('open.page', encodeURI(JSON.stringify(pageParam)));
            }
        } else {
            api.login(window.location.href, window.location.href);
        }
    };
    //打开我的订单
    bridge.openMyOrderList = function() {
        var pageParam = deepClone(pageBaseParam);
        var gaInfo = '->我的订单';
        if (api.isApp()) {
            var userId = api.getUserId();
            if (userId != null && typeof(userId) != 'undefined' && userId != '' && userId != '0') {
                if (devicetype === 'ios') {
                    pageParam.ClassInfo = { 'ClassName': 'UZOrdersPageVC', 'isStoryBoard': '0', storyBoardIdentifier: '', 'cocoaPodName': 'UZOrdersPageModule', 'isXib': false };
                    pageParam.ServicesInfo = { 'serviceClass': 'UZMyHomeService', 'serviceName': 'service', 'serviceMethod': '' };
                    //pageParam.Content = { 'orderState': 1 };
                    pageParam.GA += gaInfo;
                } else if (devicetype === 'android') {
                    pageParam.ClassInfo = { 'ClassName': 'com.demo.app.mvp.module.order.activity.OrderListNewActivity' };
                }
                api.invoke('open.page', encodeURI(JSON.stringify(pageParam)));
            } else {
                api.login(window.location.href, window.location.href);
            }
        } else {
            window.location.href = 'https://u.demo.com/mobile/order';
        }
    };
    //搜索结果
    /* 
     *city :出发城市
     *keyword:搜索关键字
     *traveclass：产品品类( 0全部 1跟团 3自由行 6邮轮）
     *preferential:是否优惠 1选中
     *play：线路玩法 (多个,分割)
     *price:价格区间 (最低价-最高价)
     *scenic:途径景点(多个,分割)
     *day:行程天数(多个,分割)
     *date:出发日期 (多个,分割)
     *pageindex：当前页数
     *sort:排序规则 （0默认 1销量降序 2价格升序 3价格降序）
     *destination：航线目的地（品类为邮轮 才会存在 多个,分割）
     *company：邮轮公司（品类为邮轮 才会存在 多个,分割）
     *cruises：出发港口（品类为邮轮 才会存在 多个,分割）
     * */
    bridge.openSearch = function(keyword, traveclass, day, price, city, preferential, play, appPriceId, appDayId, scenic, date, pageindex, sort, destination, company, cruises) {
        var params = '';
        if (!api.isApp()) {
            if (city !== '' && city !== null && typeof(city) !== 'undefined' && city !== 0 && city !== '0') {
                params += 'city=' + city;
            } else {
                params += 'city=2';
            }
            if (keyword !== '' && keyword !== null && typeof(keyword) !== 'undefined' && keyword !== 0 && keyword !== '0') {
                params += '&keyword=' + keyword;
            }
            if (traveclass !== '' && traveclass !== null && typeof(traveclass) !== 'undefined' && traveclass !== 0 && traveclass !== '0') {
                params += '&traveclass=' + traveclass;
            }
            if (preferential !== '' && preferential !== null && typeof(preferential) !== 'undefined' && preferential !== 0 && preferential !== '0') {
                params += '&preferential=' + preferential;
            }
            if (play !== '' && play !== null && typeof(play) !== 'undefined' && play !== 0 && play !== '0') {
                params += '&play=' + play;
            }
            if (price !== '' && price !== null && typeof(price) !== 'undefined' && price !== 0 && price !== '0') {
                params += '&price=' + price;
            }
            if (scenic !== '' && scenic !== null && typeof(scenic) !== 'undefined' && scenic !== 0 && scenic !== '0') {
                params += '&scenic=' + scenic;
            }
            if (day !== '' && day !== null && typeof(day) !== 'undefined' && day !== 0 && day !== '0') {
                params += '&day=' + day;
            }
            if (date !== '' && date !== null && typeof(date) !== 'undefined' && date !== 0 && date !== '0') {
                params += '&date=' + date;
            }
            if (pageindex !== '' && pageindex !== null && typeof(pageindex) !== 'undefined' && pageindex !== 0 && pageindex !== '0') {
                params += '&pageindex=' + pageindex;
            }
            if (sort !== '' && sort !== null && typeof(sort) !== 'undefined' && sort !== 0 && sort !== '0') {
                params += '&sort=' + sort;
            }
            if (destination !== '' && destination !== null && typeof(destination) !== 'undefined' && destination !== 0 && destination !== '0') {
                params += '&destination=' + destination;
            }
            if (company !== '' && company !== null && typeof(company) !== 'undefined' && company !== 0 && company !== '0') {
                params += '&company=' + company;
            }
            if (cruises !== '' && cruises !== null && typeof(cruises) !== 'undefined' && cruises !== 0 && cruises !== '0') {
                params += '&cruises=' + cruises;
            }
            var url = 'https://m.demo.com/search/list?' + params;
            window.location.href = url;
        } else {
            var appversion = api.getCookie('appversion');
            if (typeof(appversion) !== 'undefined' && appversion !== null && appversion > '5.5.4') {
                var param = {
                    UserID: api.getUserId(),
                    city: city,
                    keyword: keyword,
                    traveclass: traveclass,
                    preferential: preferential,
                    play: play,
                    price: price,
                    scenic: scenic,
                    day: day,
                    date: date,
                    pageindex: pageindex,
                    sort: sort,
                    destination: destination,
                    company: company,
                    cruises: cruises
                };
                api.invoke('open.search', encodeURI(JSON.stringify(param)));
            } else {
                if (devicetype == 'ios') {
                    var paramIos = {
                        UserID: api.getUserId(),
                        SearchContent: keyword,
                        Keyword: '',
                        GoDate: '',
                        Days: appDayId,
                        Price: appPriceId,
                        Diamond: '',
                        Count: 15,
                        ProductType: '',
                        TravelClassID: 0,
                        StartIndex: 1,
                        OrderBy: '3'
                    };
                    api.invoke('open.search', encodeURI(JSON.stringify(paramIos)));
                } else if (devicetype == 'android') {
                    switch (appPriceId) {
                        case 0:
                            price = '';
                            break;
                        case 1:
                            price = '1-500';
                            break;
                        case 2:
                            price = '501-1000';
                            break;
                        case 3:
                            price = '1001-3000';
                            break;
                        case 4:
                            price = '3001-5000';
                            break;
                        case 5:
                            price = '5001-8000';
                            break;
                        case 6:
                            price = '8001-10000';
                            break;
                        case 7:
                            price = '10001-?';
                            break;
                        default:
                            price = '';
                            break;
                    }
                    if (appDayId === 4) {
                        day = '9-11';
                    } else if (appDayId === 11) {
                        day = '11-?';
                    } else if (appDayId === 0) {
                        day = '';
                    }
                    var paramAndroid = {
                        userID: api.getUserId(),
                        searchContent: keyword,
                        keyword: '',
                        goDate: '',
                        days: day,
                        price: price,
                        diamond: '',
                        count: 15,
                        productType: '',
                        travelClassID: 0,
                        startIndex: 1,
                        orderBy: '3'
                    };
                    api.invoke('open.search', encodeURI(JSON.stringify(paramAndroid)));
                }
            }
        }
    };
    /*
     * 打开订单详情页
     * orderID:订单ID
     * orderType:订单类型   0是所有订单
     */
    bridge.openOrderDetail = function(orderID, orderType) {
        if (devicetype === 'ios') {
            var pageParam = {
                orderID: orderID,
                orderType: orderType
            };
            api.invoke('open.orderdetail', encodeURI(JSON.stringify(pageParam)));
        } else if (devicetype === 'android') {
            var pageParam = deepClone(pageBaseParam);
            pageParam.ClassInfo = { 'ClassName': 'com.demo.app.activity.OrderDetailNewActivity' };
            pageParam.Content = {
                orderID: orderID,
                orderType: orderType
            };
            api.invoke('open.page', encodeURI(JSON.stringify(pageParam)));
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
     * 复制会员俱乐部订单快递号
     * code: 订单快递号
     */
    bridge.copyExpress = function(code) {
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
     * 订单支付，打开支付界面
     * @param    options.payType 支付方式编号：
     *           "KQ": 快钱手机支付
     *           "KQKJ": 快钱信用卡快捷支付
     *           "AliPay": 支付宝手机支付
     *           "UnionPay": 银联手机支付
     *           "WeiXin": 微信支付
     *           "DaiJinKa": 代金卡
     *           "CMBC": 招商银行
     *           "CCB": 建设银行手机支付
     *
     * @param    options.orderCode 订单号
     * @param    options.orderId 订单Id
     * @param    options.godate  出团日期
     * @param    options.nums    订单人数
     * @param    options.nums    订单人数
     * @param    options.pname   产品名称
     * @param    options.prepayment   订单应付金额
     */
    bridge.invokeBankPay = function(options) {
        if (api.isApp()) {
            var payParam = {
                'payType': options.payType,
                'IsSSL': 0,
                'IsUtour': 0,
                'godate': options.godate,
                'nums': options.nums,
                'ordercode': options.orderCode,
                'orderid': options.orderId,
                'pname': options.productName,
                'prepayment': options.prepayment
            };
            api.invoke('action.pay', encodeURI(JSON.stringify(payParam)));
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
    //产品详情页的返回功能
    /*
     *url要返回到的url
     *isrootpage 返回一级页面
     *islastpage 返回上一页
     */
    bridge.goBackForProductdetail = function(url, islastpage, isrootpage) {
        var backParam = {
            ClassInfo: {},
            GA: api.getLocalStorage('GA')
        };
        if (api.isApp()) {
            //手机端返回
            if (devicetype == 'ios') {
                backParam.ClassInfo = { 'ClassName': '', 'isLastPage': islastpage, 'isRootPage': isrootpage };
            } else if (devicetype == 'android') {
                backParam.ClassInfo = { 'ClassName': '', 'isLastPage': islastpage, 'isRootPage': isrootpage };
            }
            api.invoke('go.back', encodeURI(JSON.stringify(backParam)));
        } else {
            //M站返回
            if (typeof(url) !== 'undefined' && url !== 'undefined' && url.length > 0) {
                //2016-09-13 添加转码跳转 
                try {
                    url = decodeURIComponent(url);
                    url = url.replace(/\+/g, '%2b');
                } catch (e) {
                    console.log(e.message);
                } finally {
                    window.location.href = url;
                }
            } else {
                window.location.href = 'https://m.demo.com';
            }
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
    /*
     *支付页返回订单详情页
     * orderCode:订单编码
     * orderID:订单ID
     * isSonOrder:是否为子订单
     */
    bridge.openPayDetail = function(orderID, orderType, isSonOrder) {
        // var gaInfo = '->订单详情';
        if (api.isApp()) {
            var payDetailParam = {
                orderID: orderID,
                orderType: orderType,
                isSonOrder: isSonOrder
            };
            if (devicetype === 'ios') {
                api.invoke('go.payback', encodeURI(JSON.stringify(payDetailParam)));
            } else if (devicetype === 'android') {
                api.invoke('go.payback', encodeURI(JSON.stringify(payDetailParam)));
            }
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