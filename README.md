# 号码认证
直连三大运营商，一步校验手机号与当前 SIM 卡号一致性。优化注册/登录/支付等场景验证流程，有效提升拉新转化率和用户留存率

## 业务场景介绍
| 业务场景 | 说明                                                         |
| -------- | ------------------------------------------------------------ |
| 一键登录 | 用户无需输入手机号码，通过 SDK 拉起授权页，用户确认授权后，SDK 会获取 token，服务端携带 token 到运营商网关获取用户当前上网使用的流量卡号码，并返回给 APP 服务端 |
| 本机校验 | 用户输入手机号码，服务端携带手机号码和 token 去运营商网关进行校验比对，返回的校验结果是用户当前流量卡号码与服务端携带的手机号码是否一致 |

## 兼容性
| 条目        | 说明                                                                      |
| ----------- | -----------------------------------------------------------------------  |
| 适配版本    | minSdkVersion 16 及以上版本                                                 |

## 环境准备
| 条目        | 说明           |
| ----------- | -------------- |
| 网络制式    | 支持移动2G/3G/4G/5G<br>联通3G/4G/5G<br>电信4G/5G<br>2G、3G因网络环境问题，成功率低于4G |
| 网络环境    | 蜂窝网络<br> 蜂窝网络+WIFI同开<br> 双卡手机，取当前发流量的卡号                         |

## 资源引入

### 远程仓库依赖(推荐)
从 1.5.1 版本开始，提供远程依赖的方式，本地依赖的方式逐步淘汰。本地依赖集成替换为远程依赖请先去除干净本地包，避免重复依赖冲突

确认 Project 根目录的 build.gradle 中配置了 mavenCentral 支持

```
buildscript {
    repositories {
        mavenCentral()
    }
    ...
}

allprojects {
    repositories {
        mavenCentral()
    }
}
```
在对应 module 的 build.gradle 中添加依赖

```
implementation 'io.github.yidun:onePass:1.6.9'
```

## 各种配置

### 权限配置

SDK 建议开发者申请如下权限

```
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```
本权限用于移动运营商在双卡情况下更精准的获取数据流量卡的运营商类型，缺少该权限存在取号失败率上升的风险

READ_PHONE_STATE 权限是隐私权限，Android 6.0 及以上需要动态申请。使用前务必先动态申请权限

```
ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 0);
```

SDK内部权限说明

| 权限名字 | 权限说明   | 权限说明 |
| ------- | -------- | -------- |
| INTERNET | 允许应用程序联网 | 用于访问网关和认证服务器 |
| ACCESS_WIFI_STATE | 允许访问WiFi网络状态信息 | 允许程序访问WiFi网络状态信息 |
| ACCESS_NETWORK_STATE | 允许访问网络状态 | 区分移动网络或WiFi网络 |
| CHANGE_NETWORK_STATE | 允许改变网络连接状态 | 设备在WiFi跟数据双开时，强行切换使用数据网络 |
| CHANGE_WIFI_STATE | 允许改变WiFi网络连接状态 | 设备在WiFi跟数据双开时，强行切换使用 |
| READ_PHONE_STATE | 允许读取手机状态 |（可选）获取IMSI用于判断双卡和换卡 |


### 混淆配置

在 proguard-rules.pro 文件中添加如下混淆规则

```
-dontwarn com.cmic.sso.sdk.**
-keep class com.cmic.sso.**{*;}
-dontwarn com.unicom.online.account.shield.**
-keep class com.unicom.online.account.shield.** {*;}
-dontwarn com.unicom.online.account.kernel.**
-keep class com.unicom.online.account.kernel.** {*;}
-keep class cn.com.chinatelecom.account.**{*;}
-keep public class * extends android.view.View
-keep class com.netease.nis.quicklogin.entity.**{*;}
-keep class com.netease.nis.quicklogin.listener.**{*;}
-keep class com.netease.nis.quicklogin.QuickLogin{
    public <methods>;
    public <fields>;
}
-keep class com.netease.nis.quicklogin.helper.UnifyUiConfig{*;}
-keep class com.netease.nis.basesdk.**{
    public *;
    protected *;
}
```

## 快速调用示例

