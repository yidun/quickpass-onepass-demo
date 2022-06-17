package nis.netease.com.quickpassdemo.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.netease.nis.quicklogin.listener.QuickLoginPreMobileListener
import com.netease.nis.quicklogin.listener.QuickLoginTokenListener
import kotlinx.android.synthetic.main.layout_quick_login_body.*
import kotlinx.android.synthetic.main.layout_quick_login_privacy.*
import nis.netease.com.quickpassdemo.MyApplication
import nis.netease.com.quickpassdemo.R
import nis.netease.com.quickpassdemo.tools.showToast

/**
 * @author liuxiaoshuai
 * @date 2022/5/31
 * @desc
 * @email liulingfeng@mistong.com
 */
class OnePassActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onepass)

        val application = application as? MyApplication
        setStatusBarColor(Color.parseColor("#ffffff"))
        setStatusBarTextColor(true)
        application?.mobileNumber?.let {
            oauth_mobile_et.setText(it)
        }

        quick_login_privacy_checkbox.setOnCheckedChangeListener { box, checked ->
            if (checked) {
                box.setBackgroundResource(R.drawable.login_demo_check_cus)
            } else {
                box.setBackgroundResource(R.drawable.login_demo_uncheck_cus)
            }
        }

        oauth_login?.setOnClickListener {
            if (quick_login_privacy_checkbox.isChecked) {
                application?.quickLogin?.let {
                    // 是否有效
                    if (it.isPreLoginResultValid) {
                        it.onePass(object : QuickLoginTokenListener() {
                            override fun onGetTokenSuccess(token: String?, accessCode: String?) {
                                finish()
                                Log.d("取号", "易盾token${token}运营商token${accessCode}")
                                token?.let {
                                    accessCode?.let { accessCode ->
                                        startResultActivity(it, accessCode, "")
                                    }
                                }
                            }

                            override fun onGetTokenError(token: String?, msg: String?) {
                                finish()
                                msg?.showToast(this@OnePassActivity)
                                Log.e("取号", "易盾token${token}错误信息${msg}")
                            }

                        })
                    } else {
                        it.prefetchMobileNumber(object : QuickLoginPreMobileListener() {
                            override fun onGetMobileNumberSuccess(
                                token: String?,
                                mobileNumber: String?
                            ) {
                                Log.d("预取号成功", "易盾token${token}掩码${mobileNumber}")
                                it.onePass(object : QuickLoginTokenListener() {
                                    override fun onGetTokenSuccess(
                                        token: String?,
                                        accessCode: String?
                                    ) {
                                        finish()
                                        Log.d("取号", "易盾token${token}运营商token${accessCode}")
                                        token?.let {
                                            accessCode?.let { accessCode ->
                                                startResultActivity(it, accessCode, "")
                                            }
                                        }
                                    }

                                    override fun onGetTokenError(token: String?, msg: String?) {
                                        finish()
                                        msg?.showToast(this@OnePassActivity)
                                        Log.e("取号", "易盾token${token}错误信息${msg}")
                                    }

                                })
                            }

                            override fun onGetMobileNumberError(token: String?, msg: String?) {
                                // 预取号异常主动退出，否则用户一直预取号造成大量异常数据
                                finish()
                                msg?.showToast(this@OnePassActivity)
                                Log.e("预取号失败", "易盾token${token}错误信息${msg}")
                            }

                        })

                    }
                }

            } else {
                "请同意隐私协议".showToast(this)
            }
        }
    }

    private fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            if (color != Color.TRANSPARENT) {
                //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                //需要设置该flag才能调用 setStatusBarColor 来设置状态栏颜色
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = color
            }
        }
    }

    private fun setStatusBarTextColor(isDarkColor: Boolean) {
        val window = this.window
        //需要设置该flag才能调用 setStatusBarColor 来设置状态栏颜色
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // 绘画模式
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        }
        if (isDarkColor) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}
