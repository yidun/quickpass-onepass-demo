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
implementation 'io.github.yidun:onePass:1.5.7'
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
-dontwarn com.sdk.**
-keep class com.sdk.** { *;}
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
QuickLogin quickLogin = QuickLogin.getInstance(Context context, String businessId);
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
     * @param YDToken      易盾Token
     * @param mobileNumber 获取的手机号码掩码
     */
    void onGetMobileNumberSuccess(String YDToken, String mobileNumber);

    /**
     * 预取号失败
     * @param YDToken 易盾Token
     * @param msg     获取手机号掩码失败原因
     */
    void onGetMobileNumberError(String YDToken, String msg);
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
     * @param YDToken    易盾token
     * @param accessCode 运营商accessCode
     */
    void onGetTokenSuccess(String YDToken, String accessCode);

    /**
     * 获取运营商token失败
     * @param YDToken 易盾token
     * @param msg     出错提示信息
     */
    void onGetTokenError(String YDToken, String msg);

     /**
     * 取消一键登录
     */
    void onCancelGetToken();
}
```

### 4. 预取号是否过期(非必须)
预取号未过期可直接调用取号

#### 代码说明

```
quickLogin.isPreLoginResultValid()
```

#### 返回值说明

|类型|描述|
|----|----|
| boolean | true：已过期，需要重新预取号 false：未过期 |

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
quickLogin.setPrefetchNumberTimeout(int timeout)
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
quickLogin.getSDKVersion()
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

### 10. 本机校验

在初始化之后执行，本机校验和一键登录可共用初始化，本机校验界面需自行实现

#### 代码说明

```
getToken(String mobileNumber，QuickLoginTokenListener listener)
```

#### 参数说明

|参数|类型|是否必填|默认值|描述|
|----|----|--------|------|----|
|mobileNumber|String|是|无| 待校验手机号 |
|listener|QuickLoginTokenListener|是|无| 本机校验监听器 |

#### QuickLoginTokenListener 接口说明

```
public interface QuickLoginTokenListener {
    /**
     * 获取运营商token成功
     * @param YDToken    易盾token
     * @param accessCode 运营商accessCode
     */
    void onGetTokenSuccess(String YDToken, String accessCode);

    /**
     * 获取运营商token失败
     * @param YDToken 易盾token
     * @param msg     出错提示信息
     */
    void onGetTokenError(String YDToken, String msg);
}
```

## 常见报错

|code|message|说明|
|----|----|--------|
| 401 | forbidden | 易盾业务id不准确 |
||sign-invalid reqId:94b1328f9e1539f2a80ae1b8bd4bc6b6|签名与包名不对应|