[demo](https://github.com/yidun/quickpass-onepass-demo)

demo使用注意事项：

1. 将 app 的 build.gradle 里面的 applicationId 换成自己的测试包名
2. 将 app 的 build.gradle 里面的签名配置改成您自己的签名配置
3. 将初始化的 businessId 换成您在易盾平台创建应用后生成的 businessId

## SDK 方法说明

### 1. 初始化

使用拉取授权页功能前必须先进行初始化操作，建议放在 Application 的 onCreate() 方法中

#### 代码说明

```
QuickLogin quickLogin = QuickLogin.getInstance();
quickLogin.init(Context context, String businessId);
```

#### 参数说明

|参数|类型|是否必填|默认值|描述|
|----|----|--------|------|----|
|context|Context|是|无| 上下文 |
|businessId|String|是|无| 号码认证业务 id |

### 2. 预取号

#### 注意事项

- 用户处于未授权状态时，调用该方法
- 已授权的用户退出当前帐号时，调用该方法
- 在执行拉取授权页的方法之前，提前调用此方法，以提升用户前端体验
- **此方法需要 1~2s 的时间取得临时凭证，不要和拉取授权页方法一起串行调用。建议放在启动页的 onCreate() 方法中或者 Application 的 onCreate() 方法中去调用**
- 不要频繁的多次调用

#### 代码说明

```
quickLogin.prefetchMobileNumber(QuickLoginPreMobileListener listener)
```

#### 参数说明

|参数|类型|是否必填|默认值|描述|
|----|----|--------|------|----|
|listener|QuickLoginPreMobileListener|是|无| 预取号回调监听 |

#### QuickLoginPreMobileListener 接口说明

```
public interface QuickLoginPreMobileListener {
    /**
     * 预期号成功
     * @param ydToken      易盾Token
     * @param mobileNumber 获取的手机号码掩码
     */
    void onGetMobileNumberSuccess(String ydToken, String mobileNumber);

    /**
     * 预取号失败
     * @param ydToken 易盾Token
     * @param msg     获取手机号掩码失败原因
     */
    void onGetMobileNumberError(String ydToken,int code,String msg);
}
```

### 3. 取号

#### 注意事项

- 用于获取 accessCode，accessCode 可用于获取真实手机号

#### 代码说明

```
quickLogin.onePass(QuickLoginTokenListener listener)
```

#### 参数说明

|参数|类型|是否必填|默认值|描述|
|----|----|--------|------|----|
|listener|QuickLoginTokenListener|是|无|取号回调监听 |

#### QuickLoginTokenListener 接口说明

```
public interface QuickLoginTokenListener {
    /**
     * 获取运营商token成功
     * @param ydToken    易盾token
     * @param accessCode 运营商accessCode
     */
    void onGetTokenSuccess(String ydToken, String accessCode);

    /**
     * 获取运营商token失败
     * @param ydToken 易盾token
     * @param code    错误码
     * @param msg     出错提示信息
     */
    void onGetTokenError(String ydToken, int code, String msg);
}
```

### 4. 预取号token是否有效(非必须)
预取号未过期可直接调用取号

#### 代码说明

```
quickLogin.isPreLoginResultValid()
```

#### 返回值说明

|类型|描述|
|----|----|
| boolean | true：有效 false：已过期，需要重新预取号 |

### 5. 判断运营商类型(非必须)

#### 代码说明

```
quickLogin.checkNetWork(Context context)
```

#### 参数说明

|参数|类型|是否必填|默认值|描述|
|----|----|--------|------|----|
|context|Context|是|无| 上下文 |

#### 返回值说明

|类型|描述|
|----|----|
| int | 1：电信 2：移动 3：联通 4:wifi 5：未知 |

### 6. 设置预取号超时时间(非必须)

#### 代码说明

```
quickLogin.setPrefetchNumberTotalTimeout(int timeout)
```

#### 参数说明

|参数|类型|是否必填|默认值|描述|
|----|----|--------|------|----|
|timeout|int|是|8| 单位秒 |

### 7. 设置本机校验超时时间(非必须，仅联通有效)

#### 代码说明

```
quickLogin.setFetchNumberTimeout(int timeout)
```

#### 参数说明

|参数|类型|是否必填|默认值|描述|
|----|----|--------|------|----|
|timeout|int|是|5| 单位秒 |

### 8. 返回 SDK 版本号(非必须)

#### 代码说明

```
quickLogin.getSdkVersion()
```

#### 返回值说明

|类型|描述|
|----|----|
| String | 版本号 |

### 9. 设置是否打开日志(非必须)

#### 代码说明

```
quickLogin.setDebugMode(boolean debug)
```

#### 参数说明

|参数|类型|是否必填|默认值|描述|
|----|----|--------|------|----|
|debug|boolean|是|false| 是否打印日志 |

### 10. 获取运营商名称(非必须，需要在预取号成功后调用)

#### 代码说明

```
quickLogin.getSlogan()
```

### 11. 获取运营商隐私协议标题(非必须，需要在预取号成功后调用)

#### 代码说明

```
quickLogin.getProtocol()
```

### 12. 获取运营商隐私协议地址(非必须，需要在预取号成功后调用)

#### 代码说明

```
quickLogin.getProtocolUrl()
```


### 13. 本机校验

在初始化之后执行，本机校验和一键登录可共用初始化，本机校验界面需自行实现

#### 代码说明

```
getToken(QuickLoginTokenListener listener)
```

#### 参数说明

|参数|类型|是否必填|默认值|描述|
|----|----|--------|------|----|
|listener|QuickLoginTokenListener|是|无| 本机校验监听器 |

#### QuickLoginTokenListener 接口说明

```
public interface QuickLoginTokenListener {
    /**
     * 获取运营商token成功
     * @param ydToken    易盾token
     * @param accessCode 运营商accessCode
     */
    void onGetTokenSuccess(String ydToken, String accessCode);

    /**
     * 获取运营商token失败
     * @param ydToken 易盾token
     * @param code    错误码
     * @param msg     出错提示信息
     */
    void onGetTokenError(String ydToken, int code, String msg);
}
```

## 常见报错

### 公共异常
|code|说明|
|----|--------|
| 401| 易盾业务id不准确 |
| -2 |json解析异常|
| -3 |移动运营商注入页面body失败|
| -4 |网络环境仅wifi|
| -5 |网络环境未知|
| -6 |SDK内部异常|
| -7 |Aes解密失败|

### 移动运营商异常
|code|说明|
|----|--------|
| 102101 |无网络|
| 102102 |网络异常|
| 102103 |未开启数据网络|
| 102203 |输入参数错误|
| 102223 |数据解析异常（一般是卡欠费）|
| 102508 |数据网络切换失败（从9.6.1版本开始，Android5.0以下不支持网络切换）|
| 102507 |授权登录超时|
| 103101 |请求签名错误（appkey不正确）|
| 103102 |包签名错误（appid和包签名不对应）|
| 103111 |网关IP错误（开了VPN或境外ip）|
| 103119 |appid不存在|
| 103412 |无效的请求（1.加密方式错误2.非json格式3.空请求）|
| 103414 |参数校验异常|
| 103511 |服务器ip白名单校验失败|
| 103811 |token为空|
| 103902 |scrip失效|
| 103911 |token请求过于频繁|
| 104201 |token已失效或不存在|
| 105002 |移动取号失败（一般是物联网卡）|
| 105018 |token权限不足|
| 105019 |应用未授权|
| 105021 |当天已达取号限额|
| 105302 |appid不在白名单|
| 105312 |余量不足|
| 105313 |非法请求|
| 105317 |受限用户|
| 200002 |用户未安装sim卡|
| 200010 |无法识别sim卡或没有sim卡|
| 200023 |登录超时|
| 200005 |用户未授权（READN_PHONE_STATE）|
| 200021 |数据解析异常（一般是卡欠费|
| 200022 |无网络|
| 200023 |请求超时|
| 200024 |数据网络切换失败|
| 200025 |socket、系统未授权数据蜂窝权限|
| 200026 |输入参数错误|
| 200027 |未开启数据网络或网络不稳定|
| 200028 |网络异常|
| 200038 |异网取号网络请求异常|
| 200039 |异网取号网关取号异常|
| 200040 |UI资源加载异常|
| 200050 |EOF异常|
| 200072 |CA根证书校验失败|
| 200080 |本机号码验证仅支持移动手机号|
| 200082 |服务器繁忙|

### 电信运营商异常
|code|说明|
|----|--------|
| -64 |服务器繁忙|
| -65 |服务器繁忙|
| -10001 |取号失败|
| -10002 |参数错误|
| -10003 |解密失败|
| -10004 |ip受限|
| -10005 |异网取号回调参数异常|
| -10006 |Mdn取号失败|
| -10007 |重定向到异网取号|
| -10008 |超过预设取号阈值|
| -10009 |时间戳过期|
| -20005 |sign-invalid（签名错误）|
| -20006 |应用不存在|
| -20007 |公钥数据不存在|
| -20100 |内部解析错误|
| -20102 |加密参数解析失败|
| -30001 |非法时间戳|
| -30003 |topClass失效|
| 51002 |参数为空|
| 51114 |无法获取手机号数据|
| 80000 |请求超时|
| 80001 |请求网络异常|
| 80002 |响应码错误|
| 80003 |无网络连接|
| 80004 |移动网络未打开|
| 80005 |Socket超时异常|
| 80006 |域名解析异常|
| 80007 |IO异常|
| 80100 |登录结果为空|
| 80101 |登录结果异常|
| 80102 |预登录异常|
| 80103 |SDK未初始化|
| 80104 |未调用预登录接口|
| 80105 |加载nib文件异常|
| 80800 |WIFI切换超时|
| 80801 |WIFI切换超时|

### 联通运营商异常
|code|说明|
|----|--------|
| 101001 |授权码不能为空|
| 101002 |认证的手机号不能为空|
| 101003 |UiConfig不能为空|
| 101004 |ApiKey或PublicKey不能为空|
| 101005 |超时|
| 101006 |公钥出错|
| 102001 |选择流量通道失败|
| 201001 |操作频繁，稍后再试|
| 302001 |SDK解密异常|
| 302002 |网络访问异常|
| 302003 |服务端数据格式出错|
| 111 |认证失败|
| 112 |认证失败|
| 1101 |公网ip无效|
| 1102 |私网ip无效|
| 1104 |授权码为空|
| 1105 |参数信息错误|
| 1106 |应用秘钥信息不匹配|
| 1107 |余额不足|
| 1108 |调用能力不匹配|
| 1201 |取号失败|
| 1202 |认证失败|
| 1203 |获取置换码失败|
| 2101 |鉴权失败|
| 2102 |accessCode已失效|
| 2103 |序列号不存在|
| 2201 |addid无效|
| 2202 |应用信息错误|
| 2203 |sdk信息错误|
| 2205 |接入信息解析错误|
| 2206 |流控制超限|
| 3201 |系统繁忙|
| 3202 |内部网关错误|
| 3203 |内部路由错误|
| 3205 |当前省份不支持取号|
| 3206 |取号功能暂时不可用|
