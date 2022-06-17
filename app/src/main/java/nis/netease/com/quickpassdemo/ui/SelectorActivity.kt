package nis.netease.com.quickpassdemo.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.netease.nis.quicklogin.QuickLogin
import com.netease.nis.quicklogin.listener.QuickLoginPreMobileListener
import kotlinx.android.synthetic.main.activity_select.*
import nis.netease.com.quickpassdemo.MyApplication
import nis.netease.com.quickpassdemo.R
import nis.netease.com.quickpassdemo.tools.openActivity
import nis.netease.com.quickpassdemo.tools.showToast

/**
 * @author liuxiaoshuai
 * @date 2022/3/18
 * @desc
 * @email liulingfeng@mistong.com
 */
class SelectorActivity : BaseActivity() {
    private var quickLogin: QuickLogin? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select)

        quickLogin = (application as MyApplication).quickLogin
        initListeners()
    }

    private fun initListeners() {
        demo_seletor?.setOnClickListener {
            quickLogin?.let {
                // 是否有效
                if (it.isPreLoginResultValid && !TextUtils.isEmpty((application as MyApplication).mobileNumber)) {
                    openActivity<OnePassActivity>(this) {}
                } else {
                    if (quickLogin?.checkNetWork(this) != 4 && quickLogin?.checkNetWork(this) != 5) {
                        quickLogin?.prefetchMobileNumber(object : QuickLoginPreMobileListener() {
                            override fun onGetMobileNumberSuccess(
                                token: String?,
                                mobileNumber: String?
                            ) {
                                (application as? MyApplication)?.mobileNumber = mobileNumber
                                Log.d("预取号成功", "易盾token${token}掩码${mobileNumber}")
                                openActivity<OnePassActivity>(this@SelectorActivity) {}
                            }

                            override fun onGetMobileNumberError(token: String?, msg: String?) {
                                msg?.showToast(this@SelectorActivity)
                                Log.e("预取号失败", "易盾token${token}错误信息${msg}")
                            }

                        })
                    } else {
                        // TODO 埋点上报，当前网络不支持一键登录（这部分作不作为基数适使用方自己情况定）
                        Log.e("预取号失败", "当前网络不支持一键登录")
                    }
                }
            }

        }

        demo_benji?.setOnClickListener {
            openActivity<BenjiActivity>(this) {}
        }
    }
}
