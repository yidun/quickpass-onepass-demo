package nis.netease.com.quickpassdemo

import android.app.Application
import com.netease.nis.quicklogin.QuickLogin

/**
 * @author liuxiaoshuai
 * @date 2022/3/18
 * @desc
 * @email liulingfeng@mistong.com
 */
class MyApplication : Application() {
    var quickLogin: QuickLogin? = null
    var mobileNumber: String? = ""

    /**
     * 一键登录业务id
     */
    private val onePassId: String = "xxx"

    override fun onCreate() {
        super.onCreate()

        quickLogin = QuickLogin.getInstance(this, onePassId)
        quickLogin?.setDebugMode(true)
    }
}