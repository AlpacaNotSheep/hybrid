hybrid常用来形容App混合框架。
## 什么是App混合框架呢？
> 1. 为了加快开发效率（节省人员、时间成本）;
> 2. 为了快速更新（控制用户终端版本），使用H5+JavaScript 替代native开发的技术。

##
## 目前混合框架都有哪些呢？
> 目前常见的混合框架大概可归纳为两个流派。
>1. H5+原生部分，使用webview实现自定义约定交互。
> 常见框架有:
>* **Adobe PhoneGap--Apache cordova** [https://cordova.apache.org/](https://cordova.apache.org/) 打包资源成apk/ipa，有插件库供开发者使用，也可自行开发交互插件。
>   ##### 2011年10月4日Adobe公司收购了PhoneGap和PhoneGap Build的新创公司Nitobi Software，随后将Phonegap的核心代码剥离并捐给了Apache公司，并改名为了Cordova。
>
>* **ionic** [http://ionicframework.com/](http://ionicframework.com/) 可以认为是cordova的一个封装版本，基于angularjs语法开发，使用cordova进行编译；ionic native是ionic团队使用TypeScript，ES6+重写的常用插件，统一了语法和代码规范。
>* **apicloud** [http://www.apicloud.com/](http://www.apicloud.com/) 国内厂商，集成了自己的插件库，编辑器，上传分发平台。
>* **dcloud mui** [http://dev.dcloud.net.cn/mui/](http://dev.dcloud.net.cn/mui/) 
>* **meteor** [https://www.meteor.com/](https://www.meteor.com/)
>
>2. 使用JavaScript开发,通过定义更强的约束规则，使用编译工具编译或解析为Native代码进行执行。运行时为原生代码，避免了webview内存泄露、绘制掉帧的情况，提高了渲染和交互性能。
>    ##### 也有说法认为这种情况不属于hybrid范畴，但从广义上讲，又何尝不是一种混合开发呢。
>    常见框架有：
>* **Facebook ReactNative** [https://github.com/facebook/react-native/](https://github.com/facebook/react-native/) 大厂出品，粉丝众多，文档全。但存在这通用性差的缺点，RN无法用于M站，Android/iOS也需要单独的js进行维护，似乎为了解决性能问题，又违背了混合开发节省人力的初衷（节省了客户端开发，但又增加了前端开发，尴尬的由 write once，run anywhere 变成了Learn once，write anywhere）。
>
>* **微信小程序** [官方文档](https://mp.weixin.qq.com/debug/wxadoc/dev/) 自定义WXML、WXSS、JSSDK的强约束H5子集，便于解析对照。如果能开源并推广行业标准，可能会更热。因为只服务于微信，估无RN write anywhere的尴尬。
>
#### 随着前端技术的蓬勃发展，很快就会出现各类命名的混合框架，到时候我在再更新吧。

## 进入正题，那这个框架又是什么呢？
> 简单的说是**平民**、**低配版**混合开发框架，实现了约定契约，自定义交互，平台化打包、差异化发布、撤回，监控反馈用户更新状态、版本使用情况等功能。（说的那么高端还平民？嗯，看代码就知道了。）
> ### 优点：
>* 学习曲线低，搭配angularjs/vue实现前端mvvm快速绑定；
>* 全流程断点，无商业利益的封装和限制，有断点才放心嘛；
>* 成本低廉，不形容了，自行体会。
> #### 今天先写到这里。
> ### **TODO:**
>* 如何搭建
>* 如何使用
>* 如何自行封装约定
>* 原理解释及坑坑洼洼
>
> 感谢“不是绵羊，是羊驼”([AlpacaNotSheep](https://github.com/AlpacaNotSheep/)) 组织各成员的辛苦贡献。









