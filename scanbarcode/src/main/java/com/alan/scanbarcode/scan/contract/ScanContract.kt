package com.alan.scanbarcode.scan.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.alan.scanbarcode.ScanActivity


/**
 * 自定义跳转Activity接收返回值Contract
 * 使用out 指定泛型上界，即协变，等同于Java <? extends Activity>
 */
class ScanContract() :
    ActivityResultContract<Unit?, String?>() {
    override fun createIntent(context: Context, input: Unit?) =
        Intent(context, ScanActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode == Activity.RESULT_OK) return intent?.getStringExtra("barcode")
        return null
    }
}