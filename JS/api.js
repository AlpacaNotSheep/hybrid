var api = (function(window, document) {
    'use strict';
    try {
        document.domain = 'demo.com';
    } catch (e) {
        console.log(e.message);
    }
    var version = '1.0.3',
        devicetype = '',
        postTimes = 0,
        iosGetTimes = 0,
        apiUrlPre = 'MobileCommon/RequestWebApi/544',
        path = {
            demoApi: 'http://demo.com/api/'
        },
        loginConfig = {
            from: document.referrer,
            forword: location.href
        },
        backUrl = 'javascript:history.go(-1)',
        serverVirtualDir = '',
        mvcVersion = '',
        api = {},
        ios = {},
        weixin = navigator.userAgent.toLowerCase().match(/micromessenger/i) == 'micromessenger',
        android = {},
        api = {
            version: version,
            path: path,
            backUrl: backUrl,
            serverVirtualDir: serverVirtualDir,
            mvcVersion: mvcVersion,
            __callback__: [],
            __iosCallback__: [],
            __asyncLoadingCode__: '0',
            loginConfig: loginConfig,
            __scrolltop__: 0,
            init: function() {
                //默认开启异步加载检测代码
                function asyncLoadScripts() {
                    let $array = ['demo.js'],
                        $hasScripts = document.getElementsByTagName('script'),
                        $frgment = document.createDocumentFragment()
                    _arrayHasScripts = [];
                    for (let a = 0; a < $hasScripts.length; a++) {
                        _arrayHasScripts = _arrayHasScripts.concat($hasScripts[a].getAttribute('src'));
                    }
                    _arrayHasScripts = _arrayHasScripts.toString();
                    for (let a = 0; a < $array.length; a++) {
                        if (_arrayHasScripts.indexOf($array[a]) < 0) {
                            let _scripts = document.createElement('script');
                            _scripts.setAttribute('async', 'async');
                            _scripts.setAttribute('src', 'https://' + $array[a]);
                            $frgment.appendChild(_scripts);
                        }
                    }
                    document.body.appendChild($frgment);
                }
                //默认开启异步加载检测代码
                asyncLoadScripts();
                return this;
            },
            //获取网址get方式的参数
            //key  关键字
            //func 解码方式
            //caseSensitive 大小写敏感，1为敏感
            getQueryString: function(key, func, caseSensitive) {
                if (!key || typeof(key) === 'undefined') {
                    key = '';
                    return null;
                } else {
                    key = key.toLowerCase();
                    var r,
                        reg = new RegExp('(^|&)' + key + '=([^&]*)(&|$)');
                    if (caseSensitive == '1') {
                        r = window.location.search.substr(1).match(reg);
                    } else {
                        r = window.location.search.toLowerCase().substr(1).match(reg);
                    }
                    if (r !== null) {
                        if (!func || func === '' || func == 'decodeURI' || typeof(func) == 'undefined') {
                            return decodeURI(r[2]);
                        } else if (func == 'unescape') {
                            return unescape(r[2]);
                        } else if (func == 'decodeURIComponent') {
                            return decodeURIComponent(r[2]);
                        }
                    } else {
                        return null;
                    }
                }
            },
            getValue: function(key, str, func) {
                if (!str || typeof(str) === 'undefined') {
                    return str;
                } else {
                    var reg = new RegExp('(^|&)' + key + '=([^&]*)(&|$)');
                    var r = str.substr(0).match(reg);
                    if (r !== null) {
                        if (!func || func === '' || func == 'decodeURI' || typeof(func) == 'undefined') {
                            return decodeURI(r[2]);
                        } else if (func == 'unescape') {
                            return unescape(r[2]);
                        } else if (func == 'decodeURIComponent') {
                            return decodeURIComponent(r[2]);
                        }
                    } else {
                        return null;
                    }
                }
            },
            //获取cookie
            getCookie: function(key, func) {
                if (devicetype == 'ios') {
                    return ios.getCookie(key);
                } else if (devicetype == 'android') {
                    return android.getCookie(key);
                } else {
                    var bikky = document.cookie;
                    key += '=';
                    var i = 0;
                    while (i < bikky.length) {
                        var offset = i + key.length;
                        if (bikky.substring(i, offset) == key) {
                            var endstr = bikky.indexOf(';', offset);
                            if (endstr == -1) {
                                endstr = bikky.length;
                            }
                            var r = bikky.substring(offset, endstr);
                            if (r !== null) {
                                if (!func || func === '' || func == 'decodeURI' || typeof(func) == 'undefined') {
                                    return decodeURI(r);
                                } else if (func == 'unescape') {
                                    return unescape(r);
                                } else if (func == 'decodeURIComponent') {
                                    return decodeURIComponent(r);
                                }
                            } else {
                                return null;
                            }
                        }
                        i = bikky.indexOf(' ', i) + 1;
                        if (i === 0) {
                            return;
                        }
                    }
                    return null;
                }
            },
            //写入cookie
            //timeout秒 600秒=10分钟*60
            setCookie: function(key, value, timeout) {
                if (!timeout || typeof(timeout) == 'undefined') {
                    timeout = 0;
                } else {
                    timeout = parseInt(timeout, 10);
                }
                if (devicetype == 'ios') {
                    ios.setCookie(key, value, timeout);
                } else if (devicetype == 'android') {
                    android.setCookie(key, value, timeout);
                } else {
                    if (timeout === 0) {
                        document.cookie = key + '=' + value + ';domain=.demo.com;path=/';
                    } else {
                        var exp = new Date();
                        exp.setTime(exp.getTime() + timeout * 1000);
                        document.cookie = key + '=' + value + ';expires=' + exp.toGMTString() + ';domain=.demo.com;path=/';
                    }
                }
            },
            //移除cookie
            removeCookie: function(key) {
                if (devicetype == 'ios') {
                    ios.removeCookie(key);
                } else if (devicetype == 'android') {
                    android.removeCookie(key);
                } else {
                    var exp = new Date();
                    exp.setTime(exp.getTime() - 3600 * 24);
                    var value = '';
                    document.cookie = key + '=' + value + ';expires=' + exp.toGMTString() + ';domain=.demo.com;path=/';
                }
            },
            //存入sessionStorage
            setSessionStorage: function(key, value) {
                try {
                    sessionStorage.removeItem(key);
                    sessionStorage.setItem(key, value);
                    return 1;
                } catch (_) {
                    return 0;
                    //alert('该功能无法使用，请关闭浏览器无痕模式（隐身模式）后重试');
                }
            },
            getSessionStorage: function(key) {
                try {
                    return sessionStorage.getItem(key);
                } catch (e) {
                    console.log(e.message);
                    return 0;
                }
            },
            removeSessionStorage: function(key) {
                try {
                    sessionStorage.removeItem(key);
                } catch (e) {
                    console.log(e.message);
                    return 0;
                }
            },
            //获取本地存储
            getLocalStorage: function(key) {
                try {
                    return localStorage.getItem(key);
                } catch (e) {
                    console.log(e.message);
                    return 0;
                }
            },
            //存入本地存储
            setLocalStorage: function(key, value) {
                try {
                    localStorage.removeItem(key);
                    localStorage.setItem(key, value);
                    return 1;
                } catch (_) {
                    return 0;
                    //alert('该功能无法使用，请关闭浏览器无痕模式（隐身模式）后重试');
                }
            },
            removeLocalStorage: function(key) {
                localStorage.removeItem(key);
            },
            loading: function(options) {
                //默认是api.loading();
                //传了字符就是api.loading({ 'txt': '展示的文字' });
                //只要传了文字就不能恢复了，需要重新设置文字
                var oldloading = document.getElementById('loading'),
                    loading = document.createElement('div'),
                    loader = document.createElement('div'),
                    content = document.createElement('p'),
                    backBtn = document.createElement('a');
                if (options && options.txt && options.txt.trim() !== '') {
                    content.innerHTML = options.txt;
                } else {
                    try {
                        var _text = document.getElementById('loading').getElementsByClassName('loader')[0].getElementsByTagName('p')[0].textContent;
                        if (document.getElementById('loading') && _text !== '正在加载...') {
                            content.innerHTML = _text;
                        }
                    } catch (e) {
                        content.innerHTML = '正在加载...';
                    }
                }
                if (oldloading !== null && typeof(oldloading) !== 'undefined') {
                    //确保每次调用都能把文字写进去
                    document.getElementById('loading').getElementsByClassName('loader')[0].getElementsByTagName('p')[0].textContent = content.innerHTML;
                    //修改 loading隐藏方式
                    if (oldloading.classList && oldloading.classList.contains('endloading-opacity-0')) {
                        oldloading.classList.remove('endloading-opacity-0');
                    }
                    return;
                } else {
                    loading.id = 'loading';
                    loading.className = 'loading';
                    backBtn.className = 'loading-return-btn';
                    backBtn.setAttribute('href', api.backUrl);
                    loader.className = 'loader';
                    loading.appendChild(backBtn);
                    loading.appendChild(loader);
                    loader.appendChild(content);
                    document.body.appendChild(loading);
                }
            },
            endloading: function() {
                var loading = document.getElementById('loading');
                if (loading !== null && typeof(loading) !== 'undefined') {
                    //修改 loading隐藏方式
                    loading.classList.add('endloading-opacity-0');
                    //document.body.removeChild(loading);
                    //loading.style.cssText = 'display:none';
                }
            },
            toast: function(value, timeout) {
                var oldToast = document.getElementById('toast-pop');
                if (oldToast !== null && typeof(oldToast) !== 'undefined') {
                    return;
                } else {
                    if (!timeout || typeof(timeout) == 'undefined' || parseInt(timeout, 10) === 0) {
                        timeout = 3000;
                    }
                    var pop = document.createElement('div');
                    pop.className = 'common-pop';
                    pop.id = 'toast-pop';
                    pop.innerHTML = value;
                    document.body.appendChild(pop);
                    setTimeout(function() {
                        var pop = document.getElementById('toast-pop');
                        if (pop !== null && typeof(pop) !== 'undefined') {
                            document.body.removeChild(pop);
                        }
                    }, timeout);
                }
            },
            //获取接口
            post: function($http, $scope, path, controller, action, param, callback, timeout, needLogin, loading, postOnline) {
                var useCache = true;
                if (!timeout || typeof(timeout) == 'undefined' || parseInt(timeout, 10) === 0) {
                    timeout = 0;
                    useCache = false;
                }
                if (loading === '' || typeof(loading) == 'undefined' || loading === 1 || loading === null || loading === true) {
                    loading = 1;
                } else {
                    loading = 0;
                }
                // 特殊情况 postOnline 不等0的时候 都走 m站请求
                if (!postOnline || typeof(postOnline) == 'undefined' || parseInt(timeout, 10) === 0) {
                    postOnline = 0;
                }
                if (devicetype == 'ios' && postOnline == 0) {
                    ios.post($http, $scope, path, controller, action, param, useCache, callback, timeout, needLogin, loading);
                } else if (devicetype == 'android' && postOnline == 0) {
                    android.post($http, $scope, path, controller, action, param, useCache, callback, timeout, needLogin, loading);
                } else {
                    if (loading == 1) {
                        api.loading();
                    }
                    var user = api.getCookie('user');
                    if (!needLogin && typeof(needLogin) == 'undefined') {
                        if (param.toLowerCase().indexOf('user') > -1 && (!user || user === '' || user === '0' || typeof(user) == 'undefined')) {
                            needLogin = true;
                        } else {
                            needLogin = false;
                        }
                    } else if (needLogin === false) {
                        needLogin = false;
                    }
                    if (needLogin) {
                        location.href = 'https://demo.com/login.html?url=' + location.href;
                    } else {
                        postTimes++;
                        api.__callback__.push([postTimes, callback, $scope]);
                        var url = '/data/post';
                        if (api.serverVirtualDir !== '') {
                            url = '/' + api.serverVirtualDir + url;
                        }
                        var apiParam = {
                            path: path,
                            controller: controller,
                            action: action,
                            param: param,
                            useCache: useCache
                        };
                        var config = {
                            timeout: 20000,
                            headers: {}
                        };
                        var sParam = JSON.stringify(apiParam),
                            sessionData;
                        try {
                            sessionData = sessionStorage.getItem(sParam);
                        } catch (e) {
                            console.error('No trace mode can not be used sessionStorage and ...');
                        }
                        if (sessionData !== null && sessionData !== '') {
                            sessionData = JSON.parse(sessionData);
                            var sessionTime = sessionData.sTime,
                                localTime = new Date();
                            sessionTime = new Date(Date.parse(sessionData.sTime));
                            var timeResult = localTime - sessionTime;
                            if (timeResult <= timeout * 60000) {
                                api.endloading();
                                try {
                                    sessionData.JsonResult = decodeURIComponent(sessionData.JsonResult).replace(/\+/g, ' ').replace(/@plus@/g, '+');
                                } catch (e) {
                                    sessionData.JsonResult = unescape(sessionData.JsonResult).replace(/\+/g, ' ').replace(/@plus@/g, '+');
                                    console.log(e.message);
                                }
                                callback(sessionData);
                                return;
                            }
                        }
                        if (api.mvcVersion === '2.0') {
                            config.headers = {
                                params: sParam
                            };
                        }
                        $http.post(url, apiParam, config).success(function(data) {
                            api.endloading();
                            if (data.ErrorCode === 200 && timeout > 0) {
                                var sTime = new Date();
                                data.sTime = sTime;
                                var sData = JSON.stringify(data);
                                try {
                                    sessionStorage.setItem(sParam, sData);
                                } catch (e) {
                                    console.error('No trace mode can not be used sessionStorage and ...');
                                }
                            }
                            try {
                                data.JsonResult = decodeURIComponent(data.JsonResult).replace(/\+/g, ' ').replace(/@plus@/g, '+');
                                data.ErrorMsg = decodeURIComponent(data.ErrorMsg);
                            } catch (e) {
                                data.JsonResult = unescape(data.JsonResult).replace(/\+/g, ' ').replace(/@plus@/g, '+');
                                console.log(e.message);
                            } finally {
                                callback(data);
                            }
                        }).error(function(data) {
                            api.endloading();
                            data = {
                                ErrorCode: -3,
                                ErrorMsg: '服务请求超时，请重试'
                            };
                            try {
                                data.JsonResult = decodeURIComponent(data.JsonResult).replace(/\+/g, ' ').replace(/@plus@/g, '+');
                            } catch (e) {
                                data.JsonResult = unescape(data.JsonResult).replace(/\+/g, ' ').replace(/@plus@/g, '+');
                                console.log(e.message);
                            } finally {
                                callback(data);
                            }
                        });
                    }
                }
            },
            //app callback
            callback: function(index, data) {
                try {
                    data.JsonResult = decodeURIComponent(data.JsonResult).replace(/\+/g, ' ').replace(/@plus@/g, '+');
                    data.ErrorMsg = decodeURIComponent(data.ErrorMsg);
                } catch (e) {
                    console.error(e.message);
                }
                var currentArray = [];
                for (var i = 0; i < api.__callback__.length; i++) {
                    if (api.__callback__[i][0] == index) {
                        currentArray = api.__callback__[i];
                        api.__callback__.splice(i, 1);
                        break;
                    }
                }
                if (currentArray.length > 2) {
                    try {
                        currentArray[2].$apply(function() {
                            currentArray[1](data);
                        });
                    } catch (e) {
                        currentArray[1](data);
                        console.log(e.message);
                    }
                }
                api.endloading();
            },
            iosCallback: function(action, key, value, index, callbackData) {
                ios.callback(action, key, value, index, callbackData);
            },
            getUserId: function() {
                if (devicetype == 'ios') {
                    if (api.getQueryString('hybridversion') === '2') {
                        return api.getCookie('userid');
                    }
                    if (api.getQueryString('userid')) {
                        return api.getQueryString('userid');
                    } else {
                        return api.getLocalStorage('userid');
                    }
                } else if (devicetype == 'android') {
                    return window.cookie.getUserId();
                } else {
                    var user = api.getCookie('user');
                    if (!user || typeof(user) == 'undefined' || user.length < 5) {
                        return null;
                    } else {
                        return api.getValue('userid', user);
                    }
                }
            },
            login: function(from, forword) {
                if (from) {
                    api.loginConfig.from = from;
                }
                if (forword) {
                    api.loginConfig.forword = forword;
                }
                var loginParam = JSON.stringify(api.loginConfig);
                // 不是devicetype的 都是550以下的，550以下的用老的跳转方式。
                if (!api.isApp()) {
                    var source = api.getQueryString('source'),
                        loginUrl = '',
                        loginDomain = 'http:demo.com';
                    if (source == 'iphone' || source == 'android') {
                        loginUrl = '/AppLogin?loginSucceedUrl=' + loginDomain + '/mobile/AppAutoLogin';
                        loginUrl += '?refUrl=' + forword;
                        location.href = loginUrl;
                        return false;
                    } else {
                        location.href = 'https://demo.com/login.html?url=' + forword;
                    }
                } else {
                    if (devicetype == 'ios') {
                        ios.invokeApp('action.login', encodeURI(loginParam));
                    } else if (devicetype == 'android') {
                        window.action.exec('login', loginParam);
                    } else {
                        location.href = 'https://demo.com/login.html?url=' + forword;
                    }
                }
            },
            checkLogin: function(callback) {
                if (devicetype == 'ios') {
                    ios.checkLogin(callback);
                } else if (devicetype == 'android') {
                    android.checkLogin(callback);
                } else {
                    var user = api.getCookie('user');
                    if (!user || typeof(user) == 'undefined' || user.length < 5) {
                        location.href = 'https://demo.com/login.html?url=' + location.href;
                    } else {
                        callback();
                    }
                }
            },
            logout: function() {
                if (devicetype == 'ios') {
                    ios.logout();
                } else if (devicetype == 'android') {
                    android.logout();
                } else {
                    api.removeCookie('user');
                    location.href = 'https://demo.com/login.html';
                }
            },
            invoke: function(action, param) {
                if (devicetype == 'ios') {
                    ios.invokeApp(action, param);
                } else if (devicetype === 'android') {
                    try {
                        window.action.exec(action, param);
                    } catch (e) {
                        console.log(e.message);
                    }
                }
            },
            isApp: function() {
                if (devicetype === 'ios' || devicetype === 'android') {
                    return true;
                } else {
                    return false;
                }
            },
            isweixin: function() {
                if (weixin) {
                    return true;
                } else {
                    return false;
                }
            },
            /**
             * [router 调用子页面使用]
             * @param  {[type]} $http    [ng默认]
             * @param  {[type]} $scope   [ng默认]
             * @param  {[type]} $compile [ng默认]
             * @param  {[type]} viewName   [调用的子页面ID]
             * @param  {[type]} online 是否强制下载 默认0  否则传1 和url
             */
            router: function($http, $scope, $compile, online) {
                router.init($http, $scope, $compile, online);
                window.addEventListener('hashchange', function() {
                    router.init($http, $scope, $compile, online);
                });
            },
            __routerArray__: [],
            routerCallback: function(viewName, data) {
                if (devicetype === 'android') {
                    data = encodeURIComponent(data);
                }
                var currentArray = [];
                for (var i = 0; i < api.__routerArray__.length; i++) {
                    if (api.__routerArray__[i][0] == viewName) {
                        currentArray = api.__routerArray__[i];
                        break;
                    }
                }
                if (currentArray.length > 3) {
                    try {
                        currentArray[2].$apply(function() {
                            router.append(viewName, data, currentArray[1], currentArray[2], currentArray[3]);
                        });
                    } catch (e) {
                        router.append(viewName, data, currentArray[1], currentArray[2], currentArray[3]);
                        console.log(e.message);
                    }
                }
            },
            //子页面返回使用
            routerGoBack: function(viewName) {
                var section;
                //angular = window.angular;
                if (api.isApp()) {
                    if (viewName === '' || typeof(viewName) === 'undefined' || viewName === null) {
                        viewName = window.location.hash.replace('#\/', '');
                    }
                    section = document.getElementById('J_router_' + viewName);
                    section.classList.remove('J-router-show');
                }
                document.getElementsByClassName('demo-wrapper')[0].classList.remove('hide');
                api.endloading();
                window.location.hash = '';
                var ua = window.navigator.userAgent.toLowerCase();
                if (ua.indexOf('ucbrowser') > -1) {
                    setTimeout(function() {
                        document.body.scrollTop = api.__scrolltop__;
                    }, 100);
                } else {
                    document.body.scrollTop = api.__scrolltop__;
                }
            },
            // APP-5.5.0 新增详情页增加获取用户信息
            getUser: function() {
                var user_data = {},
                    user;
                //App旧版本
                if (devicetype === 'ios' && api.getQueryString('hybridversion') < '3' && !api.getQueryString('hybridversion')) {
                    user = api.getUserId();
                    user = JSON.parse(user);
                    user_data.userId = user.value;
                } else {
                    user = api.getCookie('user');
                }
                if (!user || typeof(user) === 'undefined' || user.length < 5 || user === '') {
                    user_data.userId = '';
                    return user_data;
                }
                //用户信息
                user_data.userName = api.getValue('userName', user);
                //邮箱
                user_data.email = api.getValue('Email', user);
                //真实姓名
                user_data.realname = api.getValue('realname', user);
                //用户ID
                user_data.userId = api.getValue('userid', user);
                //昵称
                user_data.nickname = api.getValue('nickname', user);
                //用户等级
                user_data.userGrade = api.getValue('userGrade', user);
                //众信ID
                user_data.utourid = api.getValue('utourid', user);
                return user_data;
            }
        };
    /**
     * [router 子页面调用的具体方法，不暴露]
     * @type {Object}
     */
    var router = {
        init: function($http, $scope, $compile, online) {
            var url = decodeURIComponent(window.location.href);
            var viewName = url.replace(/\//g, '').split('#')[1],
                self = this;
            if (devicetype == 'android') {
                //没有子页面是1
                //有子页面是0
                var param = {
                    'has': '1'
                };
                if (viewName === '' || viewName === '/' || typeof(viewName) === 'undefined' || !viewName) {
                    param.has = '1';
                } else {
                    param.has = '0';
                }
                api.invoke('action.hasChildrenPages', JSON.stringify(param));
            }
            // 判断之前是否加载过
            var div = document.getElementById('J_router_' + viewName);
            if (viewName && div === null) {
                api.__routerArray__.push([viewName, $http, $scope, $compile]);
                self.result(viewName, $http, $scope, $compile, online);
            } else {
                if (api.__routerArray__.length === 0) {
                    return;
                }
                var routerElements = document.querySelectorAll('.J-router');
                if (routerElements.length > 0) {
                    [].filter.call(routerElements, function(el) {
                        el.classList.remove('J-router-show');
                    });
                    document.getElementsByClassName('demo-wrapper')[0].classList.remove('hide');
                }
                if (typeof(viewName) != 'undefined' && viewName != '') {
                    api.__scrolltop__ = document.body.scrollTop;
                    document.getElementsByClassName('demo-wrapper')[0].classList.add('hide');
                    div.classList.add('J-router-show');
                }
            }
        },
        // result: function (viewName, $http, $scope, $compile, online, callback) {
        result: function(viewName, $http, $scope, $compile, online) {
            api.loading();
            var path = window.location.origin + window.location.pathname,
                getURL = '',
                urlArray = [],
                //data = '',
                self = this;
            if (path.indexOf('.html') > -1) {
                urlArray = path.split('/');
                path = '';
                for (var x = 0; x < urlArray.length - 1; x++) {
                    path += urlArray[x] + '/';
                }
            }
            if (path !== '' && path.substr(path.length - 1, 1) !== '/') {
                path += '/';
            }
            getURL = path + viewName + '.html';
            if (api.mvcVersion === '2.0') {
                getURL = path + viewName;
            }
            if (api.isApp()) {
                var param = JSON.stringify({
                    'online': online.toString(),
                    'url': getURL.toString(),
                    'viewName': viewName.toString()
                });
                //var obj = api.invoke('action.getfile', param);
                api.invoke('action.getfile', param);
            } else {
                var config = {
                    timeout: 20000
                };
                $http.get(getURL, {}, config).success(function(data) {
                    self.append(viewName, data, $http, $scope, $compile);
                    if (document.getElementsByClassName('demo-member-noload')[0]) { // 处理会员登录注册页面loading层级高的问题
                        document.getElementById('loading').classList.remove('zHide');
                    }
                }).error(function() {
                    window.location.hash = '';
                    api.endloading();
                    if (document.getElementsByClassName('demo-member-noload')[0]) { // 处理会员登录注册页面loading层级高的问题
                        document.getElementById('loading').classList.add('zHide');
                    }
                    api.toast('网络链接失败，请重试', 1500);
                    return 'false';
                });
            }
        },
        append: function(viewName, data, $http, $scope, $compile) {
            var angular = window.angular,
                createElement = document.createElement('section'),
                elementObj,
                el,
                scriptsData = '<script type="text/javascript">' + 'childrenFunction.' + viewName + '();' + '</script>';
            var htmlData;
            if (api.isApp()) {
                if (typeof(data) == 'string') {
                    if (devicetype === 'android') {
                        htmlData = decodeURIComponent(data);
                        data = {
                            ErrorCode: 200,
                            JsonResult: htmlData
                        };
                    } else {
                        data = JSON.parse(data);
                    }
                }
                if (data.ErrorCode != 200 || data.JsonResult.trim() == '') {
                    api.endloading();
                    if (devicetype === 'android') {
                        console.log('安卓子页面错误:' + data);
                    } else {
                        api.toast(data.ErrorMsg);
                    }
                    if (data.JsonResult.trim() == '') {
                        window.location.hash = '';
                        return;
                    }
                }
                if (devicetype == 'ios') {
                    elementObj = angular.element(decodeURIComponent(data.JsonResult) + scriptsData);
                } else {
                    var html = decodeURIComponent(data.JsonResult).replace(/&acute;/g, '\'');
                    html = html.replace(/&quot;/g, '"');
                    elementObj = angular.element(html + scriptsData);
                }
            } else {
                elementObj = angular.element(data + scriptsData);
            }
            createElement.id = 'J_router_' + viewName;
            createElement.className += 'J-router J-router-' + viewName;
            angular.element(document.body).append(createElement);
            el = $compile(elementObj)($scope);
            angular.element(createElement).append(el);
            var routerElements = document.querySelectorAll('.J-router');
            if (routerElements.length > 0) {
                api.__scrolltop__ = document.body.scrollTop;
                [].filter.call(routerElements, function(el) {
                    el.classList.remove('J-router-show');
                });
            }
            setTimeout(function() {
                document.getElementsByClassName('demo-wrapper')[0].classList.add('hide');
                createElement.classList.add('J-router-show');
                //angular.element(createElement).addClass('J-router-show').siblings().removeClass('J-router-show');
            }, 500);
        }
    };
    window.devicetype = devicetype = api.devicetype = api.getQueryString('devicetype');
    android = {
        getCookie: function(key) {
            var value = '';
            if (key == 'userid') {
                try {
                    value = window.cookie.getUserId();
                } catch (e) {
                    console.log(e.message);
                }
            } else {
                try {
                    value = window.cookie.getCookie(key);
                } catch (e) {
                    console.log(e.message);
                }
            }
            return value;
        },
        setCookie: function(key, value, timeout) {
            if (!timeout || typeof(timeout) == 'undefined') {
                timeout = 0;
            }
            window.cookie.setCookie(key, value, timeout);
        },
        removeCookie: function(key) {
            try {
                window.cookie.removeCookie(key);
            } catch (e) {
                console.log(e.message);
            }
        },
        checkLogin: function(callback) {
            var userid = android.getCookie('userid');
            if (!userid || typeof(userid) == 'undefined') {
                var loginParam = JSON.stringify({
                    from: location.href
                });
                window.action.exec('login', loginParam);
            } else {
                callback();
            }
        },
        logout: function() {
            android.removeCookie('user');
            var loginParam = JSON.stringify({
                from: location.href,
                forward: location.href
            });
            try {
                window.action.exec('login', loginParam);
            } catch (e) {
                console.log(e.message);
            }
        },
        post: function($http, $scope, path, controller, action, param, useCache, callback, timeout, needLogin, loading) {
            if (loading == 1) {
                api.loading();
            }
            postTimes++;
            api.__callback__.push([postTimes, callback, $scope]);
            var userid = android.getCookie('userid');
            if (!needLogin && typeof(needLogin) == 'undefined') {
                if (param.toLowerCase().indexOf('user') > -1 && (!userid || userid === '' || userid === '0' || typeof(userid) == 'undefined')) {
                    needLogin = true;
                } else {
                    needLogin = false;
                }
            } else if (needLogin === false) {
                needLogin = false;
            }
            if (needLogin === true) {
                var loginParam = JSON.stringify(api.loginConfig);
                try {
                    window.action.exec('login', loginParam);
                    callback({
                        ErrorCode: -2,
                        ErrorMsg: '未登录'
                    });
                } catch (e) {
                    console.log('调用Android登录失败，' + e.message);
                }
            } else {
                var apiParam = '';
                if (param && typeof(param) != 'undefined') {
                    apiParam = JSON.stringify({
                        Path: path.replace(/:/g, '：'),
                        ControllerName: controller,
                        ActionName: action,
                        PostData: JSON.parse(param)
                    });
                } else {
                    apiParam = JSON.stringify({
                        Path: path.replace(/:/g, '：'),
                        ControllerName: controller,
                        ActionName: action,
                        PostData: {}
                    });
                }
                try {
                    window.action.invoke(postTimes, apiUrlPre, apiParam, timeout);
                } catch (e) {
                    console.log(e.message);
                }
            }
        },
    };
    ios = {
        invokeApp: function(action, param) {
            if (api.getQueryString('hybridversion') === '3') {
                //ios10
                try {
                    var iframe;
                    iframe = document.createElement('iframe');
                    iframe.setAttribute('src', action + '://demo?' + param);
                    iframe.setAttribute('style', 'display:none;');
                    iframe.setAttribute('height', '0px');
                    iframe.setAttribute('width', '0px');
                    iframe.setAttribute('frameborder', '0');
                    document.body.appendChild(iframe);
                    iframe.parentNode.removeChild(iframe);
                    iframe = null;
                } catch (e) {
                    console.log(e.message);
                }
            } else if (api.getQueryString('hybridversion') === '2') {
                try {
                    window.action.execParam(action, param);
                } catch (e) {
                    console.log(e.message);
                }
            } else {
                var iframe;
                iframe = document.createElement('iframe');
                iframe.setAttribute('src', action + '://' + param);
                iframe.setAttribute('style', 'display:none;');
                iframe.setAttribute('height', '0px');
                iframe.setAttribute('width', '0px');
                iframe.setAttribute('frameborder', '0');
                document.body.appendChild(iframe);
                iframe.parentNode.removeChild(iframe);
                iframe = null;
            }
        },
        callback: function(action, key, value, index) {
            var currentArray = [];
            for (var i = 0; i < api.__iosCallback__.length; i++) {
                if (api.__iosCallback__[i][0] == index) {
                    currentArray = api.__iosCallback__[i];
                    api.__iosCallback__.splice(i, 1);
                    break;
                }
            }
            if (currentArray.length > 3) {
                currentArray[3](value);
            }
        },
        //
        getCookie: function(key) {
            var values = '';
            try {
                // 兼容5.5.0之前的老版本
                if (api.getQueryString('hybridversion') === '2') {
                    values = window.cookie.getCookie(key);
                } else {
                    values = api.getLocalStorage(key);
                }
                // 增加 判断 values是否为空
                if (values !== null && typeof(values) !== 'undefined' && values !== '') {
                    try {
                        var nowTime = (new Date()).getTime();
                        var obj = JSON.parse(values);
                        if (obj.expireTime === 0) {
                            return obj.value;
                        } else if (nowTime <= obj.expireTime) {
                            return obj.value;
                        } else {
                            return '';
                        }
                    } catch (e) {
                        return values;
                    }
                }
            } catch (e) {
                console.log('ios cookie' + e.message);
            }
        },
        setCookie: function(key, value, timeout) {
            var nowTime = new Date();
            var expireTime = 0;
            var values = {
                value: value,
                expireTime: expireTime
            };
            if (timeout > 0) {
                values.expireTime = new Date(nowTime.getTime() + timeout * 60000).getTime();
            }
            // 兼容5.5.0之前的老版本
            if (api.getQueryString('hybridversion') === '2') {
                window.cookie.setCookieValue(key, JSON.stringify(values));
            } else {
                api.setLocalStorage(key, JSON.stringify(values));
            }
        },
        removeCookie: function(key, index) {
            var param;
            if (key == 'user') {
                param = JSON.stringify({
                    index: index
                });
                ios.invokeApp('user.remove', index);
            } else {
                param = JSON.stringify({
                    index: index,
                    key: key
                });
                ios.invokeApp('cookie.remove', param);
            }
        },
        checkLogin: function(callback) {
            var userid = api.getLocalStorage('userid');
            if ((!userid || userid === '' || typeof(userid) == 'undefined')) {
                var loginParam = JSON.stringify({
                    from: location.href
                });
                ios.invokeApp('action.login', loginParam);
            } else {
                callback();
            }
        },
        logout: function() {
            iosGetTimes++;
            api.__iosCallback__.push([iosGetTimes, 'removeMemberID', 'user', function() {
                var loginParam = JSON.stringify({
                    action: 'login',
                    from: location.href,
                    forward: location.href
                });
                ios.invokeApp('action.exec', loginParam);
            }]);
            ios.removeCookie('user', iosGetTimes);
        },
        post: function($http, $scope, path, controller, action, param, useCache, callback, timeout, needLogin, loading) {
            if (loading == 1) {
                api.loading();
            }
            postTimes++;
            api.__callback__.push([postTimes, callback, $scope]);
            var url = '/api/' + apiUrlPre;
            var invokeParam;
            if (param && typeof(param) != 'undefined') {
                try {
                    var apiParam = {
                        Path: path.replace(/:/g, '：'),
                        ControllerName: controller,
                        ActionName: action,
                        PostData: JSON.parse(param)
                    };
                } catch (e) {
                    var apiParam = {
                        Path: path.replace(/:/g, '：'),
                        ControllerName: controller,
                        ActionName: action,
                        PostData: param
                    };
                    console.log(e.message);
                }
                invokeParam = encodeURI(JSON.stringify({
                    index: postTimes,
                    url: url,
                    param: apiParam,
                    usecache: timeout
                }));
            } else {
                invokeParam = encodeURI(JSON.stringify({
                    index: postTimes,
                    url: url,
                    param: {},
                    usecache: timeout
                }));
            }
            if (!needLogin && typeof(needLogin) == 'undefined') {
                if (param.toLowerCase().indexOf('user') > -1) {
                    needLogin = true;
                } else {
                    needLogin = false;
                }
            } else if (needLogin === false) {
                needLogin = false;
            }
            var userid = api.getUserId();
            if (needLogin === true && (!userid || userid === '' || typeof(userid) == 'undefined')) {
                var loginParam = JSON.stringify(api.loginConfig);
                ios.invokeApp('action.login', loginParam);
            } else {
                ios.invokeApp('action.invoke', invokeParam);
            }
        }
    };
    //最后执行api的初始化
    if (document.readyState == 'complete') {
        setTimeout(function() {
            api.init();
        }, 0);
    } else {
        document.addEventListener('DOMContentLoaded', function() {
            api.init();
        }, false);
        //追加load，防止万一
        window.addEventListener('load', function() {
            api.init();
        }, false);
    }
    return api;
})(window, document);