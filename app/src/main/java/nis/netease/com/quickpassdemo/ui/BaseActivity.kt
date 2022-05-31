package nis.netease.com.quickpassdemo.ui

import androidx.fragment.app.FragmentActivity
import nis.netease.com.quickpassdemo.tools.openActivity

/**
 * @author liuxiaoshuai
 * @date 2022/3/18
 * @desc
 * @email liulingfeng@mistong.com
 */
open class BaseActivity : FragmentActivity() {
    fun startResultActivity(token: String, accessToken: String, mobileNumber: String) {
        openActivity<ResultActivity>(this) {
            putExtra("token", token)
            putExtra("accessToken", accessToken)
            putExtra("mobileNumber", mobileNumber)
        }

    }
}